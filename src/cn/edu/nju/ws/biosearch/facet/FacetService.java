package cn.edu.nju.ws.biosearch.facet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.nju.ws.biosearch.query.ResultItem;

public class FacetService {
	public static Map<String, Integer> constructFacet(List<ResultItem> results) {
		Map<String, Integer> types = new HashMap<String, Integer> ();
		for(ResultItem item : results) {
			for(String type : item.getType()) {
				Integer count = types.get(type);
				if(count == null) {
					count = 0;
				}
				count += 1;
				types.put(type, count);
			}
		}
		return types;
	}
}
