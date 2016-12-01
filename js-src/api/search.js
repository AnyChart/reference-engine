goog.provide("api.search");

goog.require("api.config");


/**
 * @private
 * @type {Object}
 */
api.search.data_ = null;
api.search.firstResult_ = null;

api.search.setFirstResult = function(entry){
    if (api.search.firstResult_ == null){
        api.search.firstResult_ = "/" + api.config.version + "/" + entry.link;
    }
};

api.search.setFirstResultByGroup = function(group){
    if (api.search.firstResult_ == null){
        group.sort(api.search.sortGroup_);
        api.search.setFirstResult(group[0]);
    }
};

/**
 *
 */
api.search.updateMaxHeight = function() {
    $('#search-results-new').css('max-height', window.innerHeight - 145);
};

/** */
api.search.hide = function() {
    $("#search-results-new").hide();
};

api.search.notBaseContain = function(name, fullName) {
    var names = fullName.split(".");
    if (name.charAt(0) == name.toLowerCase().charAt(0)) {
        names.pop();
    }
    return names.join(".").indexOf("Base") == -1;
};

/**
 * @private
 * @param {string} str
 * @param {string} target
 */
api.search.match_ = function(name, fullName, target) {
    if (target.indexOf(".") == -1)
        return name.toLowerCase().indexOf(target.toLowerCase()) != -1 && api.search.notBaseContain(name, fullName);
    return fullName.toLowerCase().indexOf(target.toLowerCase()) != -1 && api.search.notBaseContain(name, fullName);
};

/**
 * @private
 * @param {string} str
 * @return {Array}
 */
api.search.findConstants_ = function(str) {
    var res = [];
    var data = api.search.data_;
    for (var i = 0; i < data.namespaces.length; i++) {
        var ns = data.namespaces[i];
        for (var j = 0; j < ns.constants.length; j++) {
            var c = ns.constants[j];
            if (api.search.match_(c.name, c["full-name"], str)) {
                res.push(c);
            }
        }
    }
    return res;
};

/**
 * @private
 * @param {Array} res
 * @return {Array}
 */
api.search.setMultiple_ = function(res){
    var actual = [];
    for (var i = 0; i < res.length; i++) {
        var item = res[i];
        var multiple = false;
        for (var j = 0; j < actual.length; j++) {
            if (actual[j].name == item.name) {
                multiple = true;
                actual[j].multiple = true;
                if (!actual[j].group)
                    actual[j].group = [actual[j], item];
                else
                    actual[j].group.push(item);
            }
        }
        if (!multiple)
            actual.push($.extend({}, item));
    }
    return actual;
};

/**
 * @private
 * @param {string} str
 * @param {string} container
 * @param {string} key
 * @return {Array}
 */
api.search.findGrouped_ = function(str, container, key) {
    var data = api.search.data_;
    var contains = api.search.match_;
    var res = [];
    for (var i = 0; i < data[container].length; i++) {
        var c = data[container][i];
        for (var j = 0; j < c[key].length; j++) {
            var m = c[key][j];
            if (contains(m.name, m["full-name"], str)) {
                res.push(m);
            }
        }
    }
    return api.search.setMultiple_(res);
};

/**
 * @private
 * @param {string} str
 * @param {Array} entries
 * @param {string} type
 * @return {Array}
 */
api.search.filter_ = function(str, entries, type) {
    var res = [];
    for (var i = 0; i < entries.length; i++) {
        var entry = entries[i];
        if (api.search.match_(entry["name"], entry["full-name"], str)) {
            res.push(entry);
        }
    }
    return res;
};

/**
 * @private
 * @param {string} str
 * @return {Array}
 */
api.search.findMethods_ = function(str) {
    return api.search.findGrouped_(str, "classes", "methods");
};

/**
 * @private
 * @param {string} str
 * @return {Array}
 */
api.search.findFunctions_ = function(str) {
    return api.search.findGrouped_(str, "namespaces", "functions");
};

/**
 * @private
 * @param {string} str
 * @return {Array}
 */
api.search.findNamespaces_ = function(str) {
    return api.search.filter_(str, api.search.data_.namespaces, "namespace");
};

/**
 * @private
 * @param {string} str
 * @return {Array}
 */
api.search.findClasses_ = function(str) {
    return api.search.filter_(str, api.search.data_.classes, "class");
};

/**
 * @private
 * @param {string} str
 * @return {Array}
 */
api.search.findEnums_ = function(str) {
    return api.search.filter_(str, api.search.data_.enums, "enum");
};

/**
 * @private
 * @param {string} str
 * @return {Array}
 */
api.search.findTypedefs_ = function(str) {
    return api.search.filter_(str, api.search.data_.typedefs, "typedef");
};

api.search.sortGroup_ = function(a, b){
    if(a["full-name"] < b["full-name"]) return -1;
    if(a["full-name"] > b["full-name"]) return 1;
    return 0;
};

/**
 * @private
 * @param {Object} item
 * @param {string} prefix
 * @param {string} postfix
 */
api.search.showGrouped_ = function(item, prefix, postfix) {
    api.history.setHashSearch(item.name);
    api.breadcrumb.showSearch(item.name);

    $("#search-results-new").hide();
    var $res = $("<ul></ul>");
    item.group.sort(api.search.sortGroup_);
    for (var i = 0; i < item.group.length; i++) {
        var entry = item.group[i];
        $res.append("<li><a class='item-link' href='/" + api.config.version + "/" + entry.link + "'>" + prefix + entry["full-name"] + postfix + "</a></li>");
    }

    api.config.page = null;

    api.page.showSearchResults($res);
};

/**
 * @private
 * @param {Object} $res
 * @param {Array} items
 * @param {string} prefix
 * @param {string} postfix
 * @param {string} title
 */
api.search.addToResults_ = function($res, items, prefix, postfix, title) {
    if (!items.length) return;
    $res.append("<li class='group-name'>" + title +"</li>");

    for (var i = 0; i < items.length; i++) {
        var item = items[i];
        if (item.multiple) {
            api.search.setFirstResultByGroup(item.group);
            (function(item) {
                var $link = $("<li><a class='group-link' href='#'>" + prefix + item.name + postfix + " <span>"+ item.group.length +" matches</span></a></li>");
                $link.find("a").click(function() {
                    api.search.showGrouped_(item, prefix, postfix);
                    return false;
                });
                $res.append($link);
            })(item);
        }
    }
    for (var i = 0; i < items.length; i++) {
        var item = items[i];
        if (!item.multiple){
            api.search.setFirstResult(item);
            $res.append("<li><a class='item-link' href='/" + api.config.version + "/" + item.link + "'>" + prefix + item["full-name"] + postfix + "</a></li>");
        }
    }
};

/**
 * @private
 * @param {Object} $res
 */
api.search.showEmpty_ = function($res) {
    $res.append("<li>No results found</li>");
};

/**
 * @private
 * @param {string} query
 */
api.search.search_ = function(query) {
    api.search.firstResult_ = null;
    if (query.length < 2) {
        api.search.hide();
        return;
    }

    var $res = $("<ul></ul>");
    api.search.addToResults_($res, api.search.findConstants_(query), "", "", "Constants");
    api.search.addToResults_($res, api.search.findFunctions_(query), "", "()", "Functions");
    api.search.addToResults_($res, api.search.findMethods_(query), "", "()", "Methods");
    api.search.addToResults_($res, api.search.findNamespaces_(query), "", "", "Namespaces");
    api.search.addToResults_($res, api.search.findEnums_(query), "[", "]", "Enums");
    api.search.addToResults_($res, api.search.findTypedefs_(query), "{", "}", "Typedefs");
    api.search.addToResults_($res, api.search.findClasses_(query), "", "", "Classes");
    $res.find("a.item-link").click(api.links.typeLinkClick);

    if (!$res.find("a").length) api.search.showEmpty_($res);

    $("#search-results-new").show();
    $("#search-results-new").html("");
    $("#search-results-new").append($res);
};

/**
 * @private
 * @param {Array} data
 * @param {string} query
 */
api.search.onDataLoad_ = function(data, query){
    api.search.data_ = data;
    var constants = [];
    var functions = [];
    var methods = [];
    var namespaces = [];
    var enums = [];
    var classes = [];
    var typedefs = [];
    for(var i = 0; i < data.length; i++){
        var item = data[i];
        switch(item.type){
            case "method" : methods.push(item); break;
            case "function" : functions.push(item); break;
            case "class" : classes.push(item); break;
            case "typedef": typedefs.push(item); break;
            case "enum": enums.push(item); break;
            case "namespace": namespaces.push(item); break;
            case "constant": constants.push(item); break;
        }
    }
    methods = api.search.setMultiple_(methods);
    functions = api.search.setMultiple_(functions);

    var $res = $("<ul></ul>");
    api.search.addToResults_($res, constants, "", "", "Constants");
    api.search.addToResults_($res, functions, "", "()", "Functions");
    api.search.addToResults_($res, methods, "", "()", "Methods");
    api.search.addToResults_($res, namespaces, "", "", "Namespaces");
    api.search.addToResults_($res, enums, "[", "]", "Enums");
    api.search.addToResults_($res, typedefs, "{", "}", "Typedefs");
    api.search.addToResults_($res, classes, "", "", "Classes");
    $res.find("a.item-link").click(api.links.typeLinkClick);

    if (!$res.find("a").length) api.search.showEmpty_($res);

    $("#search-results-new").show();
    $("#search-results-new").html("");
    $("#search-results-new").append($res);
};

/**
 * @private
 * @param {string} query
 */
api.search.makeSearchReq = function(query){
    if (query.length < 2) {
        api.search.hide();
        return;
    }
    $.get("/" + api.config.version + "/search.json?q=" + query, function(data){
        api.search.onDataLoad_(data, query);
    });
};

api.search.init_ = function() {
    $("#search-results-new").click(function(e) {
        e.stopPropagation();
    });
    $("html").click(api.search.hide);

    $("#search").click(function() {
        return false;
    });
    $("#search").focus(function() {
        if( $(this).val() != ''){
            $("#search-results-new").show();
        }
    });

    var timeout = null;
    var query = "";
    $("#search").keyup(function(e) {
        if (e.keyCode == 27) { //esc
            api.search.hide();
            $("#search").val('');
        }else {
            var newQuery = $("#search").val();
            if (newQuery != query) {
                query = newQuery;
                if (timeout !== null) {
                    window.clearTimeout(timeout);
                }
                timeout = window.setTimeout(function(){
                    api.search.makeSearchReq(query);
                }, 250);
            }
        }
    });
};

/**
 * ========================== Old searching ===============================
 */

/**
 * @private
 * @param {Object} data
 */
api.search.onLoad_ = function(data) {
    api.search.data_ = data;

    $("#search-results-new").click(function(e) {
        e.stopPropagation();
    });
    $("html").click(api.search.hide);

    $("#search").click(function() {
        return false;
    });
    $("#search").focus(function() {
        api.search.search_($(this).val());
    });
    $("#search").keydown(function(e) {
        if(e.keyCode == 13){
            e.preventDefault();
            window.location.href = api.search.firstResult_;
        }
    });
    $("#search").keyup(function(e) {
        if (e.keyCode == 27) { //esc
            api.search.hide();
            $("#search").val('');
        }else {
            api.search.search_($(this).val());
        }
    });
    api.search.setSearchPage();
};

api.search.setSearchPage = function(){
    var data = api.search.data_;
    var searchArr = /entry=([^&]+)/.exec(window.location.href);
    if (searchArr && searchArr.length >= 2) {
        var searchString = searchArr[1];
        $("#search").val(searchString);
        api.search.search_(searchString);

        var container = "classes";
        var key = "methods";
        var res = [];
        for (var i = 0; i < data[container].length; i++) {
            var c = data[container][i];
            for (var j = 0; j < c[key].length; j++) {
                var m = c[key][j];
                if (m.name == searchString && api.search.notBaseContain(m.name, m["full-name"])) {
                    res.push(m);
                }
            }
        }
        api.search.showGrouped_({name: searchString, group: res}, "", "()");
    }
};

api.search.init = function() {
    $.get("/" + api.config.version + "/data/search.json", api.search.onLoad_);
};

