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

    var $target = $("#tree-menu li[x-data-name='" + sel + "']");
    if ($target.length) {
        window.setTimeout(function() {
            // $("#tree-wrapper").mCustomScrollbar("scrollTo", $target.get(0).offsetTop - 20, {scrollInertia: 700});
            //api.tree.pisces.scrollToElement( $target.get(0));
            api.tree.pisces.scrollToPosition({x: 0, y:  $target.get(0).offsetTop - 20});
        }, 100);
    }
};

/**
 * @param {string} entry
 * @param {string} opt_hash
 * @private
 */
api.tree.expand_ = function(entry, opt_hash) {
    $("#tree-menu .active").removeClass("active");
    $("#tree-menu .scroll-active").removeClass("scroll-active");

    var parts = entry.split(".");

    for (var i = 0; i < parts.length; i++) {
        var path = parts.slice(0, (i + 1)).join(".");
        var $el = $("#tree-menu li[x-data-name='" + path + "']");
        $el.find(">ul").show();
        $el.find(">a i").removeClass("ac-chevron-right").addClass("ac-chevron-down");
        $el.addClass("active");
    }
    if (opt_hash) {
        var $el = $("#tree-menu li.item[x-data-name='" + entry + "#" + opt_hash + "']");
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
        $("#tree-menu .active").removeClass("active");
        $("#tree-menu .scroll-active").removeClass("scroll-active");
        //$("#tree-wrapper").mCustomScrollbar("scrollTo", 0, {scrollInertia: 700});
        api.tree.pisces.scrollToPosition({x: 0, y:  0});
        return;
    }
    var entry;
    if (api.config.is_url_version) {
        entry = path.match("^/[^/]+/(.*)$");
    } else {
        entry = path.match("^/(.*)$");
    }

    var hash;
    if (opt_hash)
        hash = opt_hash.indexOf("#") != -1 ? opt_hash.substr(1) : opt_hash;

    if (entry)
        api.tree.expand_(entry[1], hash);
};

api.tree.unhighight = function() {
    $("#tree-menu .active").removeClass("active");
    $("#tree-menu .scroll-active").removeClass("scroll-active");
};

api.tree.scrollHighlight = function(hash) {
    if (!hash || hash.length == 1) return;
    if (hash == api.tree.currentActive_) return;
    api.tree.currentActive_ = null;
    if (hash.indexOf("#") != -1)
        hash = hash.substr(1);
    var path = api.config.page;
    $("#tree-menu .scroll-active").removeClass("scroll-active");
    var $item = $("#tree-menu li[x-data-name='" + path + "']");
    $item.find(">ul>li.active").removeClass("active");
    var $el = $("#tree-menu li.item[x-data-name='" + path + "#" + hash + "']");
    $el.addClass("scroll-active");
};

/** */
api.tree.disableScrolling = function() {
    //$("#tree-wrapper").mCustomScrollbar("disable", true);
};

/** */
api.tree.updateScrolling = function() {
    //$("#tree-wrapper").mCustomScrollbar("update");
};

/**
 */
api.tree.init = function() {
    //$("#tree-wrapper").mCustomScrollbar(api.config.scrollSettings);
    api.tree.scrollBar = new GeminiScrollbar({
        //element: $("#tree-wrapper")[0]
        element: $("#tree-scr")[0]
    }).create();
    setInterval(function(){
        api.tree.scrollBar.update()
    }, 100);
    api.tree.pisces = new Pisces(api.tree.scrollBar.getViewElement());



    $("#tree-menu li.group").each(function() {
        var $ul = $(this).find(">ul");

        $(this).find(">a i").click(function() {
            if ($ul.is(":visible")) {
                $(this).addClass("ac-chevron-right").removeClass("ac-chevron-down");
                $ul.hide();
            } else {
                $(this).addClass("ac-chevron-down").removeClass("ac-chevron-right");
                $ul.show();
            }
            return false;
        });

        $(this).find(">a").click(api.links.treeLinkClick);
    });

    $("#tree-menu li.item a").click(api.links.treeLinkClick);
};
