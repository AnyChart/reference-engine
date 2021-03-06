goog.provide("api.core");

goog.require("api.versions");
goog.require("api.resize");
goog.require("api.page");
goog.require("api.breadcrumb");
goog.require("api.utils");
goog.require("api.pageScrolling");
goog.require("api.search");
goog.require("api.history");

api.core.init = function(version, page, is_url_version) {
    api.config.version = version;
    api.config.is_url_version = is_url_version;
    api.config.page = page;

    api.versions.init();
    api.resize.init();
    api.page.prettifyCode();
    api.page.fixLinks();
    api.page.fixListings();
    api.tree.init();
    api.pageScrolling.update();
    api.search.init();

    api.tree.expand(location.pathname, location.hash);
    api.breadcrumb.update(api.utils.getEntryFromURL(location.pathname));

    api.tree.scrollToEntry(page, location.hash ? location.hash.substr(1) : null);

    if (location.hash)
        api.page.highlightOnLoad(location.hash.substr(1));

    api.page.fixAccordionLinks();
    api.page.fixEnumLinks();
    api.pageScrolling.init();

    if (page == "" || page.indexOf("?entry=") > -1) {
        if (api.config.is_url_version) {
            api.tree.expand("/latest/anychart");
        } else {
            api.tree.expand("/anychart");
        }
        api.tree.unhighight();
    }
};

$(window).load(function() {
    setTimeout(function() {
        api.history.init();
    }, 100);
});
