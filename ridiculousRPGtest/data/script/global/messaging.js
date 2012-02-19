function info(infoText) {
	$.serviceProvider.getService("messaging").info(infoText);
}

function box(x, y, width, height) {
	$.serviceProvider.getService("messaging").box(x, y, width, height);
}

function face() {
	return $.serviceProvider.getService("messaging").face();
}

function title(text) {
	$.serviceProvider.getService("messaging").title(text);
}

function say(text) {
	$.serviceProvider.getService("messaging").say(text);
}

function choice() {
	$.serviceProvider.getService("messaging").choice();
}

function input() {
	$.serviceProvider.getService("messaging").input();
}

function commit() {
	return $.serviceProvider.getService("messaging").commit();
}
