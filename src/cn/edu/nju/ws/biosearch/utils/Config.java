/**
 * 
 */
package cn.edu.nju.ws.biosearch.utils;

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
		Params.ONT_PATH = prop.getProperty("ONT_PATH");
	}
}
