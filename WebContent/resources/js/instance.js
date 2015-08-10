$(document).ready(initPage);
var uri;
var label;
var type;
var img;
var traceArray;
var instancePrefix = 'instance.html?inst=';
var searchPrefix = 'query.html?q=';

function initPage() {
	initSearchPane();
	requestByParam();	
	// initViewOptions();	
	$('#menu2').menuTree();
}

function initViewOptions() {
	$('#viewOption code').bind('click', function() {
		if($(this).attr('class') == 'selected') {
			$(this).removeClass();
		}
		else {
			$(this).addClass('selected');
		}
	});
}

function requestByParam() {
	uri = getParam('inst');
	requestForInstance(uri);
}

function getParam(paramName) {
	var params = location.href.split('?')[1];
	var paramsArray = params.split('&');
	for(var i = 0; i < paramsArray.length; i++) {
		var curParam = paramsArray[i];
		if(curParam.split('=')[0] == paramName) {
			return curParam.substring(curParam.indexOf('=') + 1);
		}
	}
	return null;
}

function requestForInstance(uri) {
	$.getJSON('instance', {instURI: uri}, function callback(json) {
//		console.log(json);
		parseResult(json);
	});
}

function parseResult(json) {
	if(!json['label'])
		window.location.href = uri;
	var sentenceArray = json['pvarray'];
	//label = json['result'][0]['label'];
	label = json['label'];
	type = json['typeLabel'];
	img = json['img'];
	constructInstancePane(sentenceArray);
	console.log(json);
	constructRecommendList(json['recommend']);
	constructCoreferenceList(json['coreference']);
	//constructIntelList(json['intel'], json['intel2']);
	constructReferenceList(json['reference']);
	getTraceFromCookie();
	setTraceToCookie();
	constructBreadcrumb();
	constructRDFOutput();
}

function constructInstancePane(sentenceArray) {
	var imgElement = constructInstanceImage();
	constructSentenceTree(sentenceArray);
	
	$('#imgPane').append(imgElement);
	$('#propTree').menuTree( {
		multiOpenedSubMenu: true
	});
}

function constructInstanceImage() {
	var prefix = 'img/instance/';
	var curInstance = uri.substring(uri.indexOf('nju28/') + 6);
	var instanceType = curInstance.split('/')[0];
	var instanceId = curInstance.split('/')[1].split('=')[1];
	//var instanceImgURL = prefix + instanceType + '_' + instanceId + '.jpg';
	var instanceImgURL = img;
	var imgExisted = false;
	var imgElement = imgElement = $('<img>', {
						id: 'instanceImg',
						src: instanceImgURL,
						class: 'img-rounded col-md-12'
				});
	$('#instanceImg').attr('src', instanceImgURL);
	$('#instanceImg').addClass('col-md-12');
	
	if(instanceImgURL) {
		$('#imgPane').css('display','block');
		$('#left').css('display','block');
		$('#middle').css('float','left');
	} 
	return imgElement;
}

function constructSentenceTree(tree) {
	var ulElement = $('#ulProps');
	ulElement.append($('<h3>'+label+'</h3>'));
	
	appendGroups(ulElement);
	
	if(tree['property'] == 'Property' && tree['subtree']) {
		$.each(tree['subtree'], function(index, val) {
			var subtreeLiElement = constructTreeNode(val);
			liAppend(subtreeLiElement, tree['subtree'][index]['URI'], tree['subtree'][index]['isObject'], tree['subtree'][index]['property']);
//			console.log(tree['subtree'][index]['namespace'])
//			ulElement.append(subtreeLiElement);
		});
	}
}

function liAppend(subtreeLiElement, ns, isObject, prop) {
	if(ns.indexOf('http://bio2rdf.org/') >= 0 && ns.indexOf('x-') >= 0) {
		if(prop.indexOf('is-x-') >= 0) {
			$('#GXIP').append(subtreeLiElement);
			$('#liGXIP').css('display', 'block');
		} else {
			$('#GXOP').append(subtreeLiElement);
			$('#liGXOP').css('display', 'block');
		}
	} else {
		switch(prop) {
		case 'label':
			$('#ulLabel').append(subtreeLiElement);
			break;
		case 'title':
			$('#ulTitle').append(subtreeLiElement);
			break;
		case 'uri':
			$('#ulUri').append(subtreeLiElement);
			break;
		case 'identifier':
			$('#ulId').append(subtreeLiElement);
			break;
		case 'type':
			$('#ulType').append(subtreeLiElement);
			break;
		case 'description':
			$('#ulDes').append(subtreeLiElement);
			break;
		case 'seeAlso':
			$('#ulSeeAlso').append(subtreeLiElement);
			break;
		case 'namespace':
			$('#ulDataset').append(subtreeLiElement);
			break;
		default:
			if(isObject == "true") {
				$('#GOP').append(subtreeLiElement);
				$('#liGOP').css('display', 'block');
			} else if(isObject == "false") {
				$('#GDP').append(subtreeLiElement);
				$('#liGDP').css('display', 'block');
				$('#liGOP').css('display', 'block');
			}
			break;
		}
	}
}

function constructTreeNode(treeNode) {
	var liElement = parseValue(treeNode['URI'], treeNode['property'], treeNode['value']);
	if(treeNode['subtree']) {
		var subtreeUlElement = $('<ul>');
		$.each(treeNode['subtree'], function(index, val) {
			var subtreeLiElement = constructTreeNode(val);
			subtreeUlElement.append(subtreeLiElement);
		});
		liElement.append(subtreeUlElement);
	}

	return liElement;
}

function parseValue(URI, prop, value) {
	var valueHTML = $('<li>');
	valueHTML.css('list-style-type', 'none');
	if(value) {
		if(value['single']) {
			value['single']= dropTail(prop, value['single']);
			prop = dropMark(prop, value['single']);
//			valueHTML.append($('<span>').html('<a href=' + URI + '><strong>' + prop  + '</strong></a>：' + value['single']));
			valueHTML.append($('<span>').html('<strong title=' + URI + '>' + prop  + '</strong>：' + value['single']));
			valueHTML.addClass('child');
		}
		else if(value['multi']) {
			var aElement = $('<a>', {
				class: 'aExpande'
			});
			prop = dropMark(prop, value['multi'][0]);
			aElement.html('<strong title=' + URI + '>' + prop + '</strong>');
			aElement.css('color', '#777777');
			var ulElement = $('<ul>');
			$.each(value['multi'], function(index, value) {
				if(index == 0) return true;
				var liElement = $('<li>').addClass('child');
				liElement.css('list-style-type', 'none');
//				var anchor = $('<a>', {
//					href:instancePrefix + value['uri'],
//				});
//				anchor.html(value['label']);
				value = dropTail(prop, value);
//				anchor.bind('click', function () {
//					setTraceRelation('装备了');
//				});
//				liElement.html(anchor);
				liElement.append($('<span>').html(value));
				ulElement.append(liElement);
			});
			ulElement.css('display', 'none');
			valueHTML.append(aElement);
			valueHTML.append($('<span>').html('：' + dropTail(prop, value['multi'][0]) + ' ...'));
			valueHTML.append(ulElement);
			valueHTML.addClass('parent collapsed');
		}			
	}
	else {
		valueHTML.addClass('parent expanded');
		valueHTML.append($('<a>').html(prop));
	}
	return valueHTML;
}

function constructSentenceList(array) {
	var ulElement = $('<ul>', {
						class: 'col-md-6'
						});
	ulElement.append($('<h2>'+label+'</h2>'));
	ulElement.append($('<span>',{
		text: type,
		class: 'instType typeSpan'
	}));
	for(var i = 0; i < array.length; i++) {
		constructSentence(array[i]).appendTo(ulElement);
	}
	return ulElement;
}

function constructSentence(sentence) {
	var value = sentence['value'];
	var property = sentence['prop'];
	var liElement = liElement = $('<li>');
	if(value != null) {
		liElement.html('<strong>' + property  + '</strong>：' + roundDouble(value));
	}
	else {
		var values = sentence['values'];
		liElement.html(property + '：');
		var ulElement = $('<ul>');
		$.each(values, function(index, value) {
			var uri = value.split('\t')[0];
			var label = value.split('\t')[1];
			var count = value.split('\t')[2];
			var insideLi = $('<li>');
			var aElement = $('<a>', {
							href: instancePrefix + uri,
							text: label
							});
			insideLi.append(aElement);
			insideLi.append('（' + count + '）');
			ulElement.append(insideLi);
		});
		liElement.append(ulElement);
	}
	
	return liElement;
}

function roundDouble(num) {
	var rounded = '' + num;
	if(rounded.split('.').length == 2) {
		rounded = rounded.split('.')[0] + '.' + rounded.split('.')[1].substring(0,3);
	}
	return rounded;
}

function constructReferenceList(refList) {
	var ulElement = $('#intelList');
	$.each(refList, function(index, val) {
		var liElement = constructReference(index, val);
		ulElement.append(liElement);
	});
	if(refList.length > 0) {
		$('#intelPane').fadeIn();
	}
	else {
		$('#intelPane').hide();
	}
}

function constructReference(index, ref) {
	var id = ref['id'];
	var liElement = $('<li>');
	liElement.hide();	
	$.getJSON('http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&retmode=json&id=' + id, function (summary) {
		var citation = '';
		for(author in summary.result[id].authors){
			citation += summary.result[id].authors[author].name+', ';
		}
		citation +='<a href="http://www.ncbi.nlm.nih.gov/pubmed/' + id + '" target="_blank">'+summary.result[id].title+'</a>';
		citation += '\" <i>' + summary.result[id].fulljournalname + '</i> ';
		citation += summary.result[id].volume + '.' + summary.result[id].issue;
		citation += ' (' + summary.result[id].pubdate + '): ';
		citation += summary.result[id].pages + '.';
		liElement.append(citation);
		var aElement = $();
		liElement.show();
	
	});
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

function constructCoreferenceList(corefArray) {
	var corefPane = $('#coreferencePane');
	var corefList = $('<ul>');
	for(var i = 0; i < corefArray.length; i++) {
		var liElement = constructCoreference(corefArray[i]);
		corefList.append(liElement);
	}
	corefPane.append(corefList);
	if(corefArray.length > 0) {
		corefPane.fadeIn();
	}
	else {
		corefPane.hide();
	}
}

function constructCoreference(coref) {
	var instanceViewerURL = 'instance.html?inst=';
	var liElement = $('<li>');
	var aElement = $('<a>', {
						text: coref['label'],
						href: instanceViewerURL + coref['uri']
					}).appendTo(liElement);
	aElement.click(function () {
		setTraceRelation('Coreferent');
	})
	liElement.append($('<br>'));
	var spanElement = $('<span>', {
						text: coref['source'],
						class: 'typeSpan'
						}).appendTo(liElement);
	liElement.append($('<br>'));
	return liElement;
}

function constructRecommendList(recoArray) {
	var recommendPane = $('#recommendPane');
	var recommendList = $('<ul>');
	for(var i = 0; i < recoArray.length; i++) {
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
	var instanceViewerURL = 'instance.html?inst=';
	var liElement = $('<li>');
	var aElement = $('<a>', {
						text: recommend['label'],
						href: instanceViewerURL + recommend['uri']
					}).appendTo(liElement);
	aElement.click(function () {
		setTraceRelation(recommend['reason']);
	})
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

function setTraceRelation(relation) {
	var lastTrace = traceArray.pop();
	lastTrace['rel'] = relation; 
	traceArray.push(lastTrace);
	persistTrace();
	console.log($.cookie('trace'));
}

function getTraceFromCookie() {
	var traceStr = $.cookie('trace');
	traceArray = $.parseJSON(traceStr);
}

function setTraceToCookie() {	
	for(var i = 0; i < traceArray.length; i++) {		
		if(uri === traceArray[i]['uri']) {
			traceArray = traceArray.splice(0, i + 1);
			persistTrace();
			return;
		}
	}	
	var curTraceURI = uri;
	var curTraceLabel = label;
	var trace = {'label': curTraceLabel,
				'uri': curTraceURI};
	traceArray.push(trace);
	persistTrace();
}

function persistTrace() {
	var traceJSON = JSON.stringify(traceArray);
	$.cookie('trace', traceJSON);
}

function constructBreadcrumb() {
	var ulElement = $('#breadcrumb');
	$.each(traceArray, function(index, value) {
		var liElement;
		if(index === traceArray.length - 1) {
			liElement = $('<li>', {
							class: 'active',
							text: value['label']
						});
		}
		else {		
			liElement = $('<li>');
			var aElement; 
			if(index === 0) {
				var queried = decodeURIComponent(value['label'])
				aElement = $('<a>', {
							text: queried,
							href: searchPrefix + value['uri']
							});
				setSearchInput(queried);
			}
			else {
				aElement = $('<a>', {
							text: value['label'],
							href: instancePrefix + value['uri']
							});				
			}
			liElement.append(aElement);
			if(value['rel']) {
				var relSpanElement = $('<span>').html(' ' + value['rel']);
				relSpanElement.addClass('traceRel');
				liElement.append(relSpanElement);
			}
			/*
			var spanElement = $('<span class="divider">></span>');
			liElement.append(spanElement);
			*/
		}
		ulElement.append(liElement);
	});
}

function constructRDFOutput() {
	var divElement = $('#rdfoutput');
	var centerElement = $('<center>').appendTo(divElement);
	var spanElement = $('<span>', {
		text: 'Export as: '
	}).appendTo(centerElement);
	
	var uriLN = getLocalName(uri);
	var openlifedataPre = 'http://openlifedata.org/';
	
	
	var NTriElement = $('<a>', {
		text: 'N-Triples',
		title: openlifedataPre + uriLN + "&format=n-triples&view=true",
		href: openlifedataPre + uriLN + "&format=n-triples&view=true"
	});
	
	var TurtleElement = $('<a>', {
		text: 'Turtle',
		title: openlifedataPre + uriLN + "&format=turtle&view=true",
		href: openlifedataPre + uriLN + "&format=turtle&view=true"
	});
	
	var JSONElement = $('<a>', {
		text: 'JSON',
		title: openlifedataPre + uriLN + "&format=rdf/json&view=true",
		href: openlifedataPre + uriLN + "&format=rdf/json&view=true"
	});
	
	var XMLElement = $('<a>', {
		text: 'RDF/XML',
		title: openlifedataPre + uriLN + "&format=rdf/xml&view=true",
		href: openlifedataPre + uriLN + "&format=rdf/xml&view=true"
	});
	
	centerElement.append('[');
	centerElement.append(NTriElement);
	centerElement.append('], [');
	centerElement.append(XMLElement);
	centerElement.append('], [');
	centerElement.append(TurtleElement);
	centerElement.append('], [');
	centerElement.append(JSONElement);
	centerElement.append(']');
}

function setSearchInput(queried) {
	var keyArray = $.parseJSON(queried);
	$.each(keyArray, function (index, value) {
		addTagToSearchInput(value);
	});
}

function appendGroups(ulElement) {
	var liElementGRDF = $('<li>', {
		id: 'liGRDF'
	});
	liElementGRDF.css('list-style-type', 'none');
	liElementGRDF.css('display', 'block');
	liElementGRDF.append($('<hr />'));
	liElementGRDF.append($('<h4>'+' '+'</h4>'));
	var ulElementLabel = $('<ul>', {id: 'ulLabel'}).appendTo(liElementGRDF);
	var ulElementTitle = $('<ul>', {id: 'ulTitle'}).appendTo(liElementGRDF);
	var ulElementUri = $('<ul>', {id: 'ulUri'}).appendTo(liElementGRDF);
	var ulElementId = $('<ul>', {id: 'ulId'}).appendTo(liElementGRDF);
	var ulElementType = $('<ul>', {id: 'ulType'}).appendTo(liElementGRDF);
	var ulElementDes = $('<ul>', {id: 'ulDes'}).appendTo(liElementGRDF);
	var ulElementSeeAlso = $('<ul>', {id: 'ulSeeAlso'}).appendTo(liElementGRDF);
	var ulElementDataset = $('<ul>', {id: 'ulDataset'}).appendTo(liElementGRDF);
	ulElement.append(liElementGRDF);
	
	var liElementGOP = $('<li>', {
		id: 'liGOP'
	});
	liElementGOP.css('list-style-type', 'none');
	liElementGOP.css('display', 'none');
	var ulElementGOP = $('<ul>', {
		id: 'GOP'
	});
	liElementGOP.append($('<hr />'));
	liElementGOP.append($('<h4>'+' '+'</h4>'));
	liElementGOP.append(ulElementGOP);
	ulElement.append(liElementGOP);
	
	var liElementGDP = $('<li>', {
		id: 'liGDP'
	});
	liElementGDP.css('list-style-type', 'none');
	liElementGDP.css('display', 'none');
	var ulElementGDP = $('<ul>', {
		id: 'GDP'
	});
	liElementGDP.append($('<h4>'+' '+'</h4>'));
	liElementGDP.append(ulElementGDP);
	ulElement.append(liElementGDP);
	
	var liElementGXOP = $('<li>', {
		id: 'liGXOP'
	});
	liElementGXOP.css('list-style-type', 'none');
	liElementGXOP.css('display', 'none');
	var ulElementGXOP = $('<ul>', {
		id: 'GXOP'
	});
	liElementGXOP.append($('<hr />'));
	liElementGXOP.append($('<h4>'+' '+'</h4>'));
	liElementGXOP.append(ulElementGXOP);
	ulElement.append(liElementGXOP);
	
	var liElementGXIP = $('<li>', {
		id: 'liGXIP'
	});
	liElementGXIP.css('list-style-type', 'none');
	liElementGXIP.css('display', 'none');
	var ulElementGXIP = $('<ul>', {
		id: 'GXIP'
	});
	liElementGXIP.append($('<hr />'));
	liElementGXIP.append($('<h4>'+' '+'</h4>'));
	liElementGXIP.append(ulElementGXIP);
	ulElement.append(liElementGXIP);
}

function dropTail(prop, value) {
	if(prop == 'label') return value;
	var reg = new RegExp(".*\\[.*\\](</a>)?$");
	if(reg.test(value)) {
		var regg = new RegExp("\\[.*\\](</a>)?$");
		value = value.replace(regg, "</a>");
	}
	return value;
}

function dropMark(prop, value) {
	if(prop.indexOf('x-') < 0) {
		return prop;
	}
	var reg = new RegExp(".*\\([0-9]*\\).*");
	if(reg.test(prop)) {
		var regg = new RegExp("\\([0-9]*\\)")
		prop = prop.replace(regg, "");
		var source;
		if(value.indexOf('drugbank') >= 0) source = 'drugbank';
		else if(value.indexOf('pharmgkb') >= 0) source = 'pharmgkb';
		else if(value.indexOf('omim') >= 0) source = 'omim';
		else if(value.indexOf('kegg') >= 0) source = 'kegg';
		else if(value.indexOf('mesh') >= 0) source = 'mesh';
		else if(value.indexOf('interpro') >= 0) source = 'interpro';
		else if(value.indexOf('ncbigene') >= 0) source = 'ncbigene';
		else if(value.indexOf('orphanet') >= 0) source = 'orphanet';
		prop = "in-link from " + source; 
	} else {
		prop = "out-link to " + prop.substring(2, prop.length);
	}
	return prop;
}

function getLocalName(uri) {
	return uri.substring(uri.lastIndexOf('/')+1, uri.length);
}

