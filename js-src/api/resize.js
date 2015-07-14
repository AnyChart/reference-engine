goog.provide('api.resize');

/**
 * @private
 * @param {Event} e
 */
api.resize.stopResize_ = function(e) {
    $("body").off("mouseup", api.resize.stopResize_);
    $("body").off("mousemove", api.resize.doResize_);
};

/**
 * @private
 * @param {Event} e
 */
api.resize.doResize_ = function(e) {
    if (e.pageX > 250 &&  e.pageX < window.innerWidth - 300) {
        $('#menu-bar').css('width', e.pageX);
        $('#content-wrapper')
            .css('margin-left', e.pageX)
            .css('width', window.innerWidth - e.pageX);
        $('.breadcrumb').css('left', $('#menu-bar').width());
    }
    return false;
};

/** 
 * @private
 * @param {Event} e
 */
api.resize.startResize_ = function(e) {
    $("body").on("mouseup", api.resize.stopResize_);
    $("body").on("mousemove", api.resize.doResize_);
    
    return false;
};

api.resize.init = function() {
    $("#size-controller").on("mousedown", api.resize.startResize_);
};
