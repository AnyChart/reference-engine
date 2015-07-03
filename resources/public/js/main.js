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

    function loadPage(target, opt_add) {
        if (opt_add == undefined) opt_add = true;

        if (target.indexOf("#") == 0) {
            target = location.pathname + target;
        }
        var cleanedTarget = cleanupPath(target);

        if (cleanedTarget == location.pathname) {
            if (target.indexOf("#") != -1) {
                highlightInPage(target.substr(target.indexOf("#")+1));
                return false;
            }
            return true;
        }

        if (typeof window.history == "undefined") {
            return true;
        }

        if (opt_add)
            window.history.pushState(null, null, target);

        expandInTree(target);
        
        $(".content-container").html('<div class="loader"><i class="fa fa-spinner fa-spin fa-pulse fa-2x fa-fw"></i> <span> loading ...</span> </div>');
        $("#content-wrapper").mCustomScrollbar('destroy');

        $.get(cleanedTarget + "/data", function(res) {

            $("#content-wrapper").html('<div id="content"><div class="content-container">'+res.content+'</div></div>');
            
            updateContentScrolling();
            
            $("#warning a").attr("href", "/" + $("#warning a").attr("data-last-version") + "/try/" + res.page);
            updateBreadcrumb(res.page);
            fixLinks();
            fixListings();
            if (target.indexOf("#") != -1)
                highlightInPage(target.substr(target.indexOf("#") + 1));
        });

        return false;
    }

    function typeLinkClick(e) {
        if (e.ctrlKey || e.metaKey) return true;
        return loadPage($(this).attr("href"));
    }

    function fixLinks() {
        $("#content a.type-link").click(typeLinkClick);
    }

    function fixListings() {
        prettyPrint();
    }

    function highlightInPage(target) {
        $(".content-container .active").removeClass("active");
        var entry = getEntryFromUrl(location.pathname);
        doExpandInTree(entry, target);

        setTimeout(function() {
            $("#content-wrapper").mCustomScrollbar("scrollTo", $("#" + target), {scrollInertia: 700});
        }, 100);
        $("#" + target).parent().addClass("active");
    }

    function doExpandInTree(entry, opt_hash) {
        $("#tree .active").removeClass("active");
        
        var parts = entry.split(".");
        
        for (var i = 0; i < parts.length; i++) {
            var path = parts.slice(0, (i+1)).join(".");
            var $el = $("#tree li.group[x-data-name='" + path + "']");
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
        $("#content-wrapper").mCustomScrollbar(
            $.extend(scrollSettings,
                     { callbacks: {
                         onScroll: function() {
                             if (this.mcs.top < 0 - window.innerHeight){
                                 $('#top').fadeIn();
                             } else{
                                 $('#top').fadeOut();
                             }
                         }}}))}

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

            var filterEntries = function(entries, name, childKeys) {
                var res = [];
                for (var i = 0; i < entries.length; i++) {
                    var filteredFields = {};
                    var hasFields = false;
                    for (var j = 0; j < childKeys.length; j++) {
                        var items = $.grep(entries[i][childKeys[j]], function(val) {
                            return val.indexOf(name) != -1;
                        });
                        filteredFields[childKeys[j]] = items;
                        hasFields = hasFields || items.length;
                    }
                    if (entries[i].name.toLowerCase().indexOf(name.toLowerCase()) != -1 || hasFields) {
                        var entry = {"full-name": entries[i]["full-name"], "name": entries[i].name};
                        for (var j = 0; j < childKeys.length; j++) {
                            entry[childKeys[j]] = filteredFields[childKeys[j]];
                        }
                        res.push(entry);
                    }
                }
                return res;
            }

            var searchResults = function(entries, prefix, postfix, childKeys) {
                var $res = $("<ul></ul>");
                if (!entries.length) return null;
                for (var i = 0; i < entries.length; i++) {
                    var $el = $("<li>" + prefix + entries[i]["full-name"] + postfix + "</li>");
                    for (var j = 0; j < childKeys.length; j++) {
                        if (entries[i][childKeys[j]].length) {
                            var $ul = $("<ul></ul>");
                            for (var k = 0; k < entries[i][childKeys[j]].length; k++) {
                                $ul.append("<li>" + entries[i][childKeys[j]][k] + "</li>");
                            }
                            $el.append($ul);
                        }
                    }
                    $res.append($el);
                }
                return $res;
            }

            function searchFor(query) {
                if (!query.length) {
                    $("#search-results").hide();
                    return;
                }
                $("#search-results").show();
                var $namespaces = searchResults(filterEntries(data.namespaces, query, ["constants", "functions"]),
                                               "", "", ["constants", "functions"]);
                var $classes = searchResults(filterEntries(data.classes, query, ["methods"]),
                                            "", "", ["methods"]);
                var $typedefs = searchResults(filterEntries(data.typedefs, query, ["properties"]),
                                             "{", "}", ["properties"]);
                var $enums = searchResults(filterEntries(data.enums, query, ["fields"]),
                                           "[", "]", ["fields"]);
                $("#search-results").html("");
                $("#search-results").append($namespaces);
                $("#search-results").append($classes);
                $("#search-results").append($typedefs);
                $("#search-results").append($enums);
            }
            
            $("#search").keyup(function() {
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
