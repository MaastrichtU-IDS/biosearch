package cn.edu.nju.ws.biosearch.fusion;

import java.util.ArrayList;
import java.util.List;

import cn.edu.nju.ws.biosearch.query.ResultItem;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class CoreferenceService {
	public static List<ResultItem> getCorefs(Model model) {
		List<ResultItem> corefs = new ArrayList<ResultItem> ();
		StmtIterator iter = model.listStatements();
		while(iter.hasNext()) {
			Statement stmt = iter.next();
			Property prop = stmt.getPredicate();
			if(prop.getLocalName().startsWith("x-")) {
				ResultItem coref = new ResultItem(stmt.getObject().asResource().getURI());
				if(coref.getSource() != null && coref.getLabel() != null && !coref.getLabel().equals(""))
					corefs.add(coref);
			}
		}
		return corefs;
	}
}
