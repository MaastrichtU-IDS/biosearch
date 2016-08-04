package cn.edu.nju.ws.biosearch.servlet;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import cn.edu.nju.ws.biosearch.classTree.ClassTree;
import cn.edu.nju.ws.biosearch.classTree.ClassTreeGenerator;
import cn.edu.nju.ws.biosearch.datasource.DataSourceManager;
import cn.edu.nju.ws.biosearch.facet.FacetService;
import cn.edu.nju.ws.biosearch.ontology.OntManager;
import cn.edu.nju.ws.biosearch.query.DatasetService;
import cn.edu.nju.ws.biosearch.query.QueryEngine;
import cn.edu.nju.ws.biosearch.query.ResultItem;
import cn.edu.nju.ws.biosearch.utils.Config;

/**
 * Servlet implementation class Search
 */
//@WebServlet("/search")
public class Search extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Set<String> reqPlainList;
	private Set<String> reqClassList;
	private Set<String> reqSourceList;
	private Set<String> reqPropList;
	private List<ResultItem> resultList;
	private OntManager om;
	private QueryEngine qe;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Search() {
        super();
    }
    
	@Override
	public void init() throws ServletException {
		super.init();
		Config conf = new Config();
		conf.setParams();
		om = OntManager.getInstance();
		qe = new QueryEngine();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String requestString = request.getParameter("q");
		requestString = deleteSpace(requestString);
		String isBoolean = request.getParameter("bool");
		requestString = URLDecoder.decode(requestString, "UTF-8");
		long start = 0, end = 0;
		if(isBoolean == null || isBoolean.trim().equals("")) {
			requestString = requestString.replaceAll("[\\(\\)]", "");
			parseRequest(requestString);
			start = System.currentTimeMillis();
			resultList = getResultList();
			end = System.currentTimeMillis();
		}
		else {
			String booleanRequest = parseBooleanRequest(requestString);
			parseRequest(requestString);
			start = System.currentTimeMillis();
			resultList = getBooleanQueryResultList(booleanRequest);
			end = System.currentTimeMillis();
		}
		long duration = end - start; 
		JSONObject resultJSON = null;
		if(isBoolean == null || isBoolean.trim().equals("")) {
			
			Set<String> sourcesList = DataSourceManager.getInstance().listSourceNames();//swc.getSourcesOfResults();
			resultJSON = constructResultJSON(resultList, null, sourcesList, duration);
		}
		else {
			resultJSON = constructResultJSON(resultList, null, null, duration);
		}
		response.setContentType("text/json; charset=UTF-8");
		response.getWriter().print(resultJSON);
	}
	
	private String deleteSpace(String str) {
		str = str.replace("%C2%A0", "");
		return str;
	}
	private List<ResultItem> getBooleanQueryResultList(String booleanRequest) {
		// TODO Auto-generated method stub
		return null;
	}

	private String parseBooleanRequest(String requestString) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<ResultItem> getResultList() {
//		System.out.println(reqPropList);
//		System.out.println(reqClassList);
//		System.out.println(reqPlainList);
//		System.out.println(reqSourceList);

		List<ResultItem> results = qe.query(reqPlainList, reqClassList, reqPropList, reqSourceList);
		return results;
	}

	private void parseRequest(String requestString) {
		JSONArray requestArray = (JSONArray)JSONValue.parse(requestString);
		reqPlainList = new HashSet<String> ();
		reqClassList = new HashSet<String> ();
		reqSourceList = new HashSet<String> ();
		reqPropList  = new HashSet<String> ();
		
		for(int i = 0; i < requestArray.size(); i++) { 
			String requestItem = requestArray.get(i).toString();
			parseRequestItem(requestItem);
		}
	}
	
	private void parseRequestItem(String requestItem) {
		if(requestItem.startsWith("c:") || requestItem.startsWith("C:")) {
			String classLabel = requestItem.substring(2);
			String classURI = om.getClassURI(classLabel);
			logger.info(classLabel+" "+classURI);
			
			if(classURI != null)
				reqClassList.add(classURI);
		}
		else if(requestItem.startsWith("s:") || requestItem.startsWith("S:")) {
			String source = requestItem.substring(2);
			reqSourceList.add(source);
		}
		else if(requestItem.startsWith("p:") || requestItem.startsWith("P:")) {
			String[] splited = requestItem.split("=");
			String propLabel = splited[0].substring(2);
			String propURI = om.getPropURI(propLabel);
			if(propURI != null) {
				String range = "";
				if(splited.length == 2) {
					range = splited[1];
					if(range.matches("[\\-0-9\\.]+")) {
						range = "[" + range + "," + range + "]";
					}
				}
				reqPropList.add(propURI);
			}
		}
		else {
			requestItem = requestItem.trim();
			reqPlainList.add(requestItem);
		}
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject constructResultJSON(List<ResultItem> resultList, Set<String> intelList, Set<String> sourcesList, long duration) {
		JSONObject resultJSON = new JSONObject();
		JSONArray resultListJSON = constructResultListJSON(resultList);
		JSONArray sourcesListJSON = constructSourceListJSON(sourcesList);
		JSONObject filterOption = new JSONObject();
		JSONObject classFilterOption = getClassFilterOption(resultList);
		filterOption.put("class", classFilterOption);
		
		resultJSON.put("result", resultListJSON);
		resultJSON.put("size", resultListJSON.size());
		resultJSON.put("time", duration * 0.001) ;
		resultJSON.put("sources", sourcesListJSON);
		resultJSON.put("filterOption", filterOption);
		return resultJSON;
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray constructResultListJSON(List<ResultItem> resultList) {
		JSONArray jsonArray = new JSONArray();
		for(ResultItem result : resultList) {
			JSONObject item = constructResultItemJSON(result);
			jsonArray.add(item);
		}
		return jsonArray;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject constructResultItemJSON(ResultItem resultItem) {
		JSONObject item = new JSONObject();
		String snippet = constructSnippet(resultItem.get("snippet"));
		List<String> propSnippets = constructPropertySnippet(resultItem.get("propSnippets"));
		
		if(snippet != null)
			resultItem.put("snippet", snippet);
		JSONArray propSnippetJSON = new JSONArray();
		for(String propSnippet : propSnippets) {
			JSONObject snippetJSON = new JSONObject();
			snippetJSON.put("snippet", propSnippet);
			propSnippetJSON.add(snippetJSON); 
		}
		item.put("propSnippet", propSnippetJSON);
		resultItem.put("propSnippet", propSnippetJSON);
		
		return resultItem.toJSON();
	}
	

	private List<String> constructPropertySnippet(String propertySnippetsPlainText) {
		List<String> snippets = new ArrayList<String> ();
		if(reqPropList.size() == 0) {
			return Collections.emptyList();
		}

		if(propertySnippetsPlainText != null) {
			String[] propertySnippets = propertySnippetsPlainText.split("\\[level1\\]");
			for(String propertySnippet : propertySnippets) {
				String[] splitted = propertySnippet.split("\\[level2\\]");
				if(splitted.length == 2) {
					String propertyURI = splitted[0];
					String value = splitted[1];
					String propertyLabel = propertyURI;
					if(propertyLabel != null) {
						snippets.add(String.format("<span class=\"hitted\">%s</span>：%s", propertyLabel, value));
					}
				}
			}
		}
		return snippets;
	}

	private String constructSnippet(String snippetsPlainText) {
		if(reqPlainList.size() == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		if(snippetsPlainText != null) {
			String[] snippets = snippetsPlainText.split("\\[level1\\]");
			Map<String, String> snippetMap = new HashMap<String, String> ();
			for(String snippet : snippets) {
				String[] splitted = snippet.split("\\[level2\\]");
				if(splitted.length != 4) continue;
				String snippetPropURI = splitted[0];
				String snippetPropLN = splitted[1];
				String snippetValue = splitted[2];
				String snippetPropLabel = "<span title=\"" + snippetPropURI + "\">" + DatasetService.getLabel(snippetPropLN) + "</span>";
				String snippetQuery = splitted[3];
				String[] snippetQueryItems = snippetQuery.split(" ");
				for(String snippetQueryItem : snippetQueryItems) {
					String highlighted ="<span class=\"hitted\">" + snippetQueryItem + "</span>";
					snippetValue = snippetValue.replaceAll("(?i)" + snippetQueryItem, highlighted);
				}
				snippetMap.put(snippetPropLabel, snippetValue);
			}
			for(String prop : snippetMap.keySet()) {
				if(sb.length() != 0) {
					sb.append("<br/>");
				}
				sb.append(prop + "：" + snippetMap.get(prop));
			}
		}
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	public JSONArray constructSourceListJSON(Set<String> sourcesSet) {
		JSONArray array = new JSONArray(); if(sourcesSet == null) return array;
		List<String> sourcesList = new ArrayList<String> (sourcesSet);
		Collections.sort(sourcesList);
		for(String source : sourcesList) {
			JSONObject item = new JSONObject();
			item.put("source", source);
			int count = qe.getResultCount(source);
			item.put("count", count);
			if(count != 0)
				array.add(item);
		}
		return array;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject getClassFilterOption(List<ResultItem> results) {
		JSONObject filterOption = new JSONObject();
		Map<String, Integer> classMap = FacetService.constructFacet(results);
		ClassTreeGenerator classTreeGen = new ClassTreeGenerator(classMap);
		ClassTree classTree = classTreeGen.getClassTree();
		filterOption.put("classTree", classTree);
		return filterOption;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
