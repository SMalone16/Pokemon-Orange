// Classroom join screen: no passwords, email addresses or class codes.
var Home = {};
var chatIdx = 0;
Home.init = function() {
	game.scale.pageAlignHorizontally = true;
	game.add.plugin(PhaserInput.Plugin);
};
Home.preload = function() {
	game.load.atlasJSONHash('atlas1', '/assets/sprites/pokemon_atlas1.png', '/assets/sprites/pokemon_atlas1.json');
	game.stage.disableVisibilityChange = true;
};
Home.create = function() {
	$("#chat-container").css('width', '0px').css('visibility', 'hidden');
	if (Cookies.get('id') && Cookies.get('token')) {
		net.connect(parseInt(Cookies.get('id'), 10), Cookies.get('token'));
		return;
	}
	const logo = game.add.sprite(game.width / 2, 30, 'atlas1', 'logo');
	logo.anchor.set(0.5, 0);
	logo.scale.setTo(0.5, 0.5);
	const form = document.createElement('form');
	form.id = 'classroom-join';
	form.style.cssText = 'position:fixed;z-index:50;top:50%;left:50%;transform:translate(-50%,-35%);width:min(340px,85vw);padding:28px;background:#f7f7f7;color:#27354a;border-radius:16px;box-shadow:0 8px 30px #0004;font:16px Arial;text-align:center';
	form.innerHTML = '<h1 style="margin:0 0 8px;font-size:27px">Pokémon Orange</h1><p>Choose a fictional player name to enter the world.</p><input aria-label="Display name" placeholder="Display name" required minlength="4" maxlength="12" pattern="[A-Za-z0-9]{4,12}" autocomplete="off" style="width:100%;box-sizing:border-box;padding:12px;font-size:18px;border:1px solid #aaa;border-radius:6px"><button style="display:block;width:100%;margin:14px 0;padding:12px;background:#e87823;color:white;border:0;border-radius:6px;font-size:18px;cursor:pointer">Join game</button><p style="font-size:13px">WASD or arrow keys to move · Random starter Pokémon</p><div role="status" style="color:#b42318"></div>';
	document.body.appendChild(form);
	const input = form.querySelector('input'), button = form.querySelector('button'), status = form.querySelector('[role=status]');
	input.focus();
	form.onsubmit = async event => {
		event.preventDefault();
		button.disabled = true;
		status.textContent = 'Joining…';
		try {
			const res = await fetch('/join', {method:'POST',body:new URLSearchParams({name:input.value.trim()})});
			const data = await res.json();
			if (!data.success) throw Error(data.message || 'Could not join.');
			const opts = {expires:7,secure:location.protocol==='https:',sameSite:'Strict'};
			Cookies.set('id', data.id, opts);
			Cookies.set('token', data.token, opts);
			form.remove();
			net.connect(data.id, data.token);
		} catch(e) {
			status.textContent = e.message || 'Connection error. Try again.';
			button.disabled = false;
		}
	};
};
Home.shutdown = function() {
	const form = document.getElementById('classroom-join');
	if (form) form.remove();
};
