goog.provide("api.versions");
goog.require("api.utils");

/** */
api.versions.init = function() {
    $('.versionselect').on('change', function(){
        var entry = api.utils.getEntryFromURL(location.pathname);
        if (!entry.length)
            location.href = "/" + $(this).find("option:selected").val() + "/anychart";
        else
            location.href = "/" + $(this).find("option:selected").val() + "/try/" + api.utils.getEntryFromURL(location.pathname);
    });
};
