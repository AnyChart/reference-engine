goog.provide("api.tree");

goog.require("api.config");
goog.require("api.utils");

api.tree.currentActive_ = null;

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
    $("#tree .scroll-active").removeClass("scroll-active");
    
    var parts = entry.split(".");
    
    for (var i = 0; i < parts.length; i++) {
        var path = parts.slice(0, (i+1)).join(".");
        var $el = $("#tree li[x-data-name='" + path + "']");
        $el.find(">ul").show();
        $el.find(">a i").removeClass("ac-chevron-right").addClass("ac-chevron-down");
        $el.addClass("active");
    }
    if (opt_hash) {
        var $el = $("#tree li.item[x-data-name='" + entry + "#" + opt_hash + "']");
        $el.addClass("active");
        api.tree.currentActive_ = opt_hash;
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
        $("#tree .scroll-active").removeClass("scroll-active");
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

api.tree.unhighight = function() {
    $("#tree .active").removeClass("active");
    $("#tree .scroll-active").removeClass("scroll-active");
};

api.tree.scrollHighlight = function(hash) {
    if (!hash || hash.length == 1) return;
    if (hash == api.tree.currentActive_) return;
    api.tree.currentActive_ = null;
    if (hash.indexOf("#") != -1)
        hash = hash.substr(1);
    var path = api.config.page;
    $("#tree .scroll-active").removeClass("scroll-active");
    var $item = $("#tree li[x-data-name='" + path + "']");
    $item.find(">ul>li.active").removeClass("active");
    var $el = $("#tree li.item[x-data-name='" + path + "#" + hash + "']");
    $el.addClass("scroll-active");
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
        
        $(this).find(">a i").click(function() {
            if ($ul.is(":visible")) {
                $(this).addClass("ac-chevron-right").removeClass("ac-chevron-down");
                $ul.hide();
            }else {
                $(this).addClass("ac-chevron-down").removeClass("ac-chevron-right");
                $ul.show();
            }
            return false;
        });
        
        $(this).find(">a").click(function(e) {
            if (e.ctrlKey || e.metaKey) return true;
            return api.page.load($(this).attr("href"));
        });
    });
    
    $("#tree li.item a").click(function(e) {
        if (e.ctrlKey || e.metaKey) return true;
        return api.page.load($(this).attr("href"));
    });
};
