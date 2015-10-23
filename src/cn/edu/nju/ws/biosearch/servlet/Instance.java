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
		instURI = req.getParameter("instURI");
		instURI = URLDecoder.decode(instURI, "utf-8");
		SPARQLQueryExecutor executor = ExecutorManager.getInstance().getExecutor(DatasetService.getSource(instURI));
		if(executor == null) {
			resp.setContentType("text/json; charset=UTF-8");
			resp.getWriter().print(new JSONObject());
			return;
		}
		Model model = executor.execDescribe(instURI);
		executor.close();
		List<String> uList = new ArrayList<String> ();
		uList.add(instURI);
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

	private JSONObject constructResultJSON(Model model, List<ResultItem> recos, List<ResultItem> corefs,List<String> refs) {
		JSONObject instJSON = new JSONObject();
		JSONArray pvArrayJSON = constructInstanceJSON(model);
		JSONArray refJSON = constructRefListJSON(refs);
		JSONArray recosJSON = constructRecommendListJSON(recos);
		JSONArray corefsJSON = constructCorefListJSON(corefs);
		PropertyTree tree = om.getPropertyHierarchy(pvArrayJSON);
		instJSON.put("subject", instURI);
		instJSON.put("pvarray", tree);
		instJSON.put("recommend", recosJSON);
		instJSON.put("coreference", corefsJSON);
		instJSON.put("reference", refJSON);
		instJSON.put("label", DatasetService.getLabel(instURI));
		instJSON.put("img", ImageService.getImage(instURI));
		return instJSON;
		
	}
	
	private String constructLiteralString(Literal literal) {
		String literalString = null;
			literalString = literal.getLexicalForm();
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
			
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();	
			String predicateLN = predicate.getLocalName();
			String predicateURI = predicate.getURI();

			if(isConvertLink(stmt)) {
				predicateLN = "is " + predicateLN +" of";
			}
			JSONObject item = new JSONObject();
			
			
			if(pvHashMap.containsKey(predicateURI)) {
				ArrayList<String> valueField = pvHashMap.get(predicateURI);
				pvHashMap.remove(predicateURI);
				
				if(isIncomingXLink(stmt)) {
					for(int i = 0; i < valueField.size(); i ++) {
						JSONObject itemXLINK = new JSONObject();
						String uri = subject.asResource().getURI();
						String source = DatasetService.getSource(uri);
						itemXLINK.put("isObject", true);
						itemXLINK.put("URI", predicate.getURI());
						itemXLINK.put("value", valueField.get(i));
						itemXLINK.put("prop", "is-"+predicate.getLocalName()+"("+i+")-of");
						itemXLINK.put("source", source);
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
				if(object.isResource()) {
					String uri = object.asResource().getURI();
					String source = DatasetService.getSource(uri);
					item.put("source", source);
				} else {
					item.put("source", "");
				}
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

			Property predicate = stmt.getPredicate();
			
			String predicateURI = null;
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
	
	private boolean isConvertLink(Statement stmt) {
		RDFNode object = stmt.getObject();
		if(!object.isResource()) return false;
		String uri = object.asResource().getURI();
		if(uri.equals(instURI)) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isXLink(Statement stmt) {
		Property predicate = stmt.getPredicate();
		RDFNode object = stmt.getObject();
		if(!object.isResource()) return false;
		
		if(predicate.getLocalName().contains("x-")) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isIncomingXLink(Statement stmt) {
		if(!isXLink(stmt)) return false;
		RDFNode object = stmt.getObject();
		String uri = object.asResource().getURI();
		
		if(uri.equals(instURI)) { //incoming x-link
			return true;
		} else {
			return false;
		}
	}
	
	private String getValue(Statement stmt) {
		Resource subject = stmt.getSubject();
		RDFNode object = stmt.getObject();
		
		if(isConvertLink(stmt) && !isIncomingXLink(stmt)) {
			subject = stmt.getObject().asResource();
			object = stmt.getSubject();
		}

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
				value = String.format("<a target='_blank' href ='%s' title='%s'><img src='resources/img/outlink.png' width='8' height='8'/></a>", uri, uri);
			else {
				String label = DatasetService.getLabel(uri);
				if(label == null) return null;
				if(isXLink(stmt)) {
					value = String.format("<a href ='instance.html?inst=%s' title='%s'>%s</a>"
							+ "<a href ='%s' title='%s'><img src='resources/img/outlink.png' width='8' height='8' /></a>", uri, uri, label, uri, uri);
				} else {
					value = String.format("<a href ='instance.html?inst=%s' title='%s'>%s</a>", uri, uri, label);
				}
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
