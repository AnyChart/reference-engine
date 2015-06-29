function loadPage(target) {
    var cleanedTarget = target;
    if (cleanedTarget.indexOf("#") != -1)
        cleanedTarget = cleanedTarget.substr(0, cleanedTarget.indexOf("#"));

    if (cleanedTarget == location.pathname) {
        return target.indexOf("#") != -1;
    }

    if (typeof window.history == "undefined") {
        return true;
    }

    window.history.pushState(null, null, target);

    $(".content-container").html('<div class="loader"><i class="fa fa-spinner fa-spin fa-pulse fa-2x fa-fw"></i> <span> loading ...</span> </div>');

    $.get(cleanedTarget + "/data", function(res) {
        $(".content-container").html(res.content);
    });

    return false;
}

$(function() {

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
});

// --- olya's js

var resizable;
var hiddenMenuSize = 30;
var current_parent_id;

$(function(){
    $('#size-controller').on( "mousedown", function(e){
        e.preventDefault();
        resizable = true;
    });
    $('body').on( "mouseup", function(e){
        resizable = false;
    }).on( "mousemove", function(e){
        if (resizable){
            e.preventDefault();
            if (e.pageX > 250 &&  e.pageX < window.innerWidth - 300){
                $('#menu-bar').css('width', e.pageX);
                $('#content-wrapper').css('margin-left', e.pageX).css('width', window.innerWidth - e.pageX);
                $('.breadcrumb').css('left', $('#menu-bar').width());
            }
        }
    });
    prettyPrint();
    $(window).resize(function(){contentSize();});
    $(window).load(function(){
        $("#content-wrapper").mCustomScrollbar({theme:"minimal-dark", scrollInertia: 0, callbacks:{ onScroll:function(){
            checkTop(this);
        }}});
        $("#tree-wrapper").mCustomScrollbar({theme:"minimal-dark", scrollInertia: 0});
        contentSize();
        $('.selectpicker').selectpicker();
        $('.typeahead').typeahead({source: getTypeAheadList(), autoSelect: true, items: 100, scrollHeight: 0});
    });
});

function checkTop(el){
    if (el.mcs.top < 0 - window.innerHeight){
        $('#top').fadeIn();
    } else{
        $('#top').fadeOut();
    }
}

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

function changeContent(ids){
    var hash = '';
    location.hash = hash + 'tree=';
    for (var i = 0; i < ids.length; i++){
        var $treeItem = $('li#' + ids[i]);
        hash += ids[i] + '/';
        var contentType = setTreeContent(ids[i]);
        $treeItem.addClass('active');
        if (contentType == 'method') updateContent(ids[i], ids[i-1]);
        else if (contentType == 'page' && ids.length == i + 1) updateContent(null, ids[i]);
    }
    location.hash = 'tree=' + hash + '&';
}

function makeContentActive(ids){
    $('#tree li').removeClass('active');
    var hash = '';
    location.hash = hash + 'tree=';
    for (var i = 0; i < ids.length; i++){
        var $treeItem = $('li#' + ids[i]);
        hash += ids[i] + '/';
        $treeItem.addClass('active');
    }

    var $methodBlock = $('#method_' + ids[ids.length - 1]);
    if (!$methodBlock.hasClass('active')) $methodBlock.addClass('selected');
    $('.method-block').removeClass('active');
    $methodBlock.addClass('active');

    window.setTimeout(function(){
        $("#content-wrapper").mCustomScrollbar('scrollTo', $methodBlock,
        {scrollInertia: 700});
    }, 200);
    location.hash = 'tree=' + hash + '&';
}

function updateContent(id, parent_id){
    $('.in-content').hide();
    if (current_parent_id != parent_id || !id) {
        $('.method-block').removeClass('active');
        $("#content-wrapper").mCustomScrollbar('scrollTo', 0);
    }
    uploadContent(parent_id);
    if (id) {
        $('.method-block').removeClass('active');
        $('#method_' + id).addClass('active');
        window.setTimeout(function(){
            $("#content").css('min-height', $("#content").height());
            $("#content-wrapper").mCustomScrollbar('scrollTo', $('#method_' + id), {scrollInertia: 700});
            $('.panel').on('hide.bs.collapse', function (e) {
                var $methodBlock = $(this).parent().parent();
                console.log($methodBlock.hasClass('selected'), $methodBlock.hasClass('active'));
                if ($methodBlock.hasClass('selected')) {
                    e.preventDefault();
                    $methodBlock.removeClass('selected');
                }
            });
        }, 200);
    }
}

function getHashParams() {
    var hashParams = {};
    var e,
        a = /\+/g,
        r = /([^&;=]+)=?([^&;]*)/g,
        d = function (s) { return decodeURIComponent(s.replace(a, " ")); },
        q = window.location.hash.substring(1);
    while (e = r.exec(q))
       hashParams[d(e[1])] = d(e[2]);
    return hashParams;
}



/**
    получить список всех методов для поиска
*/
function getTypeAheadList(){
    return [{id: "1", name: "anychart.data#mapAsTable"},
            {id: "2", name: "anychart.data#set"},
            {id: "3", name: "anychart.core.radar.series"},
            {id: "4", name: "anychart.core.scatter.series"},
            {id: "5", name: "anychart.core.utils"},
            {id: "6", name: "anychart.core.grids"},
            {id: "7", name: "anychart.core.gauge.pointers"},
            {id: "8", name: "anychart.core.axisMarkers"},
            {id: "9", name: "anychart.core.cartesian.series"},
            {id: "10", name: "anychart.core.axes"},
            {id: "11", name: "anychart.core.polar.series"},
            {id: "12", name: "anychart.grids"},
            {id: "13", name: "anychart.grids#polar"},
            {id: "14", name: "anychart.grids#linear"},
            {id: "15", name: "anychart.grids#radar"},
            {id: "16", name: "anychart#sparkline"},
            {id: "17", name: "anychart#cartesian"},
            {id: "18", name: "anychart#ganttResource"},
            {id: "19", name: "anychart#licenseKey"},
            {id: "20", name: "anychart#scatter"},
            {id: "21", name: "anychart#fromJson"},
            {id: "22", name: "anychart.graphics"},
            {id: "23", name: "anychart.graphics#path"},
            {id: "24", name: "anychart.graphics#hatchFill"},
            {id: "25", name: "anychart.graphics#type"},
            {id: "26", name: "anychart.graphics#image"},
            {id: "27", name: "anychart.graphics#validate"},
            {id: "28", name: "anychart.graphics#server"},
            {id: "29", name: "anychart.graphics#layer"},
            {id: "30", name: "anychart.graphics#circle"},
            {id: "31", name: "anychart.graphics#patternFill"},
            {id: "32", name: "anychart.graphics#text"},
            {id: "33", name: "anychart.graphics#create"},
            {id: "34", name: "anychart.graphics#rect"},
            {id: "35", name: "anychart.graphics#ellipse"},
            {id: "36", name: "anychart.ui#labelsFactory"},
            {id: "37", name: "anychart.ui#markersFactory"},
            {id: "38", name: "anychart.scales"},
            {id: "39", name: "anychart.scales#linear"},
            {id: "40", name: "anychart.scales#dateTime"},
            {id: "41", name: "anychart.scales#ordinal"}]
}

/**
    находим в дереве нужную li по id в дереве и заполняем новым контентом.
    return - тип "page" - если нужно просто показать content
             тип "method" - если нужно просто показать content и проскролиться до нужного метода или константы
*/
function setTreeContent(id){
    var $treeItem = $('#' + id);
    if (id == 0) {
        $treeItem.html(
            '<a onclick="changeContent([0])"><i class="fa fa-chevron-down"></i> anychart </a>' +
            '<ul>' +
                '<li id="2"> <a onclick="changeContent([0, 2])">CONSTANT</a></li>' +
                '<li id="3"> <a onclick="changeContent([0, 3])">CONSTANT</a></li>' +
                '<li id="4"> <a onclick="changeContent([0, 4])">method()</a></li>' +
                '<li id="1" class="pull-down"><a onclick="changeContent([0, 1])"><i class="fa fa-chevron-right"></i> namespace </a></li>' +
                '<li id="14" class="pull-down"><a onclick="changeContent([0, 14])"><i class="fa fa-chevron-right"></i> axes</a></li>' +
                '<li id="15" class="pull-down"><a onclick="changeContent([0, 15])"><i class="fa fa-chevron-right"></i> axisMarkers</a></li>' +
                '<li id="16" class="pull-down"><a onclick="changeContent([0, 16])"><i class="fa fa-chevron-right"></i> charts</a></li>' +
                '<li id="17" class="pull-down"><a onclick="changeContent([0, 17])"><i class="fa fa-chevron-right"></i> color</a></li>' +
                '<li id="18" class="pull-down"><a onclick="changeContent([0, 18])"><i class="fa fa-chevron-right"></i> core</a></li>' +
                '<li id="19" class="pull-down"><a onclick="changeContent([0, 19])"><i class="fa fa-chevron-right"></i> data</a></li>' +
                '<li id="20" class="pull-down"><a onclick="changeContent([0, 20])"><i class="fa fa-chevron-right"></i> enums</a></li>' +
                '<li id="21" class="pull-down"><a onclick="changeContent([0, 21])"><i class="fa fa-chevron-right"></i> graphics</a></li>' +
                '<li id="22" class="pull-down"><a onclick="changeContent([0, 22])"><i class="fa fa-chevron-right"></i> grids</a></li>' +
                '<li id="23" class="pull-down"><a onclick="changeContent([0, 23])"><i class="fa fa-chevron-right"></i> math</a></li>' +
                '<li id="24" class="pull-down"><a onclick="changeContent([0, 24])"><i class="fa fa-chevron-right"></i> palettes</a></li>' +
                '<li id="25" class="pull-down"><a onclick="changeContent([0, 25])"><i class="fa fa-chevron-right"></i> scales</a></li>' +
                '<li id="26" class="pull-down"><a onclick="changeContent([0, 26])"><i class="fa fa-chevron-right"></i> ui</a></li>' +
                '<li id="27" class="pull-down"><a onclick="changeContent([0, 27])"><i class="fa fa-chevron-right"></i> utils</a></li>' +
            '</ul>');
        return 'page'
    }
    else if (id == 1) {
        $treeItem.html(
            '<a  onclick="changeContent([0, 1])"><i class="fa fa-chevron-down"></i> namespace </a>' +
            '<ul>' +
                '<li id="5"> <a onclick="changeContent([0, 1, 5])">method()</a></li>' +
                '<li id="6"> <a onclick="changeContent([0, 1, 6])">method()</a></li>' +
                '<li id="7"> <a onclick="changeContent([0, 1, 7])">[Enum]</a></li>' +
                '<li id="8"> <a onclick="changeContent([0, 1, 8])">{Typedef}</a></li>' +
                '<li id="9"> <a onclick="changeContent([0, 1, 9])"> {Typedef complex}</a></li>' +
                '<li id="10" class="pull-down"><a onclick="changeContent([0, 1, 10])"><i class="fa fa-chevron-right"></i> Class </a></li>' +
            '</ul>');
        return 'page'
    }
    else if (id == 10) {
        $treeItem.html(
            '<a class="class" onclick="changeContent([0, 1, 10])"><i class="fa fa-chevron-down"></i> Class </a>' +
            '<ul>' +
                '<li id="11"> <a onclick="changeContent([0, 1, 10, 11])"> method()</a> </li>' +
                '<li id="12"> <a onclick="changeContent([0, 1, 10, 12])"> method()</a></li>' +
                '<li id="13"> <a onclick="changeContent([0, 1, 10, 13])"> method()</a></li>' +
            '</ul>');
        return 'page'
    }
    else if (id >= 14) {
        $treeItem.html($('li#' + id).html());
        return 'page'
    }
    else if (id == 7 || id == 8 || id == 9)
        return "page";
    return "method";
}

/**
    upload новый текст по id страницы
*/
function uploadContent(id){
    var $container = $('#content .content-container');
    var $breadcrumbs = $('.breadcrumb');

    if (id == 7) {
        $container.html($('#enum').html());
        $breadcrumbs.html('<li><a onclick="changeContent([0])">anychart</a></li><li><a onclick="changeContent([0, 1])">namespace</a></li><li class="active">[Enum]</li>');

    } else if (id == 8) {
        $container.html($('#typedef').html());
        $breadcrumbs.html('<li><a onclick="changeContent([0])">anychart</a></li><li><a onclick="changeContent([0, 1])">namespace</a></li><li class="active">{Typedef}</li>');

    } else if (id == 9) {
        $container.html($('#typedef_complex').html());
        $breadcrumbs.html('<li><a onclick="changeContent([0])">anychart</a></li><li><a onclick="changeContent([0, 1])">namespace</a></li><li class="active">{Typedef complex}</li>');

    } else if (id == 10) {
        $container.html($('#class').html());
        $breadcrumbs.html('<li><a onclick="changeContent([0])">anychart</a></li><li><a onclick="changeContent([0, 1])">namespace</a></li><li class="active">Class</li>');

    } else if (id == 0) {
        $container.html($('#namespace_anychart').html());
        $breadcrumbs.html('<li class="active">anychart</li>');

    } else if (id == 1) {
        $container.html($('#namespace').html());
        $breadcrumbs.html('<li><a onclick="changeContent([0])">anychart</a></li><li class="active">namespace</li>');
    }
    current_parent_id = id;
}

/**
    изменить версию документации
*/
$('.versionselect').on('change', function(){
    var selected = $(this).find("option:selected").val();
    alert(selected);
});
