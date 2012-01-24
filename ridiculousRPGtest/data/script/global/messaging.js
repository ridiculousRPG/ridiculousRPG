function info(infoText) {
	$.serviceProvider.getService("messaging").showInfoNormal(infoText);
}

function say(messageText) {
	var msgService = $.serviceProvider.getService("messaging");
	msgService.showInfoNormal(messageText);
}

function commit() {
	
}