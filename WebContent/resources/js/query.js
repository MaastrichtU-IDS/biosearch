var curPageNum = 0;
var sumPageNum = 0;
var g_maxRecoSize = 9;
var resultArray;
var propertyFilterArray;
var selectedPropertyFilter = new Array();
var searchPrefix = 'query.html?q=';
var instanceViewerURL = 'instance.html?inst=';
var query;
var isBool;

$(document).ready(initPage);
function initPage() {
	bindButtons();
	initSearchPane();
	requestByParam();
}
function bindButtons() {
	$('.btn').bind('click', btnListener);
}

function requestByParam() {
	query = getParam('q');
	isBool = getParam('bool');
	$.cookie('trace', '[{"label":"' + query + '","uri":"' + query + '"}]');
	setSearchInput(query);
	submitQuery(query, isBool);
}

function setSearchInput(query) {
	var list = $.parseJSON(decodeURIComponent(query));
	// var list = $.parseJSON(query);
	$.each(list, function(index, value) {
		addTagToSearchInput(value);
	});
}

function getParam(paramName) {
	var hrefStr = location.href;
	if(hrefStr.lastIndexOf('?') === -1 || hrefStr.lastIndexOf('?') === (hrefStr.length - 1)) {
		location.href = 'index.html';
	}
	var params = hrefStr.split('?')[1];
	var paramsArray = params.split('&');
	for(var i = 0; i < paramsArray.length; i++) {
		var curParam = paramsArray[i];
		if(curParam.split('=')[0] == paramName) {
			return curParam.substring(curParam.indexOf('=') + 1);
		}
	}
	return null;
}

function btnListener() {
	var btnId = this.id;
	switch(btnId) {
		case 'submitFilterBtn':
		submitFilter();
		break;
		case 'resetFilterBtn':
		resetFilter();
		break;
		default:
		break;
	}
}

function resetFilter() {
	for(var i = 0; i < propertyFilterArray.length; i++) {
		deselectPropertyFilter(i);
	}
}

function submitFilter() {
	filterByProperty();
	// var reqJSON = getFilterRequestJSON();
	// var filterStr = '{"filterArray":[{"type":"Int","property":"http://ws.nju.edu.cn/nju28/jp/ss","min":"80","max":"80000"}]}';
	// console.log($.parseJSON(reqJSON));
	// reqJSON = encodeURIComponent(reqJSON);
	// $.getJSON('facetFilter', {filter: reqJSON}, function callback(response) {
		// console.log(response);
		// filterResult(response);
	// });
}

function filterResult(filtered) {
	var filteredResultArray = new Array();
	for(var i = 0; i < filtered.length; i++) {
		var uri = filtered[i]['uri'];
		for(var j = 0; j < resultArray.length; j++) {
			if(uri == resultArray[j]['uri']) {
				filteredResultArray.push(resultArray[j]);
			}
		}
	}
	resultArray = filteredResultArray;
	hideResult();
	if(resultArray.length > 0) {
		constructPagination(filtered.length);
		displayPage(1);	
		showResult();
	}
	else {
		constructPagination(1);
		showNotFound();
	}
	console.log(resultArray);
}

function getFilterRequestJSON() {
	var json = '{"filterArray":[';
	$.each(selectedPropertyFilter, function(index, value) {
		var selected = value;
		var curJSON = getSingleFilterJSON(selected);
		json += curJSON + ',';
	});
	json = json.substring(0, json.length - 1) + ']}';
	return json;
}

function getSingleFilterJSON(selected) {
	var curFilter = propertyFilterArray[selected];
	var property = curFilter['property'];
	var type = curFilter['typeLabel'];
	var unit = curFilter['unit'];
	var json = '{"type":"' + type + '",' +
	'"property":"' + property + '",' +
	'"unit":"' + unit + '",';
	if(type == 'Int' || type == 'Double' || type == 'Date') {
		var min = $('#inputMin-' + selected).val();
		var max = $('#inputMax-' + selected).val();
		if(min == '') {
			min = $('#inputMin-' + selected).attr('placeholder');
			$('#inputMin-' + selected).val(min);
		}
		if(max == '') {
			max = $('#inputMax-' + selected).attr('placeholder');
			$('#inputMax-' + selected).val(max);
		}
		if(min > max) {
			$('#inputMin-' + selected).val(max);
			$('#inputMax-' + selected).val(min);
			var tmp = max;
			max = min;
			min = tmp;
		}
		json += '"min":"' + min + '",';
		json += '"max":"' + max + '"}';
	}
	else if(type == 'Plain') {
		var plain = $('#inputPlain-' + selected).val();
		json += '"plain":"' + plain + '"}';
	}
	return json;
	
}

function submitQuery(query, isBool) {
	if(query.trim() != '') {
		resetAll();
		hideResult();
		if(isBool) {
			$.getJSON('search',{q: query, bool: 'true'}, function callback(json) {
				console.log(json);
				parseResult(json);
			});
		}
		else {
			$.getJSON('search',{q: query}, function callback(json) {
				console.log(json);
				parseResult(json);
			});
		}
	}
}


function filterByProperty() {
	var properyName = this.text;
	var keywords = $.parseJSON(decodeURIComponent(query));
	// var keywords = $.parseJSON(query);
	//remove all the class filters
	for(var i = keywords.length - 1; i >= 0; i--)  {
		var value = keywords[i];
		if(value.indexOf('P:') === 0 || value.indexOf('p:') === 0) {
			keywords.splice(i, 1);

		}
	}
	$.each(selectedPropertyFilter, function(index, val) {
		var curFilter = propertyFilterArray[val];
		
		var label = curFilter['propertyLabel'];
		var min = $('#inputMin-' + val).val();
		var max = $('#inputMax-' + val).val();
		if(min.trim() == '' && max.trim() == '') {
			keywords.push('P:' + label);
		}
		else {
			keywords.push('P:' + label + '=[' + min + ',' + max + ']');
		}
	});
	query = arrayToJSON(keywords);
	location.href = searchPrefix + query;
}


function filterBySource() {
	var keywords = $.parseJSON(decodeURIComponent(query));
	// var keywords = $.parseJSON(query);
	//remove all the class filters
	for(var i = keywords.length - 1; i >= 0; i--)  {
		var value = keywords[i];
		if(value.indexOf('S:') === 0 || value.indexOf('s:') === 0) {
			keywords.splice(i, 1);
		}
	}
	var checked = $('#sourceFilterPane input:checked');	
	$.each(checked, function(index, val) {
		source = $(val).val();
		keywords.push('S:' + source);
	});
	query = arrayToJSON(keywords);
	location.href = searchPrefix + query;
}

function arrayToJSON(array) {
	var jsonStr = '[';
	$.each(array, function(index, value) {
		jsonStr += '"' + value + '",';
	});
	jsonStr = jsonStr.substring(0, jsonStr.length - 1);
	jsonStr += ']';
	return jsonStr;
}

function setResultInfo() {
	var keywords = $.parseJSON(decodeURIComponent(query));
	// var keywords = $.parseJSON(query);
	var plainKeys = new Array();
	var classKeys = new Array();
	var propertyKeys = new Array();
	var sourceKeys = new Array();
	$.each(keywords, function(index,value) {
		value = value.replace(/(^\s*)|(\s*$)/g, "");
		if(value.indexOf('P:') === 0 || value.indexOf('p:') === 0) {
			propertyKeys.push(value);
			var splitFrom = value.indexOf('=');
			if(splitFrom > 0) {
				var propLabel = value.substring(2, splitFrom);
				var range = value.substring(splitFrom + 2, value.length - 1);
				var splitted = range.split(",");
				var min = splitted[0];
				var max = splitted[1];
				var index = $('code:contains("'+ propLabel +'")').attr('id').split("-")[1];
				selectPropertyFilter(index);
				$('#inputMin-' + index).val(min);
				$('#inputMax-' + index).val(max);
			}
				
		}
		else if(value.indexOf('C:') === 0 || value.indexOf('c:') === 0) {
			classKeys.push(value);
		}
		else if(value.indexOf('S:') === 0 || value.indexOf('s:') === 0) {
			sourceKeys.push(value);
		}
		else {
			plainKeys.push(value);
		}
	});
	var keysStr = '';
	if(isBool) {
		keysStr = decodeURIComponent(query);
	}
	else {
		var keysArray = new Array();
		if(plainKeys.length != 0) {
			keysArray.push(plainKeys);
		}
		if(classKeys.length != 0) {
			keysArray.push(classKeys);
		}
		if(propertyKeys.length != 0) {
			keysArray.push(propertyKeys);
		}
		if(sourceKeys.length != 0) {
			keysArray.push(sourceKeys);
		}			
		$.each(keysArray, function(index, value) {
			if(index == keysArray.length - 1) {
				keysStr += keysWithLogic(value);
			}
			else {
				keysStr += keysWithLogic(value) + ' AND ';
			}
		});
	}
	$('#queriedString').html(' ' + keysStr + ' ');
}

function keysWithLogic(keys) {
	var keysStr = '(';
	$.each(keys, function(index,value) {		
		if(index == keys.length - 1) {
			keysStr += value;
		}
		else {
			keysStr += value + ' OR ';
		}
	});
	keysStr += ')';
	return keysStr;
}

function resetAll() {
	resultArray = new Array();
}

function hideResult() {
	$('#progressBar').fadeIn();
	$('#resultPane').hide();
	$('#resultInfo').hide();
	$('#recommendPane').hide();
	$('#facetFilterPane').hide();
	$('#intelPane').hide();
}

function showResult() {
	$('#progressBar').hide();
	$('#resultPane').fadeIn();
	$('#resultInfo').fadeIn();
	if(isBool == null || isBool == '') {
		//$('#recommendPane').fadeIn();
		$('#facetFilterPane').fadeIn();
	}
	
	$('#facetFilterPane').css('display','block');
	$('#left').css('display','block');
	$('#middle').css('float','left');
}

function parseResult(json) {
	var time = json['time'];
	var size = json['size'];
	time = roundDouble(time);
	setTimeAndSize(time, size);
	if(size === 0) {
		//No result found
		showNotFound();
	}
	// else if(size == 1) {
		//Only 1 result found
		// location.href=instanceViewerURL + json['result'][0]['uri'];
	// }
	else {
		resultArray = json['result'];		
		constructPagination(size);
		displayPage(1);
		constructRecommendList(json['recommend']);
		showResult();		
		//constructIntelList(json['intel']);
		constructFacetFilter(json['filterOption']);
		constructSourceFilter(json['sources']);
		setResultInfo();
	}
}

function roundDouble(num) {
	var rounded = '' + num;
	if(rounded.split('.').length == 2) 
		rounded = rounded.split('.')[0] + '.' + rounded.split('.')[1].substring(0,3);
	return rounded;
}

function constructFacetFilter(option) {
	if(option['class']) {
		$('#classFilterPane').show();
		// $('#propertyFilterPane').hide();
		constructClassFilter(option['class']['classTree']);
	}
	if(option['property']){
		// $('#classFilterPane').hide();
		$('#propertyFilterPane').show();
		constructPropertyFilter(option['property']['array']);
	}
}



function constructIntelList(intelList) {
	var ulElement = $('#intelList');
	$.each(intelList, function(index, val) {
		var liElement = constructIntel(index, val);
		ulElement.append(liElement);
	});
	if(intelList.length > 0) {
		$('#intelPane').fadeIn();
	}
	else {
		$('#intelPane').hide();
	}
}





function constructIntel(index, intel) {
	var liElement = $('<li>');
	var intelContent = intel['content'];
	var intelTime = intel['time'];
	var intelDate = intel['date'];
	var intelSnippet = constructIntelSnippet(intelContent);
	liElement.html('<strong>[' + intelDate + ']</strong> ' + intelContent);
	
	var aElement = $('<a href="#myModal' + index + '" role="button" data-toggle="modal">查看更多</a>');
	
	var modalElement = $('<div>', {
		id: 'myModal' + index,
		class: 'modal hide fade',
		tabindex: '-1',
		role: 'dialog',
		'aria-labelledby': 'myModalLabel',
		'aria-hidden': 'true'
	});
	
	var modalHeader = $(  '<div class="modal-header">'+
		'<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>'+
		'<h3 id="myModalLabel">相关情报</h3>'+
		intelDate + ' ' + intelTime + 
		'</div>').appendTo(modalElement);
	var modalBody = $(  '<div class="modal-body">'+
		'<p>' + intelContent + '</p>'+
		'</div>').appendTo(modalElement);
	var modalFooter = $('<div class="modal-footer">' +
		'<button class="btn" data-dismiss="modal" aria-hidden="true">关闭</button>' +
//	'<button class="btn btn-primary">Save changes</button>' +
'</div>').appendTo(modalElement);
	
	liElement.append(aElement);
	liElement.append(modalElement);	

	return liElement;
}


function constructIntelSnippet(content) {
	var startIndex = content.indexOf('<span class="hitted">');
	var endIndex = content.indexOf('</span>');
	var snippetLength = endIndex - startIndex + 20;
	var snippet;
	if(startIndex > 0) {
		startIndex -= 1;
	}
	snippet = content.substr(startIndex, snippetLength) + '...';
	if(startIndex > 1) {
		snippet = '...' + snippet;
	}

	return snippet;
}

function showNotFound() {
	$('#progressBar').hide();
	$('#resultPane').html('Nothing found!');
	$('#resultPane').fadeIn();
	$('#resultInfo').fadeIn();
	$('#recommendPane').hide();
	$('#facetFilterPane').hide();
	$('#intelligencePane').hide();
}

function displayPage(pageNum) {
	constructResultList(pageNum);
}

function constructResultList(pageNum) {
	var startIndex = (pageNum - 1) * 10;
	var endIndex = startIndex + 10;	
	if(endIndex > resultArray.length) {
		endIndex = resultArray.length;
	}
	var resultPane = $('#resultPane');
	var resultList = $('<ul>');
	for(var i = startIndex; i < endIndex; i++) {
		var liElement = constructResult(resultArray[i]);
		resultList.append(liElement);
	}
	resultPane.html(resultList);
}

function constructResult(result) {
	var liElement = $('<li>', {
		class: 'resultLi'
	});
	liElement.css('background-image', 'url('+ result['image']+')');

	var labelText = result['label'];
	if(labelText.split(';').length > 1) {
		labelText = labelText.split(';')[0] + ';...';
	}
	var labelElement = $('<a>', {
		title: result['uri'],
		text: labelText,
		href: instanceViewerURL + result['uri'],
		class: 'resultLabel'
	});

	var typeElement = $('<span>', {
		title: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type',
		text: 'type：',
//		text: 'Type:',
		class: 'typeSpan'
	});
	var sourceElement = $('<span>', {
		//text: '数据源：' + result['source'] + ' 来源表：' + result['tableName'] ,
		text: 'dataset：' + result['source'] ,
//		text: 'Data Source:' + result['source'],
		class: 'sourceSpan text-muted'	
	});
	var aElement = $('<span>',{
		title: result['type'][0],
		text: result['typeLabel']
	});
	typeElement.append(aElement);
	var snippetElment = constructSnippet(result['snippet']);
	var propSnippetElement = constructPropSnippet(result['propSnippet']);
	liElement.append(labelElement);
	liElement.append($('<br>'));
	liElement.append(typeElement);
	liElement.append(propSnippetElement);
	if(result['snippet'] != null) {
		liElement.append($('<br>'));
		liElement.append(snippetElment);
	}
	liElement.append($('<br>'));
	liElement.append(sourceElement);
	return liElement;
}

function constructSnippet(snippet) {
	var spanElement = $('<span>');
	if(!snippet) return spanElement;
	var splitted = snippet.split('<br/>');
	for(var i = 0; i < splitted.length; i++) {
		var cur = splitted[i];
		if(i != 0)
			spanElement.append('<br/>');
		if(cur.length < 100) {
			spanElement.append(cur);
			console.log(cur);
		} else {
			var prop = cur.split('：')[0];
			var value = cur.split('：')[1];
//			var prop = cur.split(':')[0];
//			var value = cur.split(':')[1];
			console.log(cur);
			console.log(prop);
			console.log(value);
			var firstIndex = value.indexOf('<span class="hitted">');
			spanElement.append(prop+'：...'+value.substring(firstIndex, firstIndex + 50) + ' ...');
//			spanElement.append(prop+':...'+value.substring(firstIndex, firstIndex + 50) + '...');
		}
	}
	return spanElement;
}

function constructPropSnippet(propSnippet) {
	var spanElement = $('<span>');
	$.each(propSnippet, function(index, val) {
		var snippetElement = $('<span>').html(val['snippet']);	
		spanElement.append($('<br>'));
		spanElement.append(snippetElement);
	});
	return spanElement;
}

function constructRecommendList(recoArray) {
	var recommendPane = $('#recommendPane');
	var recommendList = $('<ul>');
	var recoSize = (recoArray.length > g_maxRecoSize) ? g_maxRecoSize : recoArray.length;	
	for(var i = 0; i < recoSize; i++) {
		var liElement = constructRecommend(recoArray[i]);
		recommendList.append(liElement);
	}
	recommendPane.append(recommendList);
	if(recoArray.length > 0) {
		recommendPane.fadeIn();
	}
	else {
		recommendPane.hide();
	}
}

function constructRecommend(recommend) {
	var liElement = $('<li>');
	var aElemet = $('<a>', {
		text: recommend['label'],
		href: instanceViewerURL + recommend['uri']
	}).appendTo(liElement);
	liElement.append($('<br>'));
	var spanElement = $('<span>', {
		text: recommend['typeLabel'],
		class: 'typeSpan'
	}).appendTo(liElement);
	liElement.append($('<br>'));
	var reasonSpanElement = $('<span>', {
		text: recommend['reason'],
	}).appendTo(liElement);
	return liElement;
}
/*<label class="checkbox">
							  <input type="checkbox" id="inlineCheckbox1" value="option1">nju28_3
							</label>*/
function constructSourceFilter(sourceList) {
	var sourceFilterPane = $('#sourceFilterPane');
	$.each(sourceList, function(index, val) {
		var labelElement = $('<label>', {
			class: 'checkbox'	
		});
		labelElement.css({"margin-left":"40px"});
		var inputElement = $('<input>', {
			type: 'checkbox',
			id: 'checkbox',
			value: val['source'],
		});
		if(val['count'] == 0) {
			// inputElement.attr('disabled', '');
		}
		else {
			inputElement.attr('checked', 'checked');
		}
		labelElement.append(inputElement);
		labelElement.append(val['source'] + " (" + val['count'] + ")");
		//labelElement.append(val['source']);
		sourceFilterPane.append(labelElement);
	});
	var btnElement = $('<button>', {
		class: 'btn-sm btn-warning'
	});
	btnElement.append($('<span>').html('filter by datasets'));
	btnElement.css({"margin-left":"20px"});
	btnElement.click(filterBySource);
	sourceFilterPane.append(btnElement);
}

function check() {
	var checked = $('#sourceFilterPane input:checked');	
	$.each(checked, function(index, val) {
		console.log($(val).val());	
	});
}
function constructClassFilter(classTree) {
	var classFilterPane = $('#classFilterPane');		
	var classList = $('<ul>');
	$.each(classTree, function(index, val) {
		classList.append(constructClassTreeNode(val));
	});
	classFilterPane.append(classList);
	classFilterPane.show();
}

function constructClassTreeNode(treeNode) {
	var liElement = $('<li>');
	var className = treeNode['label'];
	var uri = treeNode['uri'];
	var aElement = $('<a>', {
		title: uri,
		text: className,
		href: filterByClass(className)
	});
	//aElement.bind('click', filterByClass);
	
	if(className.indexOf('others') < 0)
		liElement.append(aElement);
	else liElement.append($('<span>').html(className));
	if(className.charAt(className.length) != ' ') {
		liElement.append(' ')
	}
	liElement.append('(' + treeNode['count'] + ')');
	if(treeNode['subTree']) {
		var ulElement = $('<ul>');
		$.each(treeNode['subTree'], function(index, val) {
			ulElement.append(constructClassTreeNode(val));
		});
		liElement.append(ulElement);
	}
	return liElement;
}

function filterByClass(className) {
	var keywords = $.parseJSON(decodeURIComponent(query));
	// var keywords = $.parseJSON(query);
	//remove all the class filters
	for(var i = keywords.length - 1; i >= 0; i--)  {
		var value = keywords[i];
		if(value.indexOf('C:') === 0 || value.indexOf('c:') === 0) {
			keywords.splice(i, 1);

		}
	}
	keywords.push('C:' + className);
	return searchPrefix + arrayToJSON(keywords);
}


function constructPropertyFilter(propArray) {
	propertyFilterArray = propArray;
	console.log(propArray);
	var tagPane = $('#propertyFilterTagPane');
	var inputPane = $('#propertyFilterInputPane');
	$.each(propertyFilterArray, function(index, value) {
		var curProperty = value['propertyLabel'];
		var tag = constructPropertyFilterTag(curProperty, index);
		var input = constructPropertyFilterInput(index);
		tagPane.append(tag);
		inputPane.append(input);
	});
}

function constructPropertyFilterTag(property, index) {
	var tag = $('<code>', {
		text: property,
		id: 'prop-' + index
	});
	tag.bind('click',function() {
		var index = this.id.split('-')[1];
		selectPropertyFilter(index);		
	});
	return tag;
}

function selectPropertyFilter(index) {
	$('#prop-' + index).fadeOut();						
	$('#input-' + index).fadeIn();
	selectedPropertyFilter.push(index);
	if(selectedPropertyFilter.length != 0) {
		$('#filterBtnPane').show();
	}		
}

function deselectPropertyFilter(index) {
	$('#prop-' + index).fadeIn();						
	$('#input-' + index).hide();
	for(var i = 0; i < selectedPropertyFilter.length; i++) {
		if(selectedPropertyFilter[i] == index) {				
			selectedPropertyFilter.splice(i,1);
		}
	}		
	if(selectedPropertyFilter.length == 0) {
		$('#filterBtnPane').hide();
	}		
}

function constructPropertyFilterInput(index) {
	var curPropertyFilter = propertyFilterArray[index];
	var type = curPropertyFilter['typeLabel'];
	var input = constructIntervalInput(curPropertyFilter, index);
	if(type === 'Int' || type === 'Double' || type === 'Date') {
		input = constructIntervalInput(curPropertyFilter, index);
	}
	else if(type === 'Plain') {
		input = constructSelectInput(curPropertyFilter, index);
	}
	return input;
}

function constructIntervalInput(filter, index) {
	var divElement = $('<div>', {
		class: 'filterInputDiv row-fluid',
		id: 'input-' + index
	});
	var	propertyElement = $('<span>', {
		class: 'span3',
		style: 'font-size: 12px',
		text: filter['propertyLabel']
	});
	var inputDiv = $('<div>', {
		class: 'span8',
		style: 'margin-top: -14px'
	});
	var minElement = $('<input>', {
		type: 'text',
		class: 'filterInput',
		id: 'inputMin-' + index,
		placeholder: filter['min']
	});	
	var maxElement = $('<input>', {
		type: 'text',
		class: 'filterInput',
		id: 'inputMax-' + index,
		placeholder: filter['max']
	});
	inputDiv.append(minElement);
	inputDiv.append('-');
	inputDiv.append(maxElement);
	var removeElement = $('<a>', {				
		class: 'span1',
		id: 'remove-' + index
	});
	var iconElment = $('<li>', {
		class: 'icon-remove'
	}).appendTo(removeElement);
	removeElement.bind('click', function() {
		var index = this.id.split('-')[1];
		deselectPropertyFilter(index);
	});
	divElement.append(propertyElement);
	divElement.append(inputDiv);
	divElement.append(removeElement);
	return divElement;
}

function constructSelectInput(filter, index) {
	var divElement = $('<div>', {
		class: 'filterInputDiv row-fluid',
		id: 'input-' + index
	});
	var	propertyElement = $('<span>', {
		class: 'span3',
		style: 'font-size: 12px',
		text: filter['propertyLabel']
	});
	var inputDiv = $('<div>', {
		class: 'span8',
		style: 'margin-top: -14px'
	});
	var selectElement = $('<select>', {
		style: 'font-size: 12px',
		class: 'filterSelect',
		id: 'inputPlain-' + index
	});
	inputDiv.append(selectElement);
	var plainArray = filter['plain'];	
	$.each(plainArray, function(index, value) {
		var option = $('<option>').html(value);
		selectElement.append(option);
	});
	var removeElement = $('<a>', {	
		class: 'span1',
		id: 'remove-' + index
	});
	var iconElment = $('<li>', {
		class: 'icon-remove'
	}).appendTo(removeElement);

	removeElement.bind('click', function() {
		var index = this.id.split('-')[1];
		deselectPropertyFilter(index);
	});
	divElement.append(propertyElement);
	divElement.append(inputDiv);
	divElement.append(removeElement);
	return divElement;
}





function constructPagination(sumResultNum) {
	if(sumResultNum % 10 == 0)
		sumPageNum = Math.floor(sumResultNum / 10);
	else
		sumPageNum = Math.floor(sumResultNum / 10) + 1;
		$('#paginate').paginate({
						count 		: sumPageNum,
				start 		: 1,
				display     : 15,
				border					: true,
				border_color			: '#DDDDDD',
				border_hover_color		: '#DDDDDD',
				text_color  			: '#0088CC',
				text_hover_color		: '#999',
				background_color    	: 'none',	
				background_hover_color	: 'none', 
				images		: false,
				rotate		: false,
				mouse		: 'press',
                onChange    : function(page) {
                				displayPage(page);
                            }
    });
	
}


function setTimeAndSize(time, size) {
	$('#queryTime').html(time);
	$('#resultSize').html(size);
}
