package cs.brown.edu.aelp.pokemmo.pokemon;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomWildPokemon {
  // Kanto-inspired encounter bands adapted to the existing world and team progression.
  private static final String[][] POOLS = {
    {"pidgey","pidgey","rattata","rattata","caterpie","weedle","pikachu","spearow"},
    {"pidgey","spearow","ekans","sandshrew","nidoranm","nidoranf","oddish","bellsprout","jigglypuff","metapod","kakuna","zubat","geodude","magnemite"},
    {"raticate","pidgeotto","fearow","mankey","machop","geodude","zubat","onix","diglett","psyduck","poliwag","abra","gastly","growlithe","vulpix","magikarp"},
    {"golbat","graveler","machoke","haunter","kadabra","gloom","weepinbell","primeape","nidorina","nidorino","butterfree","beedrill","golduck","poliwhirl","dugtrio","arbok","sandslash","magneton","dratini","eevee"},
    {"pidgeot","raichu","arcanine","ninetales","golem","alakazam","machamp","gengar","vileplume","victreebel","dragonair","gyarados","snorlax","venusaur","charizard","blastoise"}
  };
  private static final int[] MIN = {2, 6, 13, 22, 32};
  private static final int[] MAX = {5, 12, 21, 31, 50};
  public static Pokemon generateWildPokemon(int averageLevel) {
    ThreadLocalRandom r = ThreadLocalRandom.current();
    int band = averageLevel < 8 ? 0 : averageLevel < 15 ? 1 : averageLevel < 24 ? 2 : averageLevel < 35 ? 3 : 4;
    if (band > 0 && r.nextInt(100) < 15) band--;
    else if (band < 4 && averageLevel >= MAX[band] - 2 && r.nextInt(100) < 10) band++;
    int level = r.nextInt(MIN[band], MAX[band] + 1);
    if (averageLevel >= 50) level = Math.min(70, Math.max(level, averageLevel - r.nextInt(3, 12)));
    String species = POOLS[band][r.nextInt(POOLS[band].length)];
    return PokemonLoader.load(species, Pokemon.calcXpByLevel(level));
  }
  public static <T> T randomElementFromList(List<T> list) {
    return list.get(ThreadLocalRandom.current().nextInt(list.size()));
  }
}
