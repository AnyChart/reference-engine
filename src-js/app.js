goog.provide('app');
goog.require('app.view');
goog.require('goog.events');
goog.require('goog.dom');
goog.require('goog.style');
goog.require('goog.dom.classes');
goog.require('goog.net.XhrIo');

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
    return false;
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

app.project = null;
app.version = null;
app.page = null;

app.updateLinks = function() {
    goog.array.map(goog.dom.getElementsByClass("type-link",
					       goog.dom.getElement("content")),
		   function(link) {
		       goog.events.listen(link, goog.events.EventType.CLICK, function(e) {
			   if (app.loadPage(link.getAttribute('href'), e)) {
			       return true;
			   }else {
			       e.preventDefault();
			       e.stopPropagation();
			       return false;
			   }
		       });
		   });
};

app.replacePage = function(page, data) {
    goog.dom.getElement("content").innerHTML = data.content;
    goog.dom.getElement("current-path").innerHTML = data.page;
    app.updateLinks();
    app.initEditors();
};

app.loadPage = function(page, e) {
    var original = page;
    page = page.replace(/#.*/g, "");
    if (e && (e.ctrlKey || e.shiftKey || e.metaKey)) return true;
    if (page == app.page) {
	return original.indexOf("#") != -1;
    }
    app.page = page;
    
    if (history.pushState) {
	goog.dom.getElement("content").innerHTML = "Loading...";
	if (e) {
	    history.pushState(null, null, page);
	}
	var request = new goog.net.XhrIo();
	goog.events.listen(request, "complete", function(e) {
	    if (request.isSuccess()) {
		app.replacePage(page, request.getResponseJson());
	    }else {
		// :(
		console.error(request.getResponseText());
	    }
	});
	request.send(page+"/json");
    
	return false;
    }else { return true; }
};

app.init = function(project, version, treeData) {
    if (project) {
	app.project = project;
	app.version = version;
    }
    app.page = location.pathname;
    
    app.initResize();
    app.initVersionToggle();
    app.initEditors();
    app.updateLinks();
    
    goog.events.listen(document, goog.events.EventType.CLICK, app.hideDialogs);

    React.renderComponent(TreeView({"tree": treeData}),
			  document.getElementById("tree"));

    goog.events.listen(window, goog.events.EventType.POPSTATE, function(e) {
	var path = document.location.pathname;
	app.loadPage(path);
	return false;
    });
};
