goog.provide("api.search");
goog.require("api.config");


/**
 * @private
 * @type {Object}
 */
api.search.data_ = null;
api.search.searchIndex_ = 0;
api.search.scrollOfftop_ = 0;

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


var groupBy = function(xs, key) {
    return xs.reduce(function(rv, x) {
        (rv[x[key]] = rv[x[key]] || []).push(x);
        return rv;
    }, {});
};
api.search.groupByNamespace = function(items){
    var res = [];
    items.forEach(function(item, i, arr) {
        var parts = item["full-name"].split('.');
        parts.pop();
        var cls = parts.pop() + "." + item.name;
        var namespace = parts.join(".");
        res.push({name: item.name, "full-name": item["full-name"], link: item.link, cls: cls, namespace: namespace})
    });
    var groupedItems = groupBy(res, 'namespace');
    return groupedItems;
};
api.search.getNamespaceDescription = function(namespace){
    var namespaces = api.search.data_.namespaces;
    for (var i = 0; i < namespaces.length; i++){
        if (namespaces[i]["full-name"] == namespace){
            return namespaces[i].description;
        }
    }
    return false;
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

    var groupedItems = api.search.groupByNamespace(item.group);

    var $searchPage = $("<div></div>");
    if (api.search.data_["methods"] && api.search.data_["methods"][item.name]){
        $searchPage.append(api.search.data_.methods[item.name]);
    }

    var $total =  $("<div style='display:flex; flex-wrap:wrap;'></div>");

    var groupedItemsArr = [];
    for (var namespace in groupedItems) {
        groupedItemsArr.push(groupedItems[namespace]);
    }
    groupedItemsArr = groupedItemsArr.sort(function(a, b){
        if (a[0].namespace > b[0].namespace) return 1;
        if (a[0].namespace < b[0].namespace) return -1;
        return 0;
    });

    for (var j = 0; j < groupedItemsArr.length; j++) {
        var group = groupedItemsArr[j];
        var $res = $("<div style='margin-right: 20px; width: 300px;'></div>");
        $res.append("<h4>" + group[0].namespace +  "</h4>");

        var description = api.search.getNamespaceDescription(group[0].namespace);
        if (description){
            $res.append("<p>" + description +  "</p>");
        }

        var $list = $("<ul></ul>");

        for (var i = 0; i < group.length; i++) {
            var entry = group[i];
            $list.append("<li><a class='item-link' href='/" + api.config.version + "/" + entry.link + "'>" + prefix + entry["cls"] + postfix + "</a></li>");
        }
        $res.append($list);
        $total.append($res);
    }
    $("#search-results-new").hide();
    api.config.page = null;
    $searchPage.append($total);
    api.page.showSearchResults($searchPage);
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
            (function(item) {
                var $link = $("<li><a class='group-link' href='#'>" + prefix + item.name + postfix + " <span>"+ item.group.length +" matches</span></a></li>");
                $link.find("a").click(function() {
                    api.search.showGrouped_(item, prefix, postfix);
                    return false;
                }).mouseenter(function(e){
                    api.search.searchOver(e.currentTarget);
                });
                $res.append($link);
            })(item);
        }
    }
    for (var i = 0; i < items.length; i++) {
        var item = items[i];
        if (!item.multiple){
            var $link = $("<li><a class='item-link' href='/" + api.config.version + "/" + item.link + "'>" + prefix + item["full-name"] + postfix + "</a></li>");
            $link.find("a").mouseenter(function(e){
                api.search.searchOver(e.currentTarget);
            });
            $res.append($link);
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

    $("#search-results-new").show();
    $("#search-results-new").html("");
    $("#search-results-new").append($res);

    if (!$res.find("a").length) {
        api.search.showEmpty_($res);
    }else{
        api.search.setSearchIndex(0)
    }
};


/**
 * ========================== search from server ===============================
 */
/**
 * @private
 * @param {Array} data
 * @param {string} query
 */
/*api.search.onDataLoad_ = function(data, query){
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
};*/

/**
 * @private
 * @param {string} query
 */
/*api.search.makeSearchReq = function(query){
    if (query.length < 2) {
        api.search.hide();
        return;
    }
    $.get("/" + api.config.version + "/search.json?q=" + query, function(data){
        api.search.onDataLoad_(data, query);
    });
};*/

/*api.search.init_ = function() {
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
};*/

api.search.setSearchIndex = function(index, notScroll){
    var elems = $("#search-results-new li a");
    if (index >= 0 && index < elems.length) {
        if (index == 0) {
            api.search.scrollOfftop_ = $("#search-results-new").height()/2;
        }
        api.search.searchIndex_ = index;
        elems.removeClass("hovered");
        $(elems[api.search.searchIndex_]).addClass("hovered");
        //console.log($("#search-results-new").scrollTop() + " " + $(elems[api.search.searchIndex_]).parent().position().top + " " + $("#search-results-new").height());
        if (!notScroll) {
            $('#search-results-new').animate({
                scrollTop: $("#search-results-new").scrollTop() + $(elems[api.search.searchIndex_]).parent().position().top -
                api.search.scrollOfftop_
            }, 20);
        } else {
            api.search.scrollOfftop_ = $(elems[api.search.searchIndex_]).parent().position().top;
        }
    }
};

api.search.selectUpSearchResult_ = function(){
    api.search.setSearchIndex(api.search.searchIndex_ - 1);
};

api.search.selectDownSearchResult_ = function(){
    api.search.setSearchIndex(api.search.searchIndex_ + 1);
};

api.search.searchOver = function(elem){
    var elems = $("#search-results-new li a");
    for (var i = 0; i < elems.length && elems[i].innerHTML != elem.innerHTML;) i++;
    api.search.setSearchIndex(i, true);
};

api.search.openSearchResult_ = function(){
    var elems = $("#search-results-new li a");
    if (elems.length) {
        $(elems[api.search.searchIndex_]).trigger('click');
        $("#search").blur();
    }
};

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
        api.search.search_($(this).val().trim());
    });
    $("#search").keydown(function(e) {
        switch(e.keyCode){
            case 13: e.preventDefault(); break;
            case 38: api.search.selectUpSearchResult_(); break;
            case 40: api.search.selectDownSearchResult_(); break;
        }
    });
    $("#search").keyup(function(e) {
        switch (e.keyCode){
            case 13:
                e.preventDefault();
                api.search.openSearchResult_();
                break;
            case 27:
                api.search.hide();
                $("#search").val('');
                break;
            case 38: e.preventDefault(); break;
            case 40: e.preventDefault(); break;
            default:
                api.search.search_($(this).val().trim());
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

