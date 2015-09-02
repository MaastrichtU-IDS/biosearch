$(document).ready(init);
var segmentServiceURL = '/Semantic-Annotation/SegmentService';
function init() {			
	 initSearchPane();
	 initSampleQueries();
	 //initSegmentService();
}

function initSampleQueries() {
	$('a.label').bind('click', sampleQueryHandler);
}

function sampleQueryHandler() {
	addTagToSearchInput(this.text);
}

function initSegmentService() {
	disableSearchBtn();
	$.ajax({
		url: segmentServiceURL,
		type:'get',
		data: {raw: 'test'},
		statusCode: {
			200: function callback(resp) {				
				enableSearchBtn();
			}
		}
	});
}

function disableSearchBtn() {
	$('#searchBtn').button('loading');
}

function enableSearchBtn() {
	$('#searchBtn').button('reset');
}