var searchPrefix = 'query.html?q=';
var g_tagIndex = 0;
function initSearchPane() {			
	initSearchInput();
	initSearchButton();
	bindEnterKey();
}

function initSearchInput() {
	$('#searchInput').textext({
		plugins: 'tags',
	});	

	
	$.get('listClassesAndProperties?term=label', function () {
		
	});
	$('#searchInput').autocomplete({
		source: 'listClassesAndProperties'		
	});
}

function initSearchButton() {
	$('body').tooltip({selector:'[rel=tooltip]'});
	$('#searchBtn').bind('click', searchBtnHandler);
	$('#advancedBtn').bind('click', advancedBtnHandler);
	if(getParam('bool') == null || getParam('bool') == '') {
		turnOnAdvancedBtn();
	}
	else {
		turnOffAdvancedBtn();
	}
}

function getParam(paramName) {
	var hrefStr = location.href;
	if(hrefStr.lastIndexOf('?') === -1 || hrefStr.lastIndexOf('?') === (hrefStr.length - 1)) {		
		return null;
	}
	var params = hrefStr.split('?')[1];
	if(params == null) return null;
	var paramsArray = params.split('&');
	for(var i = 0; i < paramsArray.length; i++) {
		var curParam = paramsArray[i];
		if(curParam.split('=')[0] == paramName) {
			return curParam.substring(curParam.indexOf('=') + 1);
		}
	}
	return null;
}

function tagStyleSetter(item) {
	if(item.trim() === '') {
		return;
	}
	if(item.indexOf('p:') === 0 || item.indexOf('P:') === 0) {
		$('.text-button:last').css('background-color','#d14');
	}
	else if (item.indexOf('c:') === 0 || item.indexOf('C:') === 0) {
		$('.text-button:last').css('background-color','#1d4');
	}
	else if (item.indexOf('s:') === 0 || item.indexOf('S:') === 0) {
		$('.text-button:last').css('background-color','#9b479f');
	}
	else {
		$('.text-button:last').css('background-color','#1ad');		
	}
	$('.text-button:last').css('color','#FFF');
	$('.text-button:last').css('font-weight','bold');
}

function bindEnterKey() {
	$("#searchInput").keydown(function(event){
		if(event.which == 13){
			searchBtnHandler();
			return false;
		}
	});
}

function advancedBtnHandler() {
	var btn = $(this);	
	if(btn.hasClass('turnOn')) {
		turnOnAdvancedBtn();
	}
	else {
		turnOffAdvancedBtn();
	}
}

function turnOnAdvancedBtn() {
	var btn = $('#advancedBtn');
	var icon = btn.find('i');
	var searchBtnText = $('#searchBtn').children('span');
	btn.removeClass('turnOn');
	icon.removeClass('icon-chevron-left');
	icon.addClass('icon-chevron-right');
	searchBtnText.html('Search');
}

function turnOffAdvancedBtn() {
	var btn = $('#advancedBtn');
	var icon = btn.find('i');
	var searchBtnText = $('#searchBtn').children('span');
	btn.addClass('turnOn');
	icon.removeClass('icon-chevron-right');
	icon.addClass('icon-chevron-left');
	searchBtnText.html('Advanced Search');
}

function searchBtnHandler() {
	convertPlainText();
	var queryString = $('.text-core input').val();
	if(queryString != '[]') {		
		var encodedQuery = encodeURIComponent(queryString);
		if($('#advancedBtn').hasClass('turnOn')) {
			location.href = searchPrefix + encodedQuery + "&bool=true";
		}
		else {
			location.href = searchPrefix + encodedQuery;
		}
	}	
}

function convertPlainText() {	
	var plainText = $('#searchInput').val();
	$('#searchInput').val('');
	if(plainText.trim() != '') {		
		addTagToSearchInput(plainText);
		
	}
}

function addTagToSearchInput(text) {
	$('#searchInput').textext()[0].tags().addTags([ text ]);
}

function updateFromCache() {
	$('#searchInput').textext()[0].tags().updateFromCache();
}

function changeValueTag(tag, value) {
	var newTag = tag.substr(0, tag.indexOf('=') + 1);
	newTag += value;
	return newTag;
}

function changeRangeTag(tag, min, max) {
	var newTag = tag.substr(0, tag.indexOf('=') + 1);
	newTag += '[' + min + ',' + max + ']';
	return newTag;
}

function isValueFilter(item) {
	if(item.indexOf('=') < 0) {
		return false;
	}
	else {
		if(item.indexOf('[') < 0 && item.indexOf(']') < 0) {
			return true;
		}
		else {
			return false;
		}
		
	}
}

function isRangeFilter(item) {
	if(item.indexOf('=') < 0) {
		return false;
	}
	else {
		if(item.indexOf('[') > 0 && item.indexOf(']') > 0) {
			return true;
		}
		else {
			return false;
		}
	}
}

function isEmptyValueFilter(item) {
	if(item.indexOf('=?') < 0) {
		return false;
	}
	else {
		return true;
	}
}

function isEmptyRangeFilter(item) {
	if(item.indexOf('=[]') < 0) {
		return false;
	}
	else {
		return true;
	}
}
