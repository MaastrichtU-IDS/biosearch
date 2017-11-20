package cn.edu.nju.ws.biosearch;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.Logger;

import cn.edu.nju.ws.biosearch.ontology.OntManager;

@WebListener
public class Config implements ServletContextListener {
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
    public void contextInitialized(ServletContextEvent event) {
    	logger.info("Starting to load OntManager. This can take a couple of minutes");
		long start = System.currentTimeMillis();
        OntManager.getInstance();
        logger.info("OntManager loaded (" + ((System.currentTimeMillis() - start)/1000) + " s)");
    }
    
    public void contextDestroyed(ServletContextEvent event) {
        // Do your thing during webapp's shutdown.
    }
}
