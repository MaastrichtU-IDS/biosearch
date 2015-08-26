package cn.edu.nju.ws.biosearch.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.ws.biosearch.datasource.DataSourceManager;
import cn.edu.nju.ws.biosearch.executor.ExecutorManager;
import cn.edu.nju.ws.biosearch.executor.SPARQLQueryExecutor;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class DatasetService {
	public static String getLabel(String uri) {
		String source = getSource(uri);
		if(source == null)
			return uri;
		SPARQLQueryExecutor executor = ExecutorManager.getInstance().getExecutor(source);
		if(executor == null)
			return uri;
		String label = null;
		String query = String.format("select ?label where {<%s> <%s> ?label}", uri, "http://www.w3.org/2000/01/rdf-schema#label");
		ResultSet rs = null;
		try {
			rs = executor.execSelect(query);
			while(rs != null && rs.hasNext()) {
				QuerySolution qs = rs.next();
				label = qs.get("label").asLiteral().getLexicalForm();
			}
		} finally {
			executor.close();
		}
		if(label != null) {
			label = shortLabel(label);
		}
		return label;
	}
	
	public static String shortLabel(String label) {
		String shortLabel = label.replaceAll("\\[\\w+:\\w+\\]", "");
		if(!shortLabel.equals(""))
			label = shortLabel;
		
		return label;
	}

	public static List<String> getType(String uri, String source) {
		List<String> type = new ArrayList<String> ();
		if(source == null)
			return type;
		SPARQLQueryExecutor executor = ExecutorManager.getInstance().getExecutor(source);
		if(executor == null)
			return type;
		String query = String.format("select ?type where {<%s> a ?type}", uri);
		ResultSet rs = null;
		try {
			rs = executor.execSelect(query);
			while(rs != null && rs.hasNext()) {
				QuerySolution qs = rs.next();
				String typeURI = qs.get("type").toString();
				if(typeURI.toLowerCase().contains("resource")) continue;
				type.add(typeURI);
			}
		} finally {
			executor.close();
		}
		return type;
	}

	public static Map<String, String> getProperties(String source) {
		Map<String, String> properties = new HashMap<String, String> ();
		if(source == null)
			return properties;
		SPARQLQueryExecutor executor = ExecutorManager.getInstance().getExecutor(source);
		if(executor == null)
			return properties;
		String query = String.format("select ?p, ?label where {{?p a owl:DatatypeProperty.} union {?p a owl:ObjectProperty.}. ?p rdfs:label ?label}");
		ResultSet rs = null;
		try {
			rs = executor.execSelect(query);
			while(rs != null && rs.hasNext()) {
				QuerySolution qs = rs.next();
				String[] uniqueLabelSplitted = qs.get("label").asLiteral().getLexicalForm().split("\\[");
				String uniqueLabel = null;
				if(uniqueLabelSplitted.length == 2)
					uniqueLabel = uniqueLabelSplitted[1];
				else
					uniqueLabel = uniqueLabelSplitted[0];
				uniqueLabel = uniqueLabel.substring(0, uniqueLabel.length() - 1);
				String URI = qs.get("p").asResource().getURI();
				properties.put(uniqueLabel, URI);
			}
		} finally {
			executor.close();
		}
		return properties;
	}

	public static String getSource(String uri) {
		if(uri == null) return null;
		Set<String> sourceNames = DataSourceManager.getInstance().listSourceNames();
		for(String sourceName : sourceNames) {
			if(uri.contains(sourceName)) {
				return sourceName;
			}
		}
		
		return null;
	}
	
	public static void main(String[] args) {
		
//		List<String> type = getType("http://bio2rdf.org/mesh:D010300", "mesh");
//		String label1 = getLabel("http://bio2rdf.org/drugbank_resource:calculated-properties-DB05013-3");
		//System.out.println(label);
//		System.out.println(type);
		
		
		/*
		Set<String> sources = DataSourceManager.getInstance().listSourceNames();
		for(String source : sources) {
			Map<String, String> props = getProperties(source);
			System.out.println(source);
			for(String key : props.keySet()) {
				System.out.println("\t\t" + key + ":" + props.get(key));
			}
		}
		*/
		
		String label = "[drugbank_vocabulary:e78186eb12eeaebda8a530a67513beea]";
		label = DatasetService.shortLabel(label);
		System.out.println(label);
	}
}
