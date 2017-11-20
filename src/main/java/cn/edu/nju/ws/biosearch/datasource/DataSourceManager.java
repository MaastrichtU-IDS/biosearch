package cn.edu.nju.ws.biosearch.datasource;


import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import virtuoso.jena.driver.VirtGraph;

import com.hp.hpl.jena.shared.JenaException;

/**
 * @author "Cunxin Jia"
 *
 */
public class DataSourceManager implements IDataSource {
	
	private static volatile DataSourceManager inst = null;
	private Set<String> sourceNames;
	private String url;
	private String user;
	private String passwd;
	private Map<String, String> graphMap;
	
	public static DataSourceManager getInstance() {
		if(inst == null) {
			synchronized(DataSourceManager.class) {
				if(inst == null) {
					inst = new DataSourceManager();
				}
			}
		}
		return inst;
	}
	
	private DataSourceManager() {
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
			props.load(DataSourceManager.class.getClassLoader().getResourceAsStream("config/datasource.properties"));
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
			//System.out.println(url+" "+user+" "+passwd);
			
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

}
