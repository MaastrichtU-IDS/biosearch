package cn.edu.nju.ws.biosearch.recommend;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cn.edu.nju.ws.biosearch.executor.ExecutorManager;
import cn.edu.nju.ws.biosearch.executor.SPARQLQueryExecutor;
import cn.edu.nju.ws.biosearch.query.ResultItem;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.JenaException;

public class RecommendationEngine {
	
	private Map<String, Map<String, String>> recoRules;
	private Map<String, Map<String, String>> refRules;

	private static RecommendationEngine inst = null;
	
	public RecommendationEngine() {
		recoRules = new HashMap<String, Map<String, String>> ();
		refRules = new HashMap<String, Map<String, String>> ();
		readRulesFromFile("reco");
		readRulesFromFile("ref");
	}
	
	public static RecommendationEngine getInstance() {
		if(inst == null)
			inst = new RecommendationEngine();
		return inst;
	}
	
	public static List<ResultItem> getRecommendedEntities(ResultItem item, Map<String, Map<String, String>> rules) {
		List<ResultItem> entities = new ArrayList<ResultItem> ();
		Map<String, String> rule = null;
		for(String type : item.getType()) {
			if(rules.containsKey(type))
				rule = rules.get(type);
		}
		if(rule == null)
			return entities;
		
		for(String reason : rule.keySet()) {
			String sparqlTemplate = rule.get(reason);
			String query = String.format(sparqlTemplate, item.getURI());
//			System.out.println(query);
			SPARQLQueryExecutor sqe =  ExecutorManager.getInstance().getExecutor(item.getSource());
			try {
				ResultSet rs = sqe.execSelect(query);
				while(rs.hasNext()) {
					QuerySolution qs = rs.nextSolution();
					RDFNode s = qs.get("reco");
					String entity = s.asResource().getURI();
					if(!entity.equals(item.getURI())) {
						ResultItem reco = new ResultItem(entity);
						reco.put("reason", String.format("%s With %s", reason, item.getLabel()));
						entities.add(reco);
					}
				}
			} catch (JenaException e) {
				continue;
			} finally {
				sqe.close();
			}
		}
		return entities;
	}
	
	public static List<ResultItem> getRecommendedEntities(ResultItem item) {
		return getRecommendedEntities(item, getInstance().recoRules);
	}
	
	public static List<ResultItem> getRefedEntities(ResultItem item) {
		return getRecommendedEntities(item, getInstance().refRules);
	}

	public static List<ResultItem> getRecommendedEntities(List<ResultItem> items) {
		List<ResultItem> entities = new ArrayList<ResultItem> ();
		for(ResultItem item : items) {
			entities.addAll(getRecommendedEntities(item));
		}
		return entities;
	}
	
	public void readRulesFromFile(String type) {
		InputStream input = RecommendationEngine.class.getClassLoader().getResourceAsStream("rules/index_" + type);
		Scanner scanner = new Scanner(input);
		while(scanner.hasNext()) {
			String fileName = scanner.nextLine();
			InputStream ruleInput = RecommendationEngine.class.getClassLoader().getResourceAsStream("rules/" + fileName);
			if(ruleInput != null) {
				if(type.equals("reco"))
					readRulesFromFile(ruleInput, recoRules);
				else if(type.equals("ref"))
					readRulesFromFile(ruleInput, refRules);
			}

		}
		scanner.close();
	}

	private void readRulesFromFile(InputStream ruleInput, Map<String, Map<String, String>> rules) {
		Scanner scanner = new Scanner(ruleInput);
		String type = null;
		String reason = null;
		String sparql = null;
		if(scanner.hasNext())
			type = scanner.nextLine();
		if(scanner.hasNext())
			reason = scanner.nextLine();
		StringBuilder sb = new StringBuilder();
		while(scanner.hasNext()) {
			sb.append(scanner.nextLine());
		}
		scanner.close();
		if(sb.length() != 0)
			sparql = sb.toString();
		if(type != null && reason != null && sparql != null) {
			Map<String, String> rule = rules.get(type);
			if(rule == null)
				rule = new HashMap<String, String> ();
			rule.put(reason, sparql);
			rules.put(type, rule);
		}
	}

}
