goog.provide("api.config");

api.config.version = null;
api.config.page = null;

api.config.scrollSettings = (function() {
    var scrollAmount = 80;
    var scrollKeyAmount = 100;
    /*if (navigator.platform.match(/(Mac|iPhone|iPod|iPad)/i)) {
        scrollAmount = 2;
        scrollKeyAmount = 15;
    }*/
    return {
        scrollInertia: 0,
        theme: "minimal-dark",
        mouseWheel: {
            enable: true,
            scrollAmount: scrollAmount
        },
        keyboard: {
            enable: true,
            scrollAmount: scrollKeyAmount,
            scrollType: 'stepless'
        }};
})();
