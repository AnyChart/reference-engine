goog.provide("api.core");

goog.require("api.versions");
goog.require("api.resize");
goog.require("api.page");
goog.require("api.breadcrumb");
goog.require("api.utils");
goog.require("api.pageScrolling");
goog.require("api.search");

$(function() {
    api.versions.init();
    api.resize.init();
    api.page.fixLinks();
    api.page.fixListings();
    api.tree.init();
    api.pageScrolling.update();
    api.search.init();

    api.tree.expand(location.pathname);
    api.breadcrumb.update(api.utils.getEntryFromURL(location.pathname));

    api.tree.scrollToEntry(page, location.hash ? location.hash.substr(1) : null);

    if (location.hash)
        api.pageScrolling.scrollTo(location.hash);
});;
