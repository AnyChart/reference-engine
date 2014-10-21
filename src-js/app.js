goog.provide('app');
goog.require('goog.events');
goog.require('goog.dom');
goog.require('goog.style');
goog.require('goog.dom.classes');

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
    var x = e.offsetX;
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

app.hideDialogs = function() {
    goog.style.setElementShown(goog.dom.getElement("version-toggle"), false);
    goog.dom.classes.remove(goog.dom.getElement("version-toggler"), "version-toggle");
};

app.toggleVersions = function(e) {
    var versions = goog.dom.getElement("version-toggle");
    var toggler = goog.dom.getElement("version-toggler");
    
    goog.style.setElementShown(versions, !goog.style.isElementShown(versions));
    goog.dom.classes.toggle(toggler, "version-toggle");
    e.stopPropagation();
};

app.initVersionToggle = function() {
    var toggler = goog.dom.getElement("version-toggler");
    goog.events.listen(toggler, goog.events.EventType.CLICK, app.toggleVersions);
};

app.initEditors = function() {
    var editors = goog.dom.getElementsByClass("code-sample");
    goog.array.map(editors, function(editorView) {
	var editor = ace.edit(editorView);
	editor.setTheme('ace/theme/tomorrow');
	editor.setOptions({maxLines: 30});
	editor.setReadOnly(true);
	editor.getSession().setMode("ace/mode/javascript");
    });
};

app.init = function(treeData) {
    console.log(treeData);
    app.initResize();
    app.initVersionToggle();
    app.initEditors();
    goog.events.listen(document, goog.events.EventType.CLICK, app.hideDialogs);
};
