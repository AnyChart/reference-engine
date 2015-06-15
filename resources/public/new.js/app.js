function initPage(version, page, info) {

    window["version"] = version;

    $("div.method").each(function() {
        var $this = $(this);
        var $tabs = $this.find("ul.overrides");
        var $views = $this.find("div.overrides");
        $tabs.find(">li:first").addClass("active");
        $views.find(">div:first").addClass("active");
        
        $tabs.find("a").click(function() {
            var index = $(this).parent().index() + 1;
            $tabs.find(".active").removeClass("active");
            $views.find(".active").removeClass("active");
            
            $tabs.find("li:nth-child("+index+")").addClass("active");
            $views.find(">div:nth-child("+index+")").addClass("active");
            return false;
        });
    });

    $("div.listings").each(function() {
        $(this).find("a.btn").click(function() {
            var target = $(this).attr("href").substring(1);
            $("#"+target).toggle();
            return false;
        });

        $(this).find("div.code-listing").each(function() {
            var $this = $(this);
            $this.find("a.close").click(function() {
                $(this).parent().hide();
                return false;
            });
            var editor = ace.edit($this.find("div").get(0));
            editor.setTheme("ace/theme/tomorrow");
            editor.setOptions({"maxLines": 999999});
            editor.setReadOnly(true);
            editor.getSession().setMode("ace/mode/javascript");
        });
    });

    $("div.detailed").each(function() {
        var $this = $(this);
        var $view = $this.find(">div");
        $view.hide();
        $(this).find(">a:first").click(function() {
            $view.toggle();
            return false;
        });
        $view.find("a.close").click(function() {
            $view.hide();
            return false;
        });
    });

    var $classEl = $("#path-class");
    var $nsEl = $("#path-ns");
    if (info["kind"] == "namespace") {
        $classEl.hide();
        $nsEl.find("span").hide();
        $nsEl.find("a").text(page);
        $nsEl.find("a").attr("href", "/" + version + "/" + page);
    }else {
        var ns = page.replace(/\.[^\.]+$/, "");
        var c = page.match(/.+\.([^\.]+)$/)[1];
        $classEl.show();
        $nsEl.find("span").show();
        $nsEl.find("a").text(ns);
        $nsEl.find("a").attr("href", "/" + version + "/" + ns);
        $classEl.find("a").text(c);
        $classEl.find("a").attr("href", "/" + version + "/" + page);
    }

    $("#content .node-link").click(loadPageFromLink);
    $("#content .type-link").click(loadPageFromLink);
};

function expandInTree(page) {
    if (page.indexOf("#") != -1)
        page = page.substring(0, page.indexOf("#"));

    var parent = page.substring(0, page.lastIndexOf("."));
    var cases = [page];
    if (parent.indexOf(".") > 0)
        cases.push(parent);
    for (var i = 0; i < cases.length; i++) {
        var link = "/" + version + "/" + cases[i];
        if (nodes[link])
            nodes[link].setState({"collapsed": false});
    }

    var link = "/" + version + "/" +page;
    if (nodes[link]) {
        $("#tree").scrollTop($(nodes[link].getDOMNode()).offset().top - $("#tree").offset().top);
    }
    
    $("#tree").scrollTop()
}

var searchResults;
var searchIndex;
function init(version, page, info) {
    $.get("/"+version+"/data/tree.json", function(json) {
        React.render(
            React.createElement(TreeView, {"data": json, "version": version, "page": page}),
            $("#tree").get(0)
        );
        expandInTree(page);
    });

    $.get("/"+version+"/data/search.json", function(json) {
        searchIndex = json;
        React.render(
            React.createElement(SearchView, null),
            $("#search-bar").get(0)
        );
        searchResults = React.render(
            React.createElement(SearchResults, null),
            $("#search-results").get(0)
        );
    });

    initPage(version, page, info);
};

function cleanup(url) {
    return url.replace(/#.*/, "");
};

function searchFor(query) {
    
    var props = {
        "visible": false,
        "version": version,
        "results": []
    };
    if (query.length > 0) {
        props["visible"] = true;
        var filtered = searchIndex.filter(function(item) {
            return item.indexOf(query) >= 0;
        });
        if (filtered.length > 50)
            filtered = filtered.slice(0, 50);
        props["results"] = filtered;
    }
    searchResults.setProps(props);
};

var currentPage = cleanup(location.pathname);    

function loadPage(url) {
    if (searchResults)
        searchResults.setProps({"visible": false});
    
    if (cleanup(url) == currentPage && url.indexOf("#") > 0) return true;
    if (url == currentPage) return false;
    currentPage = cleanup(url);
    history.pushState(null, null, url);
    $("#content").html("Loading...");
    
    $.get(cleanup(url)+"/data", function(res) {
        $("#content").html(res["content"]);
        initPage(res["version"], res["page"], res["info"]);
        expandInTree(res["page"]);
    });
    return false;
};

function loadPageFromLink(e) {
    if (e.ctrlKey || e.metaKey) return true;
    var href = $(e.target).attr("href");
    if (href)
        return loadPage(href);
};

$(window).on('popstate', function(e) {
    var url = document.location.pathname;
    if (cleanup(url) == currentPage)
        return;
    return loadPage(url);
});

$(function() {
    // tree resize
    var resizeTree = function(e) {
        var maxX = $(window).width() - 500;
        var x = Math.min(Math.max(229, e.screenX), maxX);
        $("#sidebar").css("width", x+"px");
        $("#body").css("left", x+"px");
    };
    $("#resizer").mousedown(function() {
        $("#locker").show();
        $("#locker").on("mousemove", resizeTree);
    });
    $("#locker").mouseup(function() {
        $("#locker").hide();
        $("#locker").off("mousemove", resizeTree);
    });

    // versions switch
    $("#version").click(function() {
        $("#versions").toggle();
        return false;
    });

    $("#path-ns").click(loadPageFromLink);
    $("#path-class").click(loadPageFromLink);
});
