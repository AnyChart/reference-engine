goog.provide('api.pageScrolling');
goog.require('api.tree');

/** 
 * @private
 * @type {boolean} 
 */
api.pageScrolling.isTopVisible_ = false;

/**
 * @private
 * @type {Array}
 */
api.pageScrolling.currentVisible_ = null;

/** 
 * @private
 * @param {number} top
 */
api.pageScrolling.checkTopVisible_ = function(top) {
    if (top < 0) {
        if (!api.pageScrolling.isTopVisible_) {
            $('#top-page-content').fadeIn();
            api.pageScrolling.isTopVisible_ = true;
        }
    }else {
        if (api.pageScrolling.isTopVisible_) {
            $('#top-page-content').fadeOut();
            api.pageScrolling.isTopVisible_ = false;
        }
    }
};

/** 
 * @private
 * @param {number} top
 * @return {string}
 */
api.pageScrolling.getFirstVisible_ = function(top) {
    var minDistance = Number.MAX_VALUE;
    var $minEl = null;
    var d = $("div.content-block.methods h3").each(function() {
        var $el = $(this);
        var distance = Math.abs($el.offset().top - 100);
        if (distance < minDistance) {
            minDistance = distance;
            $minEl = $el;
        }
    });
    if ($minEl) {
        var $first = $("div.content-block.methods h3").first();
        if ($first.position().top == $minEl.position().top) {
            if (- top + $("#content-wrapper").height() - 40 < $first.position().top)
                return null;
        }
    }
    return $minEl ? $minEl.attr("id") : null;
};

/**
 * @private
 */
api.pageScrolling.onContentScroll_ = function() {
    api.pageScrolling.checkTopVisible_(this.mcs.top);
    var el = api.pageScrolling.getFirstVisible_(this.mcs.top);
    if (el) {
        if (el != api.pageScrolling.currentVisible_) {
            api.pageScrolling.currentVisible_ = el;
            var link = "/" + version + "/" + page + "#" + el;
            api.tree.expand(location.pathname, "#" + el);
            api.page.highlight(el, false, false);
        }
    }else {
        api.pageScrolling.currentVisible_ = null;
        api.tree.expand(location.pathname);
        location.hash = "";
    }
};

/** */
api.pageScrolling.update = function() {
    $('#top-page-content').hide();
    api.pageScrolling.isTopVisible_ = false;
    api.pageScrolling.currentVisible_ = null;

    $("#content-wrapper").mCustomScrollbar(
        $.extend(api.config.scrollSettings,
                 { callbacks: {
                     onScroll: api.pageScrolling.onContentScroll_ }}));
};

/** 
 * @param {string} entry
 */
api.pageScrolling.scrollTo = function(entry) {
    $("#content-wrapper").mCustomScrollbar("scrollTo", $(entry));
};

/** 
 * @param {string} entry
 */
api.pageScrolling.highlightScroll = function(entry) {
    setTimeout(function() {
        $("#content-wrapper").mCustomScrollbar("scrollTo", $("#" + entry),
                                               {scrollInertia: 700});
        location.hash = entry.substr(entry.indexOf("#") + 1);
    }, 100);
};

/**
 */
api.pageScrolling.destroy = function() {
    $("#content-wrapper").mCustomScrollbar('destroy');
};
