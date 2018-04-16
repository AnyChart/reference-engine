goog.provide("api.versions");
goog.require("api.utils");


api.versions.init = function() {
    $('.versionselect').on('change', function() {
        var newVersion = $(this).find("option:selected").val();
        var entry = api.utils.getEntryFromURL(location.pathname);
        if (location.search.length) {
            // for ?entry=searchString
            location.href = "/" + newVersion + "/" + location.search;
        } else if (entry.length) {
            location.href = "/" + newVersion + "/try/" + entry;
        } else {
            location.href = "/" + newVersion + "/anychart";
        }
    });
};
