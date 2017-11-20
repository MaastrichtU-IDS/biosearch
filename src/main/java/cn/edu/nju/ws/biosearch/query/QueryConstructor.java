package cn.edu.nju.ws.biosearch.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.edu.nju.ws.biosearch.fusion.OntMappingService;

public class QueryConstructor {
	public static String constructQuery(Set<String> keywords, Set<String> classes, Set<String> properties, String source) {
		List<String> keywordsTriplePatterns = constructByKeywords(keywords);
		List<String> classesTriplePatterns = constructByClasses(classes, source);
		List<String> propertiesTriplePatterns = constructByProperties(properties, source);
		List<List<String>> graphPatterns = new ArrayList<List<String>> ();

		for(String classesTriplePattern : classesTriplePatterns) {
			List<String> graphPattern = new ArrayList<String> ();
			graphPattern.add(classesTriplePattern);
			graphPatterns.add(graphPattern);
		}

		List<List<String>> tmp = new ArrayList<List<String>> ();
		for(String propertiesTriplePattern : propertiesTriplePatterns) {
			if(graphPatterns.size() == 0)
				graphPatterns.add(new ArrayList<String> ());
			for(List<String> graphPattern : graphPatterns) {
				List<String> newGraphPattern = new ArrayList<String> (graphPattern);
				newGraphPattern.add(propertiesTriplePattern);
				tmp.add(newGraphPattern);
			}
		}
        if(propertiesTriplePatterns.size() != 0)
        	graphPatterns = tmp;
		tmp = new ArrayList<List<String>> ();
        for(String keywordsTriplePattern : keywordsTriplePatterns) {
			if(graphPatterns.size() == 0)
				graphPatterns.add(new ArrayList<String> ());
        	for(List<String> graphPattern : graphPatterns) {
				List<String> newGraphPattern = new ArrayList<String> (graphPattern);
				newGraphPattern.add(keywordsTriplePattern);
				tmp.add(newGraphPattern);
			}
		}
        if(keywordsTriplePatterns.size() != 0)
        	graphPatterns = tmp;
        String query = constructByGraphPattern(graphPatterns, keywords.size(), properties.size());
		return query;
	}


	private static String constructByGraphPattern(List<List<String>> graphPatterns, int keywordsSize, int propertiesSize) {
		StringBuilder sb = new StringBuilder();
		for(List<String> graphPattern : graphPatterns) {
			if(sb.length() != 0)
				sb.append("\n UNION \n");
			sb.append("{\n");
			for(String triplePattern : graphPattern) {
				sb.append(triplePattern + "\n");
			}
			sb.append("}\n");
		}
		if(keywordsSize == 0 && propertiesSize == 0) {
			return String.format("select distinct(?s) where {\n%s} limit 100", sb.toString());
		} else if(keywordsSize == 0) {
			return String.format("select distinct(?s),?o,?pc where {\n%s} limit 100", sb.toString());
		} else if(propertiesSize == 0) {
			return String.format("select distinct(?s),?p,?lo where {\n%s} limit 100", sb.toString());
		}
		return String.format("select distinct(?s),?o,?p,?pc,?lo where {\n%s} limit 1", sb.toString());
	}


	private static List<String> constructByProperties(Set<String> properties, String source) {
		List<String> triplePatterns = new ArrayList<String> ();
		if(properties == null) {
			return triplePatterns;
		}
		for(String property : properties) {
			HashSet<String> localPropertys = OntMappingService.getMapping(property, source);
			if (localPropertys == null) {
				String localProperty = property;
				triplePatterns.add(String.format("?s ?pc ?o. BIND(<%s> as ?pc).", localProperty));
			} else {
				for(String localProperty : localPropertys) {
					triplePatterns.add(String.format("?s ?pc ?o. BIND(<%s> as ?pc).", localProperty));
				}
			}
			
		}
		return triplePatterns;
	}

	private static List<String> constructByClasses(Set<String> classes, String source) {
		List<String> triplePatterns = new ArrayList<String> ();
		if(classes == null) {
			return triplePatterns;
		}
		for(String clazz : classes) {
			HashSet<String> localClasses = OntMappingService.getMapping(clazz, source);
			if(localClasses == null) {
				String localClass = clazz;
				triplePatterns.add(String.format("?s a <%s>.", localClass));
			} else {
				for(String localClass : localClasses) {
					triplePatterns.add(String.format("?s a <%s>.", localClass));
				}
			}
		}
		return triplePatterns;
	}

	private static List<String> constructByKeywords(Set<String> keywords) {
		List<String> triplePatterns = new ArrayList<String> ();
		if(keywords == null || keywords.size() == 0) {
			return triplePatterns;
		}
		StringBuilder sb = new StringBuilder();
		for(String keyword : keywords) {
			if(sb.length() != 0)
				sb.append(" ");
			sb.append(keyword);
		}
		if(keywords.size() == 1) {
			triplePatterns.add(String.format("?s ?p ?lo. ?lo bif:contains '%s'.", sb.toString()));
			return triplePatterns;
		}
		String andExp = sb.toString().replaceAll(" ", " AND ");
		String orExp = sb.toString().replaceAll(" ", " OR ");
		triplePatterns.add(String.format("?s ?p ?lo. ?lo bif:contains '%s'.", andExp));
		triplePatterns.add(String.format("?s ?p ?lo. ?lo bif:contains '%s'.", orExp));
		return triplePatterns;
	}
	
}
