package cn.edu.nju.ws.biosearch.recommend;

import java.util.ArrayList;
import java.util.List;

import cn.edu.nju.ws.biosearch.query.ResultItem;

public class ReferenceService {

	public static List<String> getReferences(String instURI) {
		List<ResultItem> refs = RecommendationEngine.getRefedEntities(new ResultItem(instURI));
		List<String> refIDs = new ArrayList<String> ();
		for(ResultItem ref : refs) {
			refIDs.add(ref.getURI().split(":")[2]);
		}
		return refIDs;
	}

}
