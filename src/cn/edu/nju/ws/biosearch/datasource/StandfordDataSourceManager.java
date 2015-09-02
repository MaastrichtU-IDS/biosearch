package cn.edu.nju.ws.biosearch.datasource;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import virtuoso.jena.driver.VirtGraph;
import cn.edu.nju.ws.biosearch.executor.VirtuosoSPARQLQueryExecutor;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.JenaException;

/**
 * @author "Cunxin Jia"
 *
 */
public class StandfordDataSourceManager implements IDataSource {
	
	private static StandfordDataSourceManager inst = null;
	private Set<String> sourceNames;
	private String url;
	private String user;
	private String passwd;
	private Map<String, String> graphMap;
	
	public static StandfordDataSourceManager getInstance() {
		if(inst == null) {
			inst = new StandfordDataSourceManager();
		}
		return inst;
	}
	
	private StandfordDataSourceManager() {
		initContainers();
		readConfig();
	}
	
	private void initContainers() {
		sourceNames = new HashSet<String>();
		graphMap = new HashMap<String, String> ();
		url = "";
		user = "";
		passwd = "";
	}
	
	private void readConfig() {
		Properties props = new Properties();
		try {
			props.load(StandfordDataSourceManager.class.getClassLoader().getResourceAsStream("config/datasource.properties"));
			String sources = (String) props.get("SOURCES");
			String[] sourcesArray = sources.split(";");
			for(String source : sourcesArray) {
				String graph = (String) props.get(source + "_GRAPH");
				if(graph != null) {
					graphMap.put(source, graph);
					sourceNames.add(source);
				}
			}
			
			url = (String) props.get("endpoint_URL");
			user = (String) props.get("endpoint_USER");
			passwd = (String) props.get("endpoint_PASSWD");
			System.out.println(url+" "+user+" "+passwd);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getDataSourceURL() {
		return url;
	}
	
	public String getDataSourceUser() {
		return user; 
	}
	
	public String getDataSourcePassword() {
		return passwd;
	}
	
	@Override
	public Set<String> listSourceNames() {
		return sourceNames;
	}

	@Override
	public Connection getDBConnectionBySource(String source) {
		return null;
	}
	
	
	@Override
	public VirtGraph getVirtGraphBySource(String source) {
		VirtGraph conn = null;
		try {
			conn = new VirtGraph (graphMap.get(source), url, user, passwd);
		} catch(JenaException e) {
			//failed to connect
			conn = null;
		}
		return conn;
	}

	public static void main(String[] args) throws SQLException {
//<<<<<<< .mine
		IDataSource ds = StandfordDataSourceManager.getInstance();
		Set<String> names = ds.listSourceNames();
		for(String name : names) {
			VirtuosoSPARQLQueryExecutor vsqe = new VirtuosoSPARQLQueryExecutor(name);
			ResultSet results = vsqe.execSelect("select count(*) as ?count where {?s ?p ?o}");
                while (results.hasNext()) {
                        QuerySolution result = results.nextSolution();
                        RDFNode count = result.get("count");
                        System.out.println(name + "\t" + count.toString());
                }
		}
//		
////		
////		try {
////			String url = "jdbc:oracle:thin:@114.212.86.226:1521:websoft";
////			String user = "TEST";
////			String passwd = "test";
////			Connection co = DriverManager.getConnection(url, user, passwd);
////			PreparedStatement pstmt = co.prepareStatement("select * from HKMJ");
////			ResultSet rs = pstmt.executeQuery();
////			while(rs.next()) {
////				System.out.println(rs.getString(2));
////			}
////			
////			Class<?> a = Class.forName("String");
////			String test = (String) a.newInstance();
////			System.out.println(test);
////		} catch(Exception e) {
////			
////		}
//		
//		
//=======
//		IDataSource ds = DataSourceManager.getInstance();
//		List<String> names = ds.listSourceNames();
//		for(String name : names) {
//			System.out.println(name);
//			Connection conn = ds.getDBConnectionBySource(name);
//			ResultSet rst = conn.getMetaData().getTables(null, name, "%", new String[]{"TABLE"});
////			while (rst.next()) {
////				String tname = rst.getString("TABLE_NAME");
////				System.out.println(tname);
////			}
//		}
//>>>>>>> .r69
	}


}
