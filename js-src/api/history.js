goog.provide('api.history');

api.history.setHash = function(hash) {
    if (hash == '') return;
    if (window.history)
        window.history.replaceState(null, null, location.pathname + "#" + hash);
    else
        location.hash = "#" + hash;
};

api.history.setHashSearch = function(search) {
    if (window.history)
        window.history.replaceState(null, null, (api.config.is_url_version ? "/" + api.config.version : "") + "/?entry=" + search);
};

api.history.init = function() {
    window.onpopstate = function(e) {
        if (location.href.indexOf("?entry=") > -1) {
            api.search.setSearchPage();
        } else {
            api.page.load(location.href, false, true);
        }
    };
};
