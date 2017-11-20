package cn.edu.nju.ws.biosearch.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.edu.nju.ws.biosearch.datasource.DataSourceManager;

public class ExecutorManager {
	private Map<String, SPARQLQueryExecutor> executors;
	private static volatile ExecutorManager inst = null;
	
	public static ExecutorManager getInstance() {
		if(inst == null)
			synchronized(ExecutorManager.class) {
				if(inst == null) {
					inst = new ExecutorManager();
				}
			}
		return inst;
	}
	
	private ExecutorManager() {
		executors = new HashMap<String, SPARQLQueryExecutor> ();
		Set<String> names = DataSourceManager.getInstance().listSourceNames();
		for(String name : names) {
			executors.put(name, new VirtuosoSPARQLQueryExecutor(name));
		}
	}
	
	public SPARQLQueryExecutor getExecutor(String name) {
		if(name == null || !executors.containsKey(name))
			return null;
		executors.put(name, new VirtuosoSPARQLQueryExecutor(name));
		return executors.get(name);
	}
}
