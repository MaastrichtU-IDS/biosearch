package cn.edu.nju.ws.biosearch.datasource;


import java.sql.Connection;
import java.util.Set;

import virtuoso.jena.driver.VirtGraph;


/**
 * @author "Cunxin Jia"
 *
 */
public interface IDataSource {
	
	/**
	 *get the list of registered sources 
	 * @return list of source names
	 */
	public Set<String> listSourceNames();
	
	/**
	 * get connection of specified source
	 * @param source: name of source
	 * @return connection
	 */
	public Connection getDBConnectionBySource(String source);
	
	/**
	 * 
	 * @param source
	 * @return VirtGraph
	 */
	public VirtGraph getVirtGraphBySource(String source);
	
}
