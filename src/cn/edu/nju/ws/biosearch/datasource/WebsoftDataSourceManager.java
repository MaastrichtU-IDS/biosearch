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
public class WebsoftDataSourceManager implements IDataSource {
	
	private static volatile WebsoftDataSourceManager inst = null;
	private Set<String> sourceNames;
	private Map<String, String> urlMap;
	private Map<String, String> graphMap;
	private Map<String, String> userMap;
	private Map<String, String> passwdMap;
	
	public static WebsoftDataSourceManager getInstance() {
		if(inst == null) {
			synchronized(WebsoftDataSourceManager.class) {
				if(inst == null) {
					inst = new WebsoftDataSourceManager();
				}
			}
		}
		return inst;
	}
	
	private WebsoftDataSourceManager() {
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
			props.load(WebsoftDataSourceManager.class.getClassLoader().getResourceAsStream("config/datasource.properties.websoft"));
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

}
