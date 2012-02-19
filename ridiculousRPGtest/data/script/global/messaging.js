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

function choice(text, value) {
	$.serviceProvider.getService("messaging").choice(text, value);
}

function inputText(defaultText) {
	inputAdvanced(defaultText, -1, false, false);
}

function inputNumber(defaultNumber) {
	inputAdvanced(defaultNumber, -1, true, false);
}

function inputAdvanced(text, maximum, numberInput, password) {
	$.serviceProvider.getService("messaging").input(text, maximum, numberInput,
			password);
}

function commit() {
	return $.serviceProvider.getService("messaging").commit();
}
