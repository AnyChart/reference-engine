goog.provide("api.tree");

goog.require("api.config");
goog.require("api.utils");

/**
 * @param {string} entry
 * @param {string=} opt_hash
 */
api.tree.scrollToEntry = function(entry, opt_hash) {
    var sel = entry + (opt_hash ? ("#" + opt_hash) : "");
    
    var $target = $("#tree li[x-data-name='" + sel + "']");
    window.setTimeout(function(){
        $("#tree-wrapper").mCustomScrollbar("scrollTo", $target.offset().top - 120, {scrollInertia: 700});
    }, 200);
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
};

/** 
 * @param {string} path
 */
api.tree.expand = function(path) {
    path = api.utils.cleanupPath(path);
    var entry = path.match("^/[^/]+/(.*)$");
    if (entry)
        api.tree.expand_(entry[1]);
};

/**
 */
api.tree.init = function() {
    $("#tree-wrapper").mCustomScrollbar(api.config.scrollSettings);

    $("#tree li.group").each(function() {
        var $ul = $(this).find(">ul");
        $(this).find(">a").click(function(e) {
            if (e.ctrlKey || e.metaKey) return true;
            $ul.toggle();
            if ($ul.is(":visible"))
                $(this).find("i").addClass("fa-chevron-down").removeClass("fa-chevron-right");
            else
                $(this).find("i").addClass("fa-chevron-right").removeClass("fa-chevron-down");
            return api.page.load($(this).attr("href"));
        });
    });
    
    $("#tree li.item a").click(function(e) {
        if (e.ctrlKey || e.metaKey) return true;
        return api.page.load($(this).attr("href"));
    });
};
