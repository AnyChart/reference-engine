goog.provide('api.page');

goog.require('api.links');
goog.require('api.config');

/**
 * @param {string} entry
 */
api.page.scrollToEntry = function(entry) {
    $('.method-block').removeClass('active');
    $('#' + entry).addClass('active');
    window.setTimeout(function(){
        $("#content").css('min-height', $("#content").height());
        $("#content-wrapper").mCustomScrollbar('scrollTo', $('#' + entry), {scrollInertia: 700});
        $('.panel').on('hide.bs.collapse', function (e) {
            var $methodBlock = $(this).parent().parent();
            if ($methodBlock.hasClass('selected')) {
                e.preventDefault();
                $methodBlock.removeClass('selected');
            }
        });
    }, 200);
};

/** */
api.page.fixLinks = function() {
    $("#content a.type-link").click(api.links.typeLinkClickWithScroll);
};

/** */
api.page.fixListings = function() {
    prettyPrint();
};

/**
 * @param {string} target
 * @param {boolean=} opt_expand
 * @param {boolean=} opt_scroll
 */
api.page.highlight = function(target, opt_expand, opt_scroll) {
    var expand = opt_expand == undefined ? true : opt_expand;
    var scroll = opt_scroll == undefined ? true : opt_scroll;
    $(".content-container .active").removeClass("active");
    if (expand) {
        var entry = getEntryFromUrl(location.pathname);
        doExpandInTree(entry, target);
    }
    if (scroll) {
        setTimeout(function() {
            $("#content-wrapper").mCustomScrollbar("scrollTo", $("#" + target), {scrollInertia: 700});
        }, 100);
    }
    $("#" + target).parent().addClass("active");
    location.hash = target;
};


api.page.load = function(target, opt_add, opt_scrollTree) {
};
