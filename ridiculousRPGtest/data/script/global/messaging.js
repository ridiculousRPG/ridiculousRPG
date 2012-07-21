function info(infoText) {
	$.serviceProvider.getService("messaging").info(infoText);
}

function box(x, y, width, height, boxAutoSize) {
	if (boxAutoSize == null) boxAutoSize = false;
	$.serviceProvider.getService("messaging").box(x, y, width, height, boxAutoSize);
}

function face(internalPath, x, y, width, height) {
	return $.serviceProvider.getService("messaging").face(internalPath, x, y, width, height);
}

function putPictureRegion(internalPath, x, y, width,
		height, posX, posY, posZkey) {
	$.serviceProvider.getService("messaging").putPicture(internalPath, x, y, width, height, posX, posY, posZkey);
}

function putPicture(internalPath, posX, posY, posZkey) {
	$.serviceProvider.getService("messaging").putPicture(internalPath, 0, 0, 0, 0, posX, posY, posZkey);
}

function removePicture(posZkey) {
	$.serviceProvider.getService("messaging").removePicture(posZkey);
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
