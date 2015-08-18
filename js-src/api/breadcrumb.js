goog.provide("api.breadcrumb");
goog.require("api.utils");
goog.require("api.links");

/** 
 * @param {string} path
 */
api.breadcrumb.update = function(path) {
    path = api.utils.cleanupPath(path);
    $("ol.breadcrumb").html('');
    var parts = path.split(".");
    for (var i = 0; i < parts.length; i++) {
        var $el;
        if (i < parts.length - 1) {
            var url = parts.slice(0,i+1).join(".");
            $el = $("<li><a href='/" + api.config.version + "/"+url+"'>" + parts[i] + "</a></li>");
            $el.find("a").click(api.links.typeLinkClickWithScroll);
        }else {
            $el = $("<li class='active'>"+parts[i]+"</li>");
        }
        $("ol.breadcrumb").append($el);
    }
};

/**
 * @param {string} query
 */
api.breadcrumb.showSearch = function(query) {
    $("ol.breadcrumb li").remove();
    $("ol.breadcrumb").append("<li class='active'>Search results for " + query + "</li>");
};
