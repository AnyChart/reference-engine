goog.provide("api.core");

goog.require("api.versions");
goog.require("api.resize");
goog.require("api.page");
goog.require("api.breadcrumb");
goog.require("api.utils");
goog.require("api.pageScrolling");
goog.require("api.search");
goog.require("api.history");

api.core.init = function(version, page) {
    api.config.version = version;
    api.config.page = page;
    
    api.versions.init();
    api.resize.init();
    api.page.fixLinks();
    api.page.fixListings();
    api.tree.init();
    //api.pageScrolling.update();
    api.search.init();

    api.tree.expand(location.pathname, location.hash);
    api.breadcrumb.update(api.utils.getEntryFromURL(location.pathname));

    api.tree.scrollToEntry(page, location.hash ? location.hash.substr(1) : null);

    if (location.hash)
        api.page.highlightOnLoad(location.hash.substr(1));

    api.page.fixAccordionLinks();
    //api.pageScrolling.init();

    if (page == "" || page.indexOf("?entry=") > -1) {
        api.tree.expand("/latest/anychart");
        api.tree.unhighight();
    }
};

$(window).load(function() {
    setTimeout(function() {
        api.history.init();
    }, 100);
});
