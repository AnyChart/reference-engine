goog.provide('api.resize');
goog.require('api.search');

/**
 * @private
 * @type {Number}
 */
api.resize.hiddenMenuSize_ = 30;

/**
 * @private
 * @type {Number}
 */
api.resize.visibleMenuSize_ = 300;

/**
 * @private
 * @type {boolean}
 */
api.resize.sidebarVisible_ = true;

/**
 * @private
 * @param {Event} e
 */
api.resize.stopResize_ = function(e) {
    $("body").off("mouseup", api.resize.stopResize_);
    $("body").off("mousemove", api.resize.doResize_);
};

/**
 * @private
 * @param {Event} e
 */
api.resize.doResize_ = function(e) {
    if (e.pageX > 250 && e.pageX < window.innerWidth - 300) {
        $('#menu-bar').css('width', e.pageX);
        $('#content-wrapper')
            .css('padding-left', e.pageX)
        //.css('width', window.innerWidth - e.pageX)
        ;
        $('.breadcrumb').css('left', $('#menu-bar').width());
    }
    return false;
};

/**
 * @private
 * @param {Event} e
 */
api.resize.startResize_ = function(e) {
    $("body").on("mouseup", api.resize.stopResize_);
    $("body").on("mousemove", api.resize.doResize_);

    return false;
};

/**
 * @private
 * @param {boolean} animate
 */
api.resize.hideSidebar_ = function(animate) {
    api.resize.sidebarVisible_ = false;

    var w = api.resize.hiddenMenuSize_;
    if (animate) {
        $('#menu-bar').animate({width: w}, 300);
        //$('#content-wrapper').animate({marginLeft: w, width: window.innerWidth - w}, 300);
        $('#content-wrapper').animate({paddingLeft: w}, 300);
        $('#search-form').animate({opacity: 0}, 200);
        $('#tree-wrapper').animate({opacity: 0}, 200);
        $('.breadcrumb').animate({left: w}, 300);
        $('#footer').animate({opacity: 0}, 200);
    } else {
        $('#menu-bar').css('width', w);
        $('#content-wrapper')
        //.css('width', window.innerWidth - w)
        //.css('margin-left', w);
            .css('padding-left', w);
        $('#search-form').css('opacity', 0);
        $('#tree-wrapper').css('opacity', 0);
        $('#footer').css('opacity', 0);
        $('.breadcrumb').css('left', w);
    }
    $('a.switcher .ac').attr('class', 'ac ac-chevron-right-thick');
    $('#size-controller').css('cursor', 'default');
    api.tree.disableScrolling();
};

/**
 * @private
 * @param {booelan} animate
 */
api.resize.showSidebar_ = function(animate) {
    api.resize.sidebarVisible_ = true;

    var w = api.resize.visibleMenuSize_;

    if (animate) {
        $('#menu-bar').animate({width: w}, w);
        //$('#content-wrapper').animate({marginLeft: w, width: window.innerWidth - w}, w);
        $('#content-wrapper').animate({paddingLeft: w}, w);
        $('.breadcrumb').animate({left: w}, w);
    } else {
        $('#menu-bar').css('width', w);
        $('#content-wrapper')
        //.css('width', window.innerWidth - w)
            .css('padding-left', w);
        $('.breadcrumb').css('left', w);
    }
    $('#search-form').css('opacity', 1);
    $('#tree-wrapper').css('opacity', 1);
    $('#footer').css('opacity', 1);
    $('a.switcher .ac').attr('class', 'ac ac-chevron-left-thick');
    $('#size-controller').css('cursor', 'col-resize');

    api.tree.updateScrolling();
};

/**
 * @private
 */
api.resize.toggleSidebar_ = function() {
    if (api.resize.sidebarVisible_)
        api.resize.hideSidebar_(true);
    else
        api.resize.showSidebar_(true);
};

/** @private */
api.resize.contentSize_ = function() {
    if (window.innerWidth < 992) {
        api.resize.hideSidebar_(false);
    } else {
        api.resize.showSidebar_(false);
        $('#content-wrapper')
        //.css('width', window.innerWidth - 300)
            .css('padding-left', 300);
        $('.breadcrumb').css('left', $('#menu-bar').width());
    }
    api.search.updateMaxHeight();
};

api.resize.init = function() {
    $("#size-controller").on("mousedown", api.resize.startResize_);
    $("a.switcher").click(api.resize.toggleSidebar_);
    $(window).resize(api.resize.contentSize_);
    $(window).load(api.resize.contentSize_);
};
