package cn.edu.nju.ws.biosearch.servlet;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import cn.edu.nju.ws.biosearch.executor.ExecutorManager;
import cn.edu.nju.ws.biosearch.executor.SPARQLQueryExecutor;
import cn.edu.nju.ws.biosearch.fusion.CoreferenceService;
import cn.edu.nju.ws.biosearch.ontology.OntManager;
import cn.edu.nju.ws.biosearch.ontology.PropertyTree;
import cn.edu.nju.ws.biosearch.query.DatasetService;
import cn.edu.nju.ws.biosearch.query.ImageService;
import cn.edu.nju.ws.biosearch.query.ResultItem;
import cn.edu.nju.ws.biosearch.recommend.RecommendationEngine;
import cn.edu.nju.ws.biosearch.recommend.ReferenceService;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Servlet implementation class Instance
 */
//@WebServlet("/instance")
public class Instance extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Instance() {
        super();
        // TODO Auto-generated constructor stub
    }

    private OntManager om;
	private String instURI;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String instURI = req.getParameter("instURI");
		this.instURI = instURI;
//		String source = KeywordSearcher.getInstance().getInstanceSource(instURI);
		instURI = URLDecoder.decode(instURI, "utf-8");
		SPARQLQueryExecutor executor = ExecutorManager.getInstance().getExecutor(DatasetService.getSource(instURI));
		if(executor == null) {
			resp.setContentType("text/json; charset=UTF-8");
			resp.getWriter().print(new JSONObject());
			//resp.sendRedirect(instURI);
			return;
		}
		Model model = executor.execDescribe(instURI);
//		Set<Intel> intels = getIntel();
		List<String> uList = new ArrayList<String> ();
		uList.add(instURI);
//		List<String> recos = Reco2.reco(uList, 5);
//		List<String> recos = new ArrayList<String> ();
//		Map<String, String> recos = Reco2.recommend(uList);
		List<ResultItem> recos = RecommendationEngine.getRecommendedEntities(new ResultItem(instURI));
		List<ResultItem> corefs = CoreferenceService.getCorefs(model);
		List<String> refs = ReferenceService.getReferences(instURI);
		JSONObject instJSON = constructResultJSON(model, recos, corefs, refs);
		resp.setContentType("text/json; charset=UTF-8");
		resp.getWriter().print(instJSON);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	public void init() throws ServletException {
		super.init();
		om = OntManager.getInstance();
	}
	
	
	private JSONArray constructRecommendListJSON(List<ResultItem> recos) {
		JSONArray jsonArray = new JSONArray();
		for(ResultItem reco : recos) {
			jsonArray.add(reco.toJSON());
		}
		return jsonArray;
	}
	
	private JSONArray constructCorefListJSON(List<ResultItem> corefs) {
		JSONArray jsonArray = new JSONArray();
		for(ResultItem coref : corefs) {
			jsonArray.add(coref.toJSON());
		}
		return jsonArray;
	}
	
	private JSONArray constructRefListJSON(List<String> refs) {
		JSONArray jsonArray = new JSONArray();
		if(refs == null || refs.isEmpty()) return jsonArray;
		
		for(String ref : refs) {
			JSONObject item = new JSONObject();
			item.put("id", ref);
			jsonArray.add(item);
		}
		return jsonArray;
	}
/*	
	private JSONArray constructIntelListJSON2(Set<Intel> intelSet) {
		JSONArray jsonArray = new JSONArray();
		if(intelSet == null || intelSet.isEmpty()) return jsonArray;
		List<Intel> intelList = new ArrayList<Intel>(intelSet);
		if(intelList.size() > 5)
		intelList = intelList.subList(0, 5);
		Collections.sort(intelList, intelList.get(0).getTimeComparator());
		
		for(Intel intel : intelList) {
			JSONObject item = new JSONObject();
			item.put("content", intel.getContentWithKeyword());
			item.put("date", intel.getDateString());
			item.put("time", intel.getTimeString());
			item.put("keyword", intel.getKeyword());
			jsonArray.add(item);
		}
		return jsonArray;
	}
	
	private Set<Intel> getIntel() {
		Set<Intel> intels = new HashSet<Intel> ();
		String instLabel = keywordSearcher.getInstanceLabel(instURI);
		intels.addAll(IntelManager.getInstance().getRelativeIntel(instLabel));
		return intels;
	}
*/	
	private JSONObject constructResultJSON(Model model, List<ResultItem> recos, List<ResultItem> corefs,List<String> refs) {
		JSONObject instJSON = new JSONObject();
		JSONArray pvArrayJSON = constructInstanceJSON(model);
		JSONArray refJSON = constructRefListJSON(refs);
//		JSONArray intelsJSON2 = constructIntelListJSON2(intels);
		JSONArray recosJSON = constructRecommendListJSON(recos);
		JSONArray corefsJSON = constructCorefListJSON(corefs);
		PropertyTree tree = om.getPropertyHierarchy(pvArrayJSON);
//		System.out.println(tree);
		instJSON.put("subject", instURI);
		instJSON.put("pvarray", tree);
		instJSON.put("recommend", recosJSON);
		instJSON.put("coreference", corefsJSON);
		instJSON.put("reference", refJSON);
//		instJSON.put("intel2", intelsJSON2);
		instJSON.put("label", DatasetService.getLabel(instURI));
		instJSON.put("img", ImageService.getImage(instURI));
//		instJSON.put("type", keywordSearcher.getInstanceType(instURI));
		return instJSON;
		
	}
	
//	private JSONArray constructInstanceJSON(Model model) {
//		JSONArray pvArray = new JSONArray();
//		JSONObject item = new JSONObject();
//		StmtIterator iter = model.listStatements();
//		Map<String, JSONArray> composedMap = new HashMap<String, JSONArray> ();
//		int x_linkin = 0;
//		while(iter.hasNext()){
//			Statement stmt = iter.nextStatement();
//
//			System.out.println(stmt.toString());
//			Resource subject = stmt.getSubject();
//			Property predicate = stmt.getPredicate();
//			RDFNode object = stmt.getObject();	
//			String valueField = null;
//			item = new JSONObject();
//			
//			if(object.isLiteral()) {
//				valueField = object.asLiteral().getLexicalForm();
//				
//				item.put("prop", predicate.getLocalName());
//				item.put("isObject", "false");
//			}
//			else if(object.isResource()) {
//				String uri = object.asResource().getURI();
//				
//				if(uri.equals(instURI) && predicate.getLocalName().contains("x-")) { //x-link in
//					uri = subject.asResource().getURI();
//					item.put("prop", "is-"+predicate.getLocalName()+"("+x_linkin+")-of");
//					x_linkin ++;
//				} else { //normal property
//					item.put("prop", predicate.getLocalName());
//				}
//				
//				String source = DatasetService.getSource(uri);
//				if(source == null)
//					valueField = String.format("<a target='_blank' href ='%s' title='%s'>%s</a>", uri, uri, uri);
//				else {
//					String label = DatasetService.getLabel(uri);
//					if(label == null) continue;
//					valueField = String.format("<a href ='instance.html?inst=%s' title='%s'>%s</a>", uri, uri, label);
//				}
//				
//				item.put("isObject", "true");
//			}
//			
//			
//			item.put("URI", predicate.getURI());
//			item.put("value", valueField);
//			pvArray.add(item);
//		}
//		
//		return pvArray;
//	}
	
	private String constructLiteralString(Literal literal) {
		String literalString = null;
//		if(literal.getDatatypeURI().equals(XSD.date.getURI())) {
//			Date date = new Date(literal.getLexicalForm());
//			DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
//			literalString = dateformat.format(date);
//		}
//		else {
			literalString = literal.getLexicalForm();
//		}
		return literalString;
	}
	
	private JSONObject constructPVJSON(String prop, String value) {
		JSONObject pvJSON = new JSONObject();
		pvJSON.put("prop", prop);
		pvJSON.put("value", value);
		return pvJSON;
	}
	
	private JSONArray constructInstanceJSON(Model model) {
		JSONArray pvArray = new JSONArray();
		HashMap<String, JSONArray> pvHashMap = constructPVHashMap(model);
		StmtIterator iter = model.listStatements();
		while(iter.hasNext()){
			Statement stmt = iter.nextStatement();
			
//			System.out.println("!!"+stmt.toString());
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();	
			JSONObject item = new JSONObject();
			
			String predicateLN = predicate.getLocalName();
			String predicateURI = predicate.getURI();
			if(pvHashMap.containsKey(predicateURI)) {
				ArrayList<String> valueField = pvHashMap.get(predicateURI);
				pvHashMap.remove(predicateURI);
				
				if(isIncomingXLink(stmt)) {
					for(int i = 0; i < valueField.size(); i ++) {
						JSONObject itemXLINK = new JSONObject();
						itemXLINK.put("isObject", true);
						itemXLINK.put("URI", predicate.getURI());
						itemXLINK.put("value", valueField.get(i));
						itemXLINK.put("prop", "is-"+predicate.getLocalName()+"("+i+")-of");
						pvArray.add(itemXLINK);
					}
					continue;
				} else if(valueField.size() == 1) {
					item.put("value", valueField.get(0));
				} else {
					item.put("value", valueField);
				}
				item.put("isObject", object.isResource());
				item.put("URI", predicate.getURI());
				item.put("prop", predicateLN);
			} else {
				continue;
			}
			
			pvArray.add(item);
		}
		return pvArray;
	}
	
	private HashMap<String, JSONArray> constructPVHashMap(Model model) {
		HashMap<String, JSONArray> pvHashMap = new HashMap<String, JSONArray>();
		StmtIterator iter = model.listStatements();
		while(iter.hasNext()){
			Statement stmt = iter.nextStatement();

//			System.out.println(stmt.toString());
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();
			
			String predicateURI = null;
			String uri = null;
			JSONArray valueField;
			String value = getValue(stmt);
			
			if(value == null) continue;
			
			predicateURI = predicate.getURI();
			if(!pvHashMap.containsKey(predicateURI))	{
				valueField = new JSONArray();
				pvHashMap.put(predicateURI, valueField);
			} else {
				valueField = pvHashMap.get(predicateURI);
			}
			
			valueField.add(getValue(stmt));
		}
		
		Iterator it = pvHashMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Object key = entry.getKey();
			Object val = entry.getValue();
			
			pvHashMap.put((String) key, reduceDuplicateValue((JSONArray) val));
		}
		
		return pvHashMap;
	}
	
	private boolean isIncomingXLink(Statement stmt) {
		Property predicate = stmt.getPredicate();
		RDFNode object = stmt.getObject();
		if(!object.isResource()) return false;
		String uri = object.asResource().getURI();
		
		if(uri.equals(instURI) && predicate.getLocalName().contains("x-")) { //incoming x-link
			return true;
		} else {
			return false;
		}
	}
	
	private String getValue(Statement stmt) {
		Resource subject = stmt.getSubject();
		RDFNode object = stmt.getObject();
		
		String value = null;
		if(object.isLiteral()) {
			value = object.asLiteral().getLexicalForm();
		}
		else if(object.isResource()) {
			String uri = null;
			if(isIncomingXLink(stmt)) { //incoming x-link
				uri = subject.asResource().getURI();
			} else {
				uri = object.asResource().getURI();
			}
			String source = DatasetService.getSource(uri);
			if(source == null)
				value = String.format("<a target='_blank' href ='%s' title='%s'>%s</a>", uri, uri, uri);
			else {
				String label = DatasetService.getLabel(uri);
				if(label == null) return null;
				value = String.format("<a href ='instance.html?inst=%s' title='%s'>%s</a>", uri, uri, label);
			}
		}
		return value;
	}
	
	private JSONArray reduceDuplicateValue(JSONArray valueField) {
		HashSet<String> hashValueField = new HashSet<String>();
		for(int i = 0; i < valueField.size(); i ++) {
			hashValueField.add(valueField.get(i).toString());
		}
		valueField.clear();
		
		for(String value : hashValueField) {
			valueField.add(value);
		}
		
		return valueField;
	}
}
