goog.provide("api.links");


api.links.typeLinkClick = function(e) {
    if (e.ctrlKey || e.metaKey) return true;
    return api.page.load($(this).attr("href"), true, true);
};

api.links.treeLinkClick = function(e) {
    if (e.ctrlKey || e.metaKey) return true;
    return api.page.load($(this).attr("href"), true, false);
};