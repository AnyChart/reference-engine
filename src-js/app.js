goog.provide('app');
goog.require('goog.events');
goog.require('goog.dom');
goog.require('goog.style');

var locker;

app.startResize = function() {
    goog.style.showElement(locker, true);
    goog.events.listen(locker, goog.events.EventType.MOUSEMOVE, app.resize);
};

app.stopResize = function() {
    goog.style.showElement(locker, false);
    goog.events.unlisten(locker, goog.events.EventType.MOUSEMOVE, app.resize);
};

app.resize = function(e) {
    var x = e.screenX;
    if (x < 229) x = 229;
    var maxWidth = goog.dom.getViewportSize().width;
    if (x > maxWidth - 500) x = maxWidth - 500;
    
    goog.style.setStyle(
	goog.dom.getElement("sidebar"),
	"width",
	x+"px");
    
    goog.style.setStyle(
	goog.dom.getElement("main"),
	"left",
	x+"px");
};

app.initResize = function() {
    locker = goog.dom.getElement("locker");

    var resizer = goog.dom.getElement("resizer");
    goog.events.listen(resizer, goog.events.EventType.MOUSEDOWN, app.startResize);
    goog.events.listen(locker, goog.events.EventType.MOUSEUP, app.stopResize);
};

app.init = function() {
    app.initResize();
};
