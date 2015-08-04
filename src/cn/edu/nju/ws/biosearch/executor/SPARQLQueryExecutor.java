package cn.edu.nju.ws.biosearch.executor;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public interface SPARQLQueryExecutor {
	ResultSet execSelect(String query);
	Model execDescribe(String query);
	Model execIncomeLink(String query);
	void close();
}
