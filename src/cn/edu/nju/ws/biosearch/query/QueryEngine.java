package cn.edu.nju.ws.biosearch.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.atlas.logging.Log;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import cn.edu.nju.ws.biosearch.datasource.DataSourceManager;
import cn.edu.nju.ws.biosearch.executor.ExecutorManager;
import cn.edu.nju.ws.biosearch.executor.SPARQLQueryExecutor;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class QueryEngine {
	private Map<String, Integer> resultCount;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public QueryEngine() {
		resultCount = new HashMap<String, Integer>();
	}
	public List<ResultItem> query(Set<String> keywords, Set<String> classes,
						Set<String> properties, Set<String> sources) {
		if(sources == null || sources.isEmpty()) {
			//If no source input, select all the sources.
			sources = DataSourceManager.getInstance().listSourceNames();
		}
		List<ResultItem> results = new ArrayList<ResultItem> ();
		Map<String, ResultItem> resultMap = new HashMap<String, ResultItem> ();
		resultCount.clear();
		for(String source : sources) {
			SPARQLQueryExecutor executor = ExecutorManager.getInstance().getExecutor(source);
			String queryString = QueryConstructor.constructQuery(keywords, classes, properties, source);

			logger.info(source+"\t"+queryString+"\n");

			if(executor == null) continue;
			ResultSet rs = executor.execSelect(queryString);
			if(rs == null) continue;
			int count = 0;
			while (rs.hasNext()) {
				QuerySolution result = rs.nextSolution();
				RDFNode s = result.get("s");
				if(s == null) continue;
				RDFNode p = result.get("p");
				RDFNode pc = result.get("pc");
				RDFNode lo = result.get("lo");
				RDFNode o = result.get("o");
				String URI = s.asResource().getURI();
				ResultItem item = resultMap.get(URI);
				if(item == null) {
//					item = new ResultItem(URI, source);
					item = new ResultItem(URI);
					if(item.getSource() == null) continue;
					resultMap.put(URI, item);
					results.add(item);
					count++;
				}
				if(o != null && pc != null) {
					String propURI = pc.asResource().getURI();
					String propLN = pc.asResource().getLocalName();
					String value = o.toString();
					if(o.isLiteral()) {
						value = o.asLiteral().getLexicalForm();
					} else if(o.isResource()) {
						value = String.format("<a href='instance.html?inst=%s'>%s</a>", o.asResource().getURI(), o.asResource().getLocalName());
					}
					String snippet = item.get("propSnippet");
					if(snippet == null)
						snippet = "";
					item.put("propSnippets", String.format("%s[level2]%s[level2]%s[level1]%s", propURI, propLN, value, snippet));
				}
				if(lo != null && p != null) {
					String propURI = p.asResource().getURI();
					String propLN = p.asResource().getLocalName();
					String value = lo.asLiteral().getLexicalForm();
					StringBuilder sb = new StringBuilder();
					for(String keyword : keywords) {
						if(sb.length() != 0)
							sb.append(" ");
						sb.append(keyword);
					}
					String queryItems = sb.toString();
					String snippet = item.get("snippet");
					if(snippet == null)
						snippet = "";
					item.put("snippet", String.format("%s[level2]%s[level2]%s[level2]%s[level1]%s", propURI, propLN, value, queryItems, snippet));
					item.addScore(ResultScorer.getScore(queryItems, value));
				}
			}
			if(count != 0) {
				resultCount.put(source, count);
				logger.info("["+source+": "+count+"]\n");
			}
		}

		Collections.sort(results, new ResultItem.ResultItemComparator());

		return results;
	}
	
	public int getResultCount(String source) {
		Integer count = null;
		count = resultCount.get(source);
		if(count == null)
			count = 0;
		return count;
	}
	
	public static void main(String[] args) {
		QueryEngine qe = new QueryEngine();
		Set<String> keywords = new TreeSet<String> (Arrays.asList("triceps"));
		qe.query(keywords, null, null, null);
	}
}
