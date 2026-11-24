/**
 * 
 */
function register(username, email, password, species, nickname, success, fail) {

	if (!username || !/^[A-Za-z0-9]{4,12}$/.test(username)) {
		fail({success: false, error: "Use 4-12 letters or numbers."});
		return;
	}
	
	let registerURL = '/register';
	
	error = false;
	
	if (username == undefined) {
		error = 'Please input a user name.';
	} else if (password == undefined) {
		error = 'Please input a password.';
	} else if (email == undefined) {
		error = 'Please enter your class code.';
	} else if (species == undefined) {
		error = 'Please select a pokemon.';
	} else if (nickname == undefined) {
		error = 'Please input a nickname.';
	}
	
	if (error != false) {
		 fail({
			success: false,
			error: error
		 });
		 
		 return;
	}

	$.ajax({
		type: "POST",
		url: registerURL,
		data: {
			username: username,
			classCode: email,
			password: password,
			species: species,
			nickname: nickname
		},
		success: function(data) {
			
			if (data.success == true) {
				
				success(data);
				return;
				
			} else {
				
				error = (data.message != undefined) ? data.message : "Unknown error occurred.";
				
				fail({
					success: false,
					error: error
				});
				
				return;
			}
		},
		error: function(xhr, status, err) {
			fail({
				success: false,
				error: "A network error occured!"
			});
			
			return;
		},
		dataType: 'json'
	});
}

function login(username, password, success, fail) {
	
	let loginURL = '/login';
	
	error = false;
	
	if (username == undefined) {
		error = 'Please input a user name.';
	} else if (password == undefined) {
		error = 'Please input a password.';
	}
	
	if (error != false) {
		 fail({
			success: false,
			error: error
		 });
		 
		 return;
	}

	$.ajax({
		type: "POST",
		url: loginURL,
		data: {
			username: username,
			password: password
		},
		success: function(data) {
			
			if (data.success == true) {
				
				success(data);
				return;
				
			} else {
				
				error = (data.message != undefined) ? data.message : "Unknown error occurred.";
				
				fail({
					success: false,
					error: error
				});
				
				return;
			}
		},
		error: function(xhr, status, err) {
			fail({
				success: false,
				error: "A network error occured!"
			});
			
			return;
		},
		dataType: 'json'
	});
}

function simpleLogin(username, password) {
	login(username, password, function(resp) {
		net.connect(resp.id, resp.token);
	}, function(resp) {
		console.log('Login failure: ' + resp.error);
	});
}

function simpleRegister(username, nickname, email, password) {
	
	register(username, email, password, 'pikachu', nickname, function(resp) {
		net.connect(resp.id, resp.token);
	}, function(resp) {
		console.log('Registration failure: ' + resp.error);
	});
}
