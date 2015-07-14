goog.provide("api.versions");
goog.require("api.utils");

/** */
api.versions.init = function() {
    $('.versionselect').on('change', function(){
        location.href = "/" + $(this).find("option:selected").val() + "/try/" + api.utils.getEntryFromUrl(location.pathname);
    });
};
