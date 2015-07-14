(function() {
    function cleanupPath(target) {
        if (target.indexOf("http") != -1)
            target = target.substr(target.indexOf("/", target.indexOf("//") + 2));
        if (target.indexOf("#") != -1)
            target = target.substr(0, target.indexOf("#"));
        return target;
    }

    function getEntryFromUrl(path) {
        path = cleanupPath(path);
        return path.match("^/[^/]+/(.*)$")[1];
    }

    function scrollToEntry(entry) {
        $('.method-block').removeClass('active');
        $('#' + entry).addClass('active');
        window.setTimeout(function(){
            $("#content").css('min-height', $("#content").height());
            $("#content-wrapper").mCustomScrollbar('scrollTo', $('#' + entry), {scrollInertia: 700});
            $('.panel').on('hide.bs.collapse', function (e) {
                var $methodBlock = $(this).parent().parent();
                if ($methodBlock.hasClass('selected')) {
                    e.preventDefault();
                    $methodBlock.removeClass('selected');
                }
            });
        }, 200);
    }

    function scrollTreeToEntry(entry, opt_hash) {
        var sel = entry + (opt_hash ? ("#" + opt_hash) : "");
        
        var $target = $("#tree li[x-data-name='" + sel + "']");
        window.setTimeout(function(){
            $("#tree-wrapper").mCustomScrollbar("scrollTo", $target.offset().top - 120, {scrollInertia: 700});
        }, 200);
    }

    function updateBreadcrumb(path) {
        path = cleanupPath(path);
        $("ol.breadcrumb").html('');
        var parts = path.split(".");
        for (var i = 0; i < parts.length; i++) {
            var $el;
            if (i < parts.length - 1) {
                var url = parts.slice(0,i+1).join(".");
                $el = $("<li><a href='/" + version + "/"+url+"'>" + parts[i] + "</a></li>");
                $el.find("a").click(typeLinkClick);
            }else {
                $el = $("<li class='active'>"+parts[i]+"</li>");
            }
            $("ol.breadcrumb").append($el);
        }
    }

    function loadPage(target, opt_add, opt_scrollTree) {
        $("#search-results-new").hide();

        if (opt_add == undefined) opt_add = true;

        if (target.indexOf("#") == 0) {
            target = location.pathname + target;
        }
        var cleanedTarget = cleanupPath(target);

        if (cleanedTarget == location.pathname) {
            if (target.indexOf("#") != -1) {
                highlightInPage(target.substr(target.indexOf("#")+1));
            }
            return false;
        }

        if (typeof window.history == "undefined") {
            return true;
        }

        if (opt_add)
            window.history.pushState(null, null, target);

        expandInTree(target);

        if (opt_scrollTree)
            scrollTreeToEntry(cleanedTarget, hash);
        
        $(".content-container").html('<div class="loader"><i class="fa fa-spinner fa-spin fa-pulse fa-2x fa-fw"></i> <span> loading ...</span> </div>');
        $("#content-wrapper").mCustomScrollbar('destroy');

        $.get(cleanedTarget + "/data", function(res) {

            document.title = res.title;

            $("#content-wrapper").html('<div id="content"><div class="content-container">'+res.content+'</div></div>');

            page = res.page;
            
            updateContentScrolling();
            
            $("#warning a").attr("href", "/" + $("#warning a").attr("data-last-version") + "/try/" + res.page);
            updateBreadcrumb(res.page);
            fixLinks();
            fixListings();

            var hash = null;
            if (target.indexOf("#") != -1) {
                hash = target.substr(target.indexOf("#") + 1);
                highlightInPage(hash);
            }
        });

        return false;
    }

    function typeLinkClick(e) {
        if (e.ctrlKey || e.metaKey) return true;
        return loadPage($(this).attr("href"));
    }

    function typeLinkClickWithTreeScroll(e) {
        if (e.ctrlKey || e.metaKey) return true;
        return loadPage($(this).attr("href"), true, true);
    }

    function fixLinks() {
        $("#content a.type-link").click(typeLinkClickWithTreeScroll);
    }

    function fixListings() {
        prettyPrint();
    }

    function highlightInPage(target, opt_expand, opt_scroll) {
        var expand = opt_expand == undefined ? true : opt_expand;
        var scroll = opt_scroll == undefined ? true : opt_scroll;
        $(".content-container .active").removeClass("active");
        if (expand) {
            var entry = getEntryFromUrl(location.pathname);
            doExpandInTree(entry, target);
        }
        if (scroll) {
            setTimeout(function() {
                $("#content-wrapper").mCustomScrollbar("scrollTo", $("#" + target), {scrollInertia: 700});
            }, 100);
        }
        $("#" + target).parent().addClass("active");
        location.hash = target;
    }

    function doExpandInTree(entry, opt_hash) {
        $("#tree .active").removeClass("active");
        
        var parts = entry.split(".");
        
        for (var i = 0; i < parts.length; i++) {
            var path = parts.slice(0, (i+1)).join(".");
            var $el = $("#tree li[x-data-name='" + path + "']");
            $el.find(">ul").show();
            $el.find(">a i").removeClass("fa-chevron-right").addClass("fa-chevron-down");
            $el.addClass("active");
        }

        if (opt_hash) {
            var $el = $("#tree li.item[x-data-name='" + entry + "#" + opt_hash + "']");
            $el.addClass("active");
        }
    }

    function expandInTree(path) {
        path = cleanupPath(path);
        var entry = path.match("^/[^/]+/(.*)$")[1];
        if (entry)
            doExpandInTree(entry);
    }
    
    var scrollSettings = (function() {
        var scrollAmount = 80;
        var scrollKeyAmount = 100;
        if (navigator.platform.match(/(Mac|iPhone|iPod|iPad)/i)) {
            scrollAmount = 2;
            scrollKeyAmount = 15;
        }
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

    function updateContentScrolling() {

        var isTopVisible = false;

        var checkScrollToTop = function(top) {
            if (top < 0) {
                if (!isTopVisible) {
                    $('#top-page-content').fadeIn();
                    isTopVisible = true;
                }
            } else {
                if (isTopVisible) {
                    $('#top-page-content').fadeOut();
                    isTopVisible = false;
                }
            }
        }

        var findFirstVisible = function() {
            // we need nearest to 100px
            var minDistance = Number.MAX_VALUE;
            var $minEl = null;
            var d = $("div.content-block.methods h3").each(function() {
                var $el = $(this);
                var distance = Math.abs($el.offset().top - 100);
                if (distance < minDistance) {
                    minDistance = distance;
                    $minEl = $el;
                }
            });
            return $minEl ? $minEl.attr("id") : null;
        }

        var currentVisible = null;
        var onContentScroll = function() {
            checkScrollToTop(this.mcs.top);
            var el = findFirstVisible();
            if (el && el != currentVisible) {
                currentVisible = el;
                var link = "/" + version + "/" + page + "#" + el;
                doExpandInTree(page, el);
                highlightInPage(el, false, false);
                
            }
        }
        
        $("#content-wrapper").mCustomScrollbar(
            $.extend(scrollSettings,
                     { callbacks: {
                         onScroll: onContentScroll }}));
    }

    $(function() {

        // scrolling
        updateContentScrolling();
        $("#tree-wrapper").mCustomScrollbar(scrollSettings);

        // tree
        $("#tree li.group").each(function() {
            var $ul = $(this).find(">ul");
            $(this).find(">a").click(function(e) {
                if (e.ctrlKey || e.metaKey) return true;
                $ul.toggle();
                if ($ul.is(":visible"))
                    $(this).find("i").addClass("fa-chevron-down").removeClass("fa-chevron-right");
                else
                    $(this).find("i").addClass("fa-chevron-right").removeClass("fa-chevron-down");
                return loadPage($(this).attr("href"));
            });
        });
        
        $("#tree li.item a").click(function(e) {
            if (e.ctrlKey || e.metaKey) return true;
            return loadPage($(this).attr("href"));
        });

        expandInTree(location.pathname);
        updateBreadcrumb(getEntryFromUrl(location.pathname));
        scrollTreeToEntry(page, location.hash ? location.hash.substr(1) : null);
        
        // content links
        fixLinks();
        fixListings();
        if (location.hash) {
            $("#content-wrapper").mCustomScrollbar("scrollTo", $(location.hash));
        }

        // versions
        $('.versionselect').on('change', function(){
            location.href = "/" + $(this).find("option:selected").val() + "/try/" + getEntryFromUrl(location.pathname);
        });
        
        // resize
        $("#size-controller").on("mousedown", function(e) {

            var mouseUp = function(e) {
                $("body").off("mouseup", mouseUp);
                $("body").off("mousemove", mouseMove);
            }

            var mouseMove = function(e) {
                if (e.pageX > 250 &&  e.pageX < window.innerWidth - 300) {
                    $('#menu-bar').css('width', e.pageX);
                    $('#content-wrapper')
                        .css('margin-left', e.pageX)
                        .css('width', window.innerWidth - e.pageX);
                    $('.breadcrumb').css('left', $('#menu-bar').width());
                }
                return false;
            }
            
            $("body").on("mouseup", mouseUp);
            $("body").on("mousemove", mouseMove);
            
            return false;
        });

        // search
        
        $.get("/"+version+"/data/search.json", function(data) {

            var contains = function(str, target) {
                return str.toLowerCase().indexOf(target.toLowerCase()) != -1;
            }

            var searchForConstants = function(str) {
                var res = [];
                for (var i = 0; i < data.namespaces.length; i++) {
                    var ns = data.namespaces[i];
                    for (var j = 0; j < ns.constants.length; j++) {
                        var c = ns.constants[j];
                        if (contains(c.name, str)) {
                            res.push(c);
                        }
                    }
                }
                return res;
            }

            var searchForGrouped = function(str, container, key) {
                var res = [];
                for (var i = 0; i < data[container].length; i++) {
                    var c = data[container][i];
                    for (var j = 0; j < c[key].length; j++) {
                        var m = c[key][j];
                        if (contains(m.name, str)) {
                            res.push(m);
                        }
                    }
                }
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
            }

            var filterEntries = function(str, entries, type) {
                var res = [];
                for (var i = 0; i < entries.length; i++) {
                    var entry = entries[i];
                    if (contains(entry.name, str)) {
                        res.push(entry);
                    }
                }
                return res;
            }

            var searchForMethods = function(str) {
                return searchForGrouped(str, "classes", "methods");
            }

            var searchForFunctions = function(str) {
                return searchForGrouped(str, "namespaces", "functions");
            }

            var searchForNamespaces = function(str) {
                return filterEntries(str, data.namespaces, "namespace");
            }

            var searchForClasses = function(str) {
                return filterEntries(str, data.classes, "class");
            }

            var searchForEnums = function(str) {
                return filterEntries(str, data.enums, "enum");
            }

            var searchForTypedefs = function(str) {
                return filterEntries(str, data.typedefs, "typedef");
            }

            var showGroupedResult = function(item, prefix, postfix) {
                $("ol.breadcrumb li").remove();
                $("ol.breadcrumb").append("<li class='active'>Search results for " + item.name + "</li>");
                $("#search-results-new").hide();
                $("#content-wrapper").mCustomScrollbar('destroy');
                var $res = $("<ul></ul>");
                for (var i = 0; i < item.group.length; i++) {
                    var entry = item.group[i];
                    $res.append("<li><a class='item-link' href='/" + version + "/" + entry.link + "'>" + prefix + entry["full-name"] + postfix + "</a></li>");
                }

                $res.find("a").click(typeLinkClick);
                $("#content-wrapper").html('<div id="content"><div class="content-container"></div></div>');
                $("#content .content-container").append($res);
                updateContentScrolling();
            }

            var addToResults = function($res, items, prefix, postfix, title) {
                if (!items.length) return;
                $res.append("<li class='group-name'>" + title +"</li>");

                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    if (item.multiple) {
                        (function(item) {
                            var $link = $("<li><a class='group-link' href='#'>" + prefix + item.name + postfix + " <span>"+ item.group.length +" matches</span></a></li>");
                            $link.find("a").click(function() {
                                showGroupedResult(item, prefix, postfix);
                                return false;
                            });
                            $res.append($link);
                        })(item);
                    }
                }
                for (var i = 0; i < items.length; i++) {
                    var item = items[i];
                    if (!item.multiple)
                        $res.append("<li><a class='item-link' href='/" + version + "/" + item.link + "'>" + prefix + item["full-name"] + postfix + "</a></li>");
                }
            }

            function searchFor(query) {
                if (query.length < 3) {
                    $("#search-results-new").hide();
                    return;
                }
                var $res = $("<ul></ul>");
                addToResults($res, searchForConstants(query), "", "", "Constants");
                addToResults($res, searchForFunctions(query), "", "()", "Functions");
                addToResults($res, searchForMethods(query), "", "()", "Methods");
                addToResults($res, searchForNamespaces(query), "", "", "Namespaces");
                addToResults($res, searchForEnums(query), "[", "]", "Enums");
                addToResults($res, searchForTypedefs(query), "{", "}", "Typedefs");
                addToResults($res, searchForClasses(query), "", "", "Classes");
                $res.find("a.item-link").click(typeLinkClick);
                
                $("#search-results-new").show();
                $("#search-results-new").html("");
                $("#search-results-new").append($res);
            }

            $("#search-results-new").click(function(e) {
                e.stopPropagation();
            });
            $("html").click(function() {
                $("#search-results-new").hide();
            });
            $("#search").click(function() {
                return false;
            });
            $("#search").focus(function() {
                searchFor($(this).val());
            });
            
            $("#search").keyup(function(e) {
                if (e.keyCode == 27) { //esc
                    $("#search-results-new").hide();
                    $("#search").val('');
                    return;
                }
                searchFor($(this).val());
            });
        });

        // history api
        (function() {
            var ua = navigator.userAgent.toLowerCase();
            var needIgnoreFirst = ua.indexOf("safari") != -1 && ua.indexOf("chrome") == -1;
            var firstIgnored = false;

            window.onpopstate = function(e) {
                if (needIgnoreFirst && !firstIgnored) {
                    firstIgnored = true;
                    return;
                }
                loadPage(location.href, false);
            };
        }());
    });
})();

// --- olya's js

var resizable;
var hiddenMenuSize = 30;
var current_parent_id;

$(function(){
    
    $(window).resize(function(){contentSize();});
    $(window).load(function(){
        //$("#content-wrapper").mCustomScrollbar({theme:"minimal-dark", scrollInertia: 0, callbacks:{ onScroll:function(){
        //    checkTop(this);
        //}}});
        //$("#tree-wrapper").mCustomScrollbar({theme:"minimal-dark", scrollInertia: 0});
        contentSize();
        
        //$('.typeahead').typeahead({source: getTypeAheadList(), autoSelect: true, items: 100, scrollHeight: 0});
    });
});

function contentSize(){
    if (window.innerWidth < 992) {hideSideBar(true, false)}
    else {
        hideSideBar(false, false);
        $('#content-wrapper').css('width', window.innerWidth - 300).css('margin-left', 300);
        $('.breadcrumb').css('left', $('#menu-bar').width());
    }
    $('#search-results-new').css('max-height', window.innerHeight - 145)
}

function hideSideBar(flag, animate){
    if (flag){
        if (animate){
            $('#menu-bar').animate({width: hiddenMenuSize}, 300);
            $('#content-wrapper').animate({marginLeft: hiddenMenuSize, width: window.innerWidth - hiddenMenuSize}, 300);
            $('#search-form').animate({opacity: 0}, 200);
            $('#tree-wrapper').animate({opacity: 0}, 200).mCustomScrollbar("disable",true);
            $('.breadcrumb').animate({left: hiddenMenuSize}, 300);
            $('#footer').animate({opacity: 0}, 200);
        } else {
            $('#menu-bar').css('width', hiddenMenuSize);
            $('#content-wrapper').css('width', window.innerWidth - hiddenMenuSize).css('margin-left', hiddenMenuSize);
            $('#search-form').css('opacity', 0);
            $('#tree-wrapper').css('opacity', 0).mCustomScrollbar("disable",true);
            $('#footer').css('opacity', 0);
            $('.breadcrumb').css('left', hiddenMenuSize);
        }
        $('a.switcher').attr('onclick', 'hideSideBar(false, true)');
        $('a.switcher .fa').attr('class', 'fa fa-chevron-right');
        $('#size-controller').css('cursor', 'default');
    }else{
        if (animate){
            $('#menu-bar').animate({width: 300}, 300);
            $('#content-wrapper').animate({marginLeft: 300, width: window.innerWidth - 300}, 300);
            $('.breadcrumb').animate({left: 300}, 300);
        } else {
            $('#menu-bar').css('width', 300);
            $('#content-wrapper').css('width', window.innerWidth - 300).css('margin-left', 300);
            $('.breadcrumb').css('left', 300);
        }
        $('#search-form').css('opacity', 1);
        $('#tree-wrapper').css('opacity', 1).mCustomScrollbar('update');
        $('#footer').css('opacity', 1);
        $('a.switcher').attr('onclick', 'hideSideBar(true, true)');
        $('a.switcher .fa').attr('class', 'fa fa-chevron-left');
        $('#size-controller').css('cursor', 'col-resize');
    }
}
