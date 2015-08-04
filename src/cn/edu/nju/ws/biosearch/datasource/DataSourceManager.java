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
public class DataSourceManager implements IDataSource {
	
	private static DataSourceManager inst = null;
	private Set<String> sourceNames;
	private Map<String, String> urlMap;
	private Map<String, String> graphMap;
	private Map<String, String> userMap;
	private Map<String, String> passwdMap;
	
	public static DataSourceManager getInstance() {
		if(inst == null) {
			inst = new DataSourceManager();
		}
		return inst;
	}
	
	private DataSourceManager() {
		initContainers();
		readConfig();
	}
	
	private void initContainers() {
		sourceNames = new HashSet<String>();
		urlMap = new HashMap<String, String> ();
		graphMap = new HashMap<String, String> ();
		userMap = new HashMap<String, String> ();
		passwdMap = new HashMap<String, String> ();
	}
	
	private void readConfig() {
		Properties props = new Properties();
		try {
			props.load(DataSourceManager.class.getClassLoader().getResourceAsStream("config/datasource.properties"));
			String sources = (String) props.get("SOURCES");
			String[] sourcesArray = sources.split(";");
			for(String source : sourcesArray) {
				String url = (String) props.get(source + "_URL");
				String graph = (String) props.get(source + "_GRAPH");
				String user = (String) props.get(source + "_USER");
				String passwd = (String) props.get(source + "_PASSWD");
				if(url != null && user != null && passwd != null && graph != null) {
					urlMap.put(source, url);
					graphMap.put(source, graph);
					userMap.put(source, user);
					passwdMap.put(source, passwd);
					sourceNames.add(source);
					System.out.println(url + "\t" + user + "\t" + passwd);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getDataSourceURL(String sourceName) {
		return urlMap.get(sourceName);
	}
	
	public String getDataSourceUser(String sourceName) {
		return userMap.get(sourceName); 
	}
	
	public String getDataSourcePassword(String sourceName) {
		return passwdMap.get(sourceName);
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
			conn = new VirtGraph (graphMap.get(source), urlMap.get(source), userMap.get(source), passwdMap.get(source));
		} catch(JenaException e) {
			//failed to connect
			conn = null;
		}
		return conn;
	}

	public static void main(String[] args) throws SQLException {
//<<<<<<< .mine
		IDataSource ds = DataSourceManager.getInstance();
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
