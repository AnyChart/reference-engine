goog.provide("api.tree");

goog.require("api.config");
goog.require("api.utils");

/**
 * @param {string} entry
 * @param {string=} opt_hash
 */
api.tree.scrollToEntry = function(entry, opt_hash) {
    var sel = entry;
    if (opt_hash)
        sel += opt_hash.indexOf("#") == -1 ? ("#" + opt_hash) : opt_hash;

    var $target = $("#tree li[x-data-name='" + sel + "']");
    if ($target.length) {
        window.setTimeout(function(){
            $("#tree-wrapper").mCustomScrollbar("scrollTo", $target.get(0).offsetTop - 20, {scrollInertia: 700});
        }, 200);
    }
};

/**
 * @param {string} entry
 * @param {string} opt_hash
 * @private
 */
api.tree.expand_ = function(entry, opt_hash) {
    $("#tree .active").removeClass("active");
    
    var parts = entry.split(".");
    
    for (var i = 0; i < parts.length; i++) {
        var path = parts.slice(0, (i+1)).join(".");
        var $el = $("#tree li[x-data-name='" + path + "']");
        $el.find(">ul").show();
        $el.find(">a i").removeClass("fa-chevron-right").addClass("fa-chevron-down");
        $el.addClass("active");
    }
    if (opt_hash) {
        var $el = $("#tree li.item[x-data-name='" + entry + "#" + opt_hash + "']");
        $el.addClass("active");
    }

    api.tree.updateScrolling();
};

/** 
 * @param {string} path
 * @param {string=} opt_hash
 */
api.tree.expand = function(path, opt_hash) {
    path = api.utils.cleanupPath(path);
    if (path == "/") {
        $("#tree .active").removeClass("active");
        $("#tree-wrapper").mCustomScrollbar("scrollTo", 0, {scrollInertia: 700});
        return;
    }
    var entry = path.match("^/[^/]+/(.*)$");

    var hash;
    if (opt_hash)
        hash = opt_hash.indexOf("#") != -1 ? opt_hash.substr(1) : opt_hash;
    
    if (entry)
        api.tree.expand_(entry[1], hash);
};

/** */
api.tree.disableScrolling = function() {
    $("#tree-wrapper").mCustomScrollbar("disable", true);
};

/** */
api.tree.updateScrolling = function() {
    $("#tree-wrapper").mCustomScrollbar("update");
};

/**
 */
api.tree.init = function() {
    $("#tree-wrapper").mCustomScrollbar(api.config.scrollSettings);

    $("#tree li.group").each(function() {
        var $ul = $(this).find(">ul");
        $(this).find(">a").click(function(e) {
            if (e.ctrlKey || e.metaKey) return true;
            if ($ul.is(":visible")) {
                $(this).find("i").addClass("fa-chevron-right").removeClass("fa-chevron-down");
                $ul.hide();
            }else {
                $ul.show();
                return api.page.load($(this).attr("href"));
            }
            return false;
        });
    });
    
    $("#tree li.item a").click(function(e) {
        if (e.ctrlKey || e.metaKey) return true;
        return api.page.load($(this).attr("href"));
    });
};
