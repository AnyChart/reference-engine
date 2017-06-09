goog.provide('api.utils');

/**
 * @param {string} target
 * @return {string}
 */
api.utils.cleanupPath = function(target) {
    if (target.indexOf("http") != -1)
        target = target.substr(target.indexOf("/", target.indexOf("//") + 2));
    if (target.indexOf("#") != -1)
        target = target.substr(0, target.indexOf("#"));
    return target;
};

/** 
 * @param {string} path
 * @return {string}
 */
api.utils.getEntryFromURL = function(path) {
    path = api.utils.cleanupPath(path);
    if (path == "/") return "";
    if (api.config.is_url_version){
        return path.match("^/[^/]+/(.*)$")[1];
    }else {
        return path.match("^/(.*)$")[1]
    }
};
