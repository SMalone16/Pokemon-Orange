package cs.brown.edu.aelp.pokemmo.map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cs.brown.edu.aelp.pokemmo.battle.BattleManager;
import cs.brown.edu.aelp.pokemmo.battle.Item;
import cs.brown.edu.aelp.pokemmo.battle.impl.WildBattle;
import cs.brown.edu.aelp.pokemmo.data.authentication.User;
import cs.brown.edu.aelp.pokemmo.data.authentication.UserManager;
import cs.brown.edu.aelp.pokemmo.pokemon.Pokemon;
import cs.brown.edu.aelp.pokemmo.pokemon.PokemonLoader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Server-owned, per-trainer arcade pursuit simulation. Coordinates are map tiles. */
public final class OrangeWorld {
  private OrangeWorld() {}
  private static final int[][] SHOPS = {{27,22},{28,43},{42,35}};
  private static final String[] SHOP_NAMES = {"Route Mart","Orange Supply","South Market"};
  private static final int[][] STOCK = {{5,0,1,2,1,0},{3,1,0,1,2,1},{6,0,2,1,1,1}};
  private static final int[][] HOMES = {{28,18},{29,26},{27,35},{29,46},{42,39}};
  private static final String[] POLICE = {"growlithe","machop","magneton","arcanine","machamp"};
  private static final int[] DR = {-1,1,0,0}, DC = {0,0,-1,1};
  private static final long RESTOCK = 120000L, STEP = 600L, GRACE = 12000L;
  private static final Map<Integer, Heat> STATES = new ConcurrentHashMap<>();

  private static final class Heat {
    int wanted, lastRow, lastCol, defeated;
    long lastTick, lastSeen, graceUntil;
    boolean searching;
    String notice = "";
    final long[] restock = new long[SHOPS.length];
    final long[] down = new long[HOMES.length];
    final int[][] officers = new int[HOMES.length][2];
    final int[][] stock = new int[SHOPS.length][];
    Heat() {
      for(int i=0;i<HOMES.length;i++) officers[i]=HOMES[i].clone();
      for(int i=0;i<SHOPS.length;i++) stock[i]=STOCK[i].clone();
    }
  }
  private static Heat state(User u) { return STATES.computeIfAbsent(u.getId(), id -> new Heat()); }
  private static int distance(int r,int c,int rr,int cc) { return Math.abs(r-rr)+Math.abs(c-cc); }
  private static boolean nearby(User u,int shop) {
    Location p=u.getLocation();
    return shop>=0 && shop<SHOPS.length && p.getChunk().getId()==1
        && distance(p.getRow(),p.getCol(),SHOPS[shop][0],SHOPS[shop][1])<=3;
  }
  private static void note(User u, Heat h,String msg) { h.notice=msg; u.sendMessage(msg); }
  private static void restock(Heat h,long now) {
    for(int i=0;i<SHOPS.length;i++) if(h.restock[i]!=0 && now>=h.restock[i]) {
      h.stock[i]=STOCK[i].clone(); h.restock[i]=0;
    }
  }
  private static boolean ready(User u) {
    return u.isConnected() && !u.isBusy() && !u.allPokemonKnockedOut()
        && u.getActivePokemon()!=null && !u.getActivePokemon().isKnockedOut();
  }
  private static int level(User u,int bonus) {
    return Math.max(3,Math.min(100,u.getActivePokemon().getLevel()+bonus));
  }
  private static void escalate(Heat h,Location p,long now) {
    h.wanted=Math.min(5,h.wanted+1); h.lastSeen=now; h.lastRow=p.getRow();
    h.lastCol=p.getCol(); h.searching=false; h.defeated=0;
    Arrays.fill(h.down,0); h.graceUntil=now+GRACE;
  }
  public static void purchase(User u,int shop,int item,int quantity) {
    Heat h=state(u);
    synchronized(h) {
      restock(h,System.currentTimeMillis());
      if(!ready(u) || !nearby(u,shop) || item<0 || item>=Item.ItemType.values().length
          || quantity<=0 || quantity>h.stock[shop][item]) { note(u,h,"Shop unavailable or out of stock."); return; }
      int total=Item.getCost(Item.ItemType.values()[item])*quantity;
      if(total>u.getCurrency()) { note(u,h,"Not enough coins."); return; }
      u.setCurrency(u.getCurrency()-total); u.getInventory().addItems(item,quantity);
      h.stock[shop][item]-=quantity;
      if(h.restock[shop]==0) h.restock[shop]=System.currentTimeMillis()+RESTOCK;
      note(u,h,"Purchase complete.");
    }
  }
  public static void rob(User u,int shop) {
    Heat h=state(u);
    synchronized(h) {
      long now=System.currentTimeMillis(); restock(h,now);
      if(!ready(u)||!nearby(u,shop)||Arrays.stream(h.stock[shop]).sum()==0) {
        note(u,h,"Move near a stocked shop with a healthy Pokemon."); return;
      }
      Pokemon foe=PokemonLoader.load("raticate",Pokemon.calcXpByLevel(level(u,1)));
      if(foe==null) return;
      u.setPath(null);
      BattleManager.getInstance().createNpcBattle(u,foe,SHOP_NAMES[shop]+" keeper",won -> {
        synchronized(h) {
          if(!won) { note(u,h,"The shopkeeper kept the stock."); return; }
          for(int i=0;i<h.stock[shop].length;i++) if(h.stock[shop][i]>0) {
            u.getInventory().addItems(i,h.stock[shop][i]); h.stock[shop][i]=0;
          }
          long end=System.currentTimeMillis(); h.restock[shop]=end+RESTOCK;
          escalate(h,u.getLocation(),end);
          note(u,h,"Stock seized! PokePolice alerted. Wanted level "+h.wanted+".");
        }
      });
    }
  }
  public static void tick() {
    for(User u:UserManager.getAllUsers()) {
      if(!u.isConnected()) continue;
      try { tickUser(u); } catch(RuntimeException e) { e.printStackTrace(); }
    }
  }
  private static void tickUser(User u) {
    Heat h=state(u);
    synchronized(h) {
      long now=System.currentTimeMillis();
      if(now-h.lastTick<STEP) return;
      h.lastTick=now; restock(h,now);
      Location p=u.getLocation();
      if(u.isBusy()) { if(h.wanted>0) h.lastSeen=now; return; }
      if(h.wanted==0) {
        for(int i=0;i<HOMES.length;i++) {
          int offset=(int)((now/1800+i)%6);
          int col=HOMES[i][1]+(offset<=3?offset:6-offset);
          if(p.getChunk().getId()==1 && p.getChunk().isWalkable(HOMES[i][0],col))
            h.officers[i][1]=col;
          h.officers[i][0]=HOMES[i][0];
        }
        return;
      }
      if(now<h.graceUntil) return;
      boolean outside=p.getChunk().getId()!=1;
      boolean seen=false;
      int radius=5+h.wanted;
      if(!outside) {
        for(int i=0;i<h.wanted;i++) if(h.down[i]<=now &&
            distance(p.getRow(),p.getCol(),h.officers[i][0],h.officers[i][1])<=radius)
          seen=true;
        if(seen) { h.lastSeen=now; h.lastRow=p.getRow(); h.lastCol=p.getCol(); }
      }
      h.searching=!seen;
      if(now-h.lastSeen>18000L+6000L*h.wanted) {
        h.wanted=0; h.searching=false; h.defeated=0; Arrays.fill(h.down,0);
        note(u,h,"WANTED CLEARED"); return;
      }
      if(outside) return;
      // Officers share a target and navigate the existing collision grid.
      for(int i=0;i<h.wanted;i++) {
        if(h.down[i]>now) continue;
        int[] o=h.officers[i];
        int[] next=nextStep(p.getChunk(),o[0],o[1],h.lastRow,h.lastCol);
        o[0]=next[0]; o[1]=next[1];
      }
      if(!ready(u)) return;
      for(int i=0;i<h.wanted;i++) {
        if(h.down[i]>now || distance(p.getRow(),p.getCol(),h.officers[i][0],h.officers[i][1])>1) continue;
        final int officer=i;
        Pokemon foe=PokemonLoader.load(POLICE[Math.min(4,h.wanted-1)],
            Pokemon.calcXpByLevel(level(u,h.wanted*2)));
        if(foe==null) return;
        u.setPath(null);
        BattleManager.getInstance().createNpcBattle(u,foe,"PokePolice • level "+h.wanted,won -> {
          synchronized(h) {
            long end=System.currentTimeMillis();
            h.lastSeen=end; h.graceUntil=end+GRACE;
            if(won) {
              h.down[officer]=end+60000L; h.defeated++;
              if(h.defeated>=h.wanted) {
                escalate(h,u.getLocation(),end);
                note(u,h,"Squad defeated. Reinforcements! Wanted level "+h.wanted+".");
              } else note(u,h,"Officer defeated. Other officers remain.");
            } else if(u.allPokemonKnockedOut()) {
              h.wanted=0; h.searching=false; h.defeated=0; Arrays.fill(h.down,0);
              note(u,h,"BUSTED! Your team will recover at the PokeCenter.");
            } else note(u,h,"You fled the encounter. PokePolice are still looking.");
          }
        });
        break;
      }
    }
  }
  // Breadth-first search returns one legal step. Never walks through Collision tiles.
  static int[] nextStep(Chunk c,int sr,int sc,int tr,int tc) {
    if(!c.isWalkable(sr,sc)||!c.isWalkable(tr,tc)) return new int[]{sr,sc};
    int w=c.getWidth(), start=sr*w+sc, target=tr*w+tc;
    if(start==target) return new int[]{sr,sc};
    int[] parent=new int[w*c.getHeight()]; Arrays.fill(parent,-1); parent[start]=start;
    ArrayDeque<Integer> q=new ArrayDeque<>(); q.add(start);
    while(!q.isEmpty() && parent[target]==-1) {
      int at=q.remove();
      for(int i=0;i<4;i++) {
        int r=at/w+DR[i], col=at%w+DC[i];
        if(!c.isWalkable(r,col)) continue;
        int n=r*w+col;
        if(parent[n]==-1) { parent[n]=at; q.add(n); }
      }
    }
    if(parent[target]==-1) return new int[]{sr,sc};
    int next=target; while(parent[next]!=start) next=parent[next];
    return new int[]{next/w,next%w};
  }
  public static JsonObject snapshot(User u) {
    Heat h=state(u);
    synchronized(h) {
      long now=System.currentTimeMillis(); JsonObject out=new JsonObject();
      out.addProperty("wanted",h.wanted); out.addProperty("searching",h.searching);
      out.addProperty("notice",h.notice);
      JsonArray shops=new JsonArray(), police=new JsonArray();
      if(u.getLocation().getChunk().getId()==1) {
        for(int i=0;i<SHOPS.length;i++) {
          JsonObject s=new JsonObject(); s.addProperty("id",i); s.addProperty("name",SHOP_NAMES[i]);
          s.addProperty("row",SHOPS[i][0]); s.addProperty("col",SHOPS[i][1]);
          JsonArray stock=new JsonArray(); for(int n:h.stock[i]) stock.add(n);
          s.add("stock",stock); shops.add(s);
        }
        for(int i=0;i<HOMES.length;i++) {
          JsonObject o=new JsonObject(); o.addProperty("id",i);
          o.addProperty("row",h.officers[i][0]); o.addProperty("col",h.officers[i][1]);
          o.addProperty("active",i<h.wanted && h.down[i]<=now);
          police.add(o);
        }
      }
      out.add("shops",shops); out.add("police",police); return out;
    }
  }
}
