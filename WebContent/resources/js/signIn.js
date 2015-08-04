function onSignIn(googleUser) {
	var profile = googleUser.getBasicProfile();
	console.log('ID: ' + profile.getId());
	console.log('Name: ' + profile.getName());
	console.log('Image URL: ' + profile.getImageUrl());
	console.log('Email: ' + profile.getEmail());
	
	$("#email").text(profile.getEmail());
	$("#sign-in").css('display','none');
	$("#social-wrapper").css('display','block');
}

function signOut() {
	var auth2 = gapi.auth2.getAuthInstance();
	auth2.signOut().then(function () {
		console.log('User signed out.');
	});
	$("#social-wrapper").css('display','none');
	$("#sign-in").css('display','block');
}