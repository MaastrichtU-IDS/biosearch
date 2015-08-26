package cn.edu.nju.ws.biosearch.executor;

import java.util.Set;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import cn.edu.nju.ws.biosearch.datasource.DataSourceManager;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.JenaException;

public class VirtuosoSPARQLQueryExecutor implements SPARQLQueryExecutor {
	private VirtGraph conn;
	private String source;
	
	public VirtuosoSPARQLQueryExecutor(String source) {
		this.source = source;
	}

	@Override
	public ResultSet execSelect(String query) {
		conn = DataSourceManager.getInstance().getVirtGraphBySource(source);
		if(conn == null) 
			return null;
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, conn);
		ResultSet rs = null;
		try {
			rs = vqe.execSelect();
		} catch(JenaException e) {
			rs = null;
		}
		return rs;
	}

	@Override
	public Model execDescribe(String uri) {
		conn = DataSourceManager.getInstance().getVirtGraphBySource(source);
		String query = String.format("describe <%s>", uri);
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, conn);
		
		Model model = null;
		try {
			model = vqe.execDescribe();
		} finally {
			conn.close();
		}
		
		Model modelIn = execIncomeLink(uri);
		if(modelIn != null) {
			model.add(modelIn);
		} else {
//			System.out.println("no income link\n");
		}
		
		return model;
	}
	
	@Override
	public Model execIncomeLink(String uri) {
		String queryIn = String.format("select ?s ?p <%s> where {?s ?p <%s>.}", uri, uri);
		Model model = null;
		
		Set<String> sourceNames = DataSourceManager.getInstance().listSourceNames();
		for(String sourceName : sourceNames) {
			if(sourceName.equals(source)) continue;
			conn = DataSourceManager.getInstance().getVirtGraphBySource(sourceName);
			if(conn == null) {
				continue;
			}
			VirtuosoQueryExecution vqeIn = VirtuosoQueryExecutionFactory.create(queryIn, conn);
			try {
				if(model == null) {
					model = vqeIn.execDescribe();
				} else {
					model.add(vqeIn.execDescribe());
				}
			} finally {
				conn.close();
			}
		}

		return model;
	}

	@Override
	public void close() {
		if(conn != null && !conn.isClosed())
			conn.close();
	}

}
