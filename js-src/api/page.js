goog.provide('api.page');

goog.require('api.links');
goog.require('api.config');
goog.require('api.search');
goog.require('api.utils');
goog.require('api.tree');
goog.require('api.pageScrolling');
goog.require('api.breadcrumb');

/**
 * @param {string} entry
 */
api.page.scrollToEntry = function (entry) {
    $('.method-block').removeClass('active');
    $('#' + entry).addClass('active');
    window.setTimeout(function () {
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
api.page.fixLinks = function () {
    $("#content a.type-link").click(api.links.typeLinkClickWithScroll);
};

/** */
api.page.fixListings = function () {
    prettyPrint();
};

api.page.currentActive_ = null;

api.page.scrollHighlight = function (target) {
    if (target == api.page.currentActive_)
        return;
    api.page.currentActive_ = null;
    $(".content-container .active").removeClass("active");
    $(".content-container .scroll-active").removeClass("scroll-active");

    api.history.setHash(target);

    if ($("#" + target).parent().hasClass("panel-heading"))
        $("#" + target).parent().parent().parent().parent().addClass("scroll-active");
    else
        $("#" + target).parent().addClass("scroll-active");
};

/**
 * @param {string} target
 * @param {boolean=} opt_expand
 * @param {boolean=} opt_scroll
 * @param {boolean=} opt_addHash
 */
api.page.highlight = function (target, opt_expand, opt_scroll, opt_addHash) {
    var expand = opt_expand == undefined ? true : opt_expand;
    var scroll = opt_scroll == undefined ? true : opt_scroll;
    var addHash = opt_addHash == undefined ? false : opt_addHash;
    $(".content-container .active").removeClass("active");
    $(".content-container .scroll-active").removeClass("scroll-active");
    if (expand) {
        var entry = api.utils.getEntryFromURL(location.pathname);
        api.tree.expand(entry, target);
    }
    api.page.currentActive_ = target;

    if (scroll)
        api.pageScrolling.highlightScroll(target);
    else if (addHash)
        api.history.setHash(target);

    if ($("#" + target).parent().hasClass("panel-heading"))
        $("#" + target).parent().parent().parent().parent().addClass("active");
    else
        $("#" + target).parent().addClass("active");
};

api.page.highlightOnLoad = function (target) {
    api.page.currentActive_ = target;
    api.pageScrolling.highlightScroll(target);
    if ($("#" + target).parent().hasClass("panel-heading"))
        $("#" + target).parent().parent().parent().parent().addClass("active");
    else
        $("#" + target).parent().addClass("active");
};

api.page.highlightCategory = function (target) {
    api.pageScrolling.highlightScroll(target);
    api.history.setHash(target);
};


api.page.load = function (target, opt_add, opt_scrollTree) {
    api.search.hide();

    if (opt_add == undefined) opt_add = true;
    if (target.indexOf("#") == 0) target = location.pathname + target;
    var cleanedTarget = api.utils.cleanupPath(target);

    var hash;
    if (target.indexOf("#") != -1 && target.indexOf("#") != target.length - 1)
        hash = target.substr(target.indexOf("#") + 1);

    var prev = (api.config.is_url_version ? "/" + api.config.version : "") + "/" + api.config.page;

    if (cleanedTarget == prev) {
        if (hash && hash.startsWith("category-")) {
            api.page.highlightCategory(hash);
            api.history.setHash(hash);
            return false;
        }

        if (hash) {
            api.page.highlight(hash);
            api.history.setHash(hash);
        }
        api.tree.expand(target, hash ? "#" + hash : undefined);
        api.tree.scrollToEntry(target, hash);
        return false;
    }

    if (cleanedTarget.split("/").length > 2) {
        cleanedTarget = cleanedTarget.split("/")[2];
    }

    if (typeof window.history == "undefined") return true;

    if (opt_add)
        window.history.pushState(null, null, target);

    api.tree.expand(target, hash);

    if (opt_scrollTree)
        api.tree.scrollToEntry(cleanedTarget, hash);

    $(".content-container").html('<div class="loader"><i class="fa fa-spinner fa-spin fa-pulse fa-2x fa-fw"></i> <span> loading ...</span> </div>');

    api.pageScrolling.destroy();

    if (cleanedTarget == "/")
        cleanedTarget = "/" + api.config.version + "/landing";

    $.get(cleanedTarget + "/data", function (res) {
        document.title = res.title;
        $("meta[property='og\\:title']").attr("content", res.title);
        $("meta[property='og\\:url']").attr("content", res.url);
        $("meta[name='keywords']").attr("content", res.keywords);
        $("meta[name='description']").attr("content", res.description);
        $("meta[property='og\\:description']").attr("content", res.description);

        $("#content-wrapper").html('<div id="content"><div class="content-container">' + res.content + '</div></div>');

        api.config.page = res.page;

        api.pageScrolling.update();

        $("#warning a").attr("href", "/" + $("#warning a").attr("data-last-version") + "/try/" + res.page);

        api.breadcrumb.update(res.page);
        api.page.fixLinks();
        api.page.fixListings();
        api.page.fixAccordionLinks();

        if (hash)
            api.page.highlight(hash);
    });

    return false;
};

api.page.fixAccordionLinks = function () {
    $(".method-block").each(function () {
        var id = $(this).find("h3").attr("id");
        var $block = $(this);
        if (id == undefined) {
            id = $(this).find("h4.panel-title").attr("id");
        }

        $(this).find(".panel-title a[data-toggle='collapse']").click(function (e) {

            api.page.scrollToEntry(id);
            api.page.highlight(id, false, false, false);
            api.history.setHash(id);
            api.tree.expand(location.pathname, "#" + id);
            api.tree.scrollToEntry(api.config.page, id);

            if ($block.find(".pannel-collapse.collapse").length > 1) {
                var href = $(this).attr("href");
                var $target = $("div" + href);
                if (!$target.hasClass("in")) {
                    $block.find(".pannel-collapse.collapse.in").collapse('hide');
                    $target.collapse('show');
                }
            }

            return false;
        });
    });
};

/**
 * @param {Object} $results
 */
api.page.showSearchResults = function ($results) {
    api.pageScrolling.destroy();

    $("#content-wrapper").html('<div id="content"><div class="content-container"></div></div>');
    $("#content .content-container").append($results);
    $("#content .content-container a").click(api.links.typeLinkClickWithScroll);
    api.pageScrolling.update();
};
