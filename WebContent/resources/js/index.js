$(document).ready(init);
function init() {			
	 initSearchPane();
	 initSampleQueries();
}

function initSampleQueries() {
	$('a.label').bind('click', sampleQueryHandler);
}

function sampleQueryHandler() {
	addTagToSearchInput(this.text);
}

function disableSearchBtn() {
	$('#searchBtn').button('loading');
}

function enableSearchBtn() {
	$('#searchBtn').button('reset');
}