var LOADED=false;

// onload should be able to set LOADED flag
function __onLoad() {
	LOADED=true;
}

// to test if onLoad actually got called
function isLoaded() {
	return LOADED;
}

// test if we can mat to interfaces
function testMyInterfaceCall(name) {
	return name;
}