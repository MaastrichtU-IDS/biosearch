/**
 * 
 */
package cn.edu.nju.ws.biosearch.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author "Cunxin Jia"
 *
 */
public class Config {
	private Properties prop;
	
	public Config() {
		prop = new Properties();
		try {
			InputStream input = Config.class.getClassLoader().getResourceAsStream("config/biosearch.properties");
			prop.load(input);
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setParams() {
		/*
		Params.DB_URL = prop.getProperty("DB_URL");
		Params.DB_USER = prop.getProperty("DB_USER");
		Params.DB_PASSWD = prop.getProperty("DB_PASSWD");
		Params.DB_SCHEMA = prop.getProperty("DB_SCHEMA");
		
		Params.DB_FUSE_URL	 = prop.getProperty("DB_FUSE_URL");
		Params.DB_FUSE_USER = prop.getProperty("DB_FUSE_USER");
		Params.DB_FUSE_PASSWD = prop.getProperty("DB_FUSE_PASSWD");
		
		Params.DB_INTEL_URL	 = prop.getProperty("DB_INTEL_URL");
		Params.DB_INTEL_USER = prop.getProperty("DB_INTEL_USER"); 
		Params.DB_INTEL_PASSWD = prop.getProperty("DB_INTEL_PASSWD");
		
		Params.LUCENE_INDEX_PATH = prop.getProperty("LUCENE_INDEX_PATH");
		
		Params.ONT_PREFIX = prop.getProperty("ONT_PREFIX");
		
		Params.SEGMENT_URL = prop.getProperty("SEGMENT_URL");
		
		*/
		Params.ONT_PATH = prop.getProperty("ONT_PATH");
	}
}
