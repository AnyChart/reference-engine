goog.provide("api.links");

api.links.typeLinkClickWithScroll = function(e) {
    if (e.ctrlKey || e.metaKey) return true;
    return api.page.load($(this).attr("href"), true, true);
};

api.links.typeLinkClick = function(e) {
    if (e.ctrlKey || e.metaKey) return true;
    return api.page.load($(this).attr("href"), true, true);
};
