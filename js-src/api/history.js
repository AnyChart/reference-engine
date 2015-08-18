goog.provide('api.history');

api.history.init = function() {
    var ua = navigator.userAgent.toLowerCase();
    var needIgnoreFirst = ua.indexOf("safari") != -1 && ua.indexOf("chrome") == -1;
    var firstIgnored = false;

    window.onpopstate = function(e) {
        if (needIgnoreFirst && !firstIgnored) {
            firstIgnored = true;
            return;
        }
        api.page.load(location.href, false);
    };
};
