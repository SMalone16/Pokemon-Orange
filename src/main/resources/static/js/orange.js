/* Orange overworld HUD. The server owns stock, battles and pursuit state. */
const OrangeUI = (() => {
  const names = ['Pokeball','Master Ball','Overload','Full Restore','Ether','Lasso'];
  const prices = [50,750,150,300,200,250];
  let hud, title, balls, shopPanel, toast, current, signature='', timer, markers = new Map();
  function el(tag, parent, text) {
    const node = document.createElement(tag);
    if (text !== undefined) node.textContent = text;
    if (parent) parent.appendChild(node);
    return node;
  }
  function mount() {
    if (hud) return;
    const style=el('style',document.head);
    style.textContent='#orange-hud{position:fixed;top:14px;right:14px;z-index:30;width:235px;font:13px Arial;color:#fff;background:#192232ed;border:1px solid #526070;padding:12px;border-radius:12px;box-shadow:0 5px 18px #0006}#orange-hud button{background:#e9eff6;border:0;border-radius:6px;padding:6px 9px;color:#142233;cursor:pointer;font-weight:bold}#orange-hud button:disabled{opacity:.4;cursor:default}#orange-hud .row{display:flex;align-items:center;justify-content:space-between;margin-top:6px;gap:5px}#orange-hud .rob{background:#ffcf73;width:100%;margin-top:9px}#orange-hud .ball{width:19px;height:19px;display:inline-block;border:2px solid #111;border-radius:50%;background:linear-gradient(#777 0 43%,#111 43% 57%,#777 57%);position:relative;margin:3px}#orange-hud .ball.hot{background:linear-gradient(#ff4b49 0 43%,#111 43% 57%,#fff 57%)}#orange-hud .ball:after{content:"";position:absolute;width:5px;height:5px;background:#fff;border:1px solid #111;border-radius:50%;left:6px;top:6px}#orange-toast{position:fixed;bottom:25px;left:50%;transform:translateX(-50%);z-index:110;color:white;background:#182234ed;border:1px solid #64748b;border-radius:10px;padding:12px 20px;font:bold 14px Arial;pointer-events:none}';
    hud=el('aside',document.body);hud.id='orange-hud';hud.hidden=true;
    title=el('strong',hud,'POKÉPOLICE');balls=el('div',hud);
    shopPanel=el('div',hud);
    toast=el('div',document.body);toast.id='orange-toast';toast.hidden=true;
  }
  function announce(message) {
    if (!message) return;
    mount();toast.textContent=message;toast.hidden=false;
    clearTimeout(timer);timer=setTimeout(()=>toast.hidden=true,4500);
  }
  function hide(){if(hud)hud.hidden=true;}
  function clear() {
    hide();signature='';
    markers.forEach(m=>{if(m.sprite && m.sprite.game)m.sprite.destroy();});
    markers.clear();
  }
  function send(shop,action,item) {
    if (!net.connected || Battle.inBattle || Game.playerFrozen) return;
    net.sendPacket(19,{id:Game.player.id,shop,action,item,quantity:1});
  }
  function marker(key,data,kind) {
    let m=markers.get(key);
    if(!m || !m.sprite.game || !m.sprite.parent) {
      const sprite=game.add.sprite(data.col*16,data.row*16,'atlas1','lucas/0');
      sprite.anchor.set(.25,.5);sprite.tint=kind==='shop'?0xffcb76:0x73baff;
      if(Game.entities) Game.entities.add(sprite);
      const label=game.add.text(8,-26,'',{font:'bold 10px Arial',fill:'#fff',stroke:'#172033',strokeThickness:3});
      label.anchor.set(.5,0);sprite.addChild(label);
      m={sprite,label};markers.set(key,m);
    }
    m.sprite.position.set(data.col*16,data.row*16);
    m.sprite.visible=true;
    m.sprite.tint=kind==='shop'?0xffcb76:(data.active?0xff5555:0x73baff);
    m.label.text=kind==='shop'?data.name:(data.active?'! PokePolice':'PokePolice');
  }
  function sync(data,loc) {
    if(!data||!loc||!Game.map||Game.loadingChunk||Game.chunkId!==loc.chunk_file)return;
    mount();
    if(data.notice && current!==data.notice){current=data.notice;announce(current);}
    if(Battle.inBattle){hide();return;}
    hud.hidden=false;
    title.textContent=data.wanted?(data.searching?'SEARCHING':'WANTED'):'POKÉPOLICE · CLEAR';
    if(balls.dataset.level!==String(data.wanted)){
      balls.dataset.level=String(data.wanted);balls.replaceChildren();
      for(let i=0;i<5;i++){const b=el('span',balls);b.className='ball'+(i<data.wanted?' hot':'');}
      balls.setAttribute('aria-label',data.wanted+' of 5 wanted levels');
    }
    const seen=new Set();
    for(const shop of data.shops){const key='s'+shop.id;seen.add(key);marker(key,shop,'shop');}
    for(const officer of data.police){const key='p'+officer.id;seen.add(key);marker(key,officer,'police');}
    for(const [key,m] of markers)if(!seen.has(key)){if(m.sprite.game)m.sprite.destroy();markers.delete(key);}
    const shop=data.shops.find(s=>Math.abs(s.row-loc.row)+Math.abs(s.col-loc.col)<=3);
    const next=JSON.stringify([shop||null,Game.player.currency,Game.playerFrozen]);
    if(next===signature)return;
    signature=next;shopPanel.replaceChildren();
    if(!shop){el('p',shopPanel,'Approach a gold shopkeeper to shop or challenge.');return;}
    el('strong',shopPanel,shop.name);
    shop.stock.forEach((count,i)=>{
      if(!count)return;
      const row=el('div',shopPanel);row.className='row';
      el('span',row,names[i]+' ×'+count);
      const buy=el('button',row,prices[i]+' coins');
      buy.disabled=Game.player.currency<prices[i]||Game.playerFrozen;
      buy.onclick=()=>send(shop.id,'buy',i);
    });
    const rob=el('button',shopPanel,'Challenge shopkeeper · Rob');rob.className='rob';
    rob.disabled=!shop.stock.some(n=>n>0)||Game.playerFrozen;
    rob.onclick=()=>send(shop.id,'rob');
  }
  return {sync,hide,clear,announce};
})();
