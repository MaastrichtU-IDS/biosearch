package cn.edu.nju.ws.biosearch.fusion;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import cn.edu.nju.ws.biosearch.datasource.DataSourceManager;


public class OntMappingService {
	private Map<String, Map<String, ArrayList<String>>> mappings;
	private Set<String> mapped;
	private static OntMappingService inst = null;
	
	public static OntMappingService getInstance() {
		if(inst == null)
			inst = new OntMappingService();
		return inst;
	}

	private OntMappingService() {
		this.mappings = new HashMap<String, Map<String, ArrayList<String>>> ();
		this.mapped = new HashSet<String> ();
		Set<String> sources = DataSourceManager.getInstance().listSourceNames();
		for(String source : sources) {
			readMappingFromFile(source);
		}
	}
	
	public static boolean isMapped(String uri) {
		return getInstance().mapped.contains(uri);
	}
	
	public void readMappingFromFile(String source) {
		InputStream input = OntMappingService.class.getClassLoader().getResourceAsStream("mappings/" + source);
		if(input == null)
			return;
		Scanner scanner = null;
		Map<String, ArrayList<String>> mapping = new HashMap<String, ArrayList<String>> ();
		try {
			scanner = new Scanner(input);
			while(scanner.hasNext()) {
				String line = scanner.nextLine();
				String[] splitted = line.split(" ");
				if(splitted.length == 2) {
					if(mapping.containsKey(splitted[1])) {
						ArrayList<String> classes = mapping.get(splitted[1]);
						classes.add(splitted[0]);
					} else {
						ArrayList<String> classes = new ArrayList<String>();
						classes.add(splitted[0]);
						mapping.put(splitted[1], classes);
					}
					mapped.add(splitted[1]);
				}
			}
		} finally {
			if(scanner != null)
				scanner.close();
		}
		mappings.put(source, mapping);
	}
	
	public static ArrayList<String> getMapping(String uri, String source) {
		Map<String, ArrayList<String>> mapping = getInstance().mappings.get(source);
		if(mapping == null) 
			return null;
		return mapping.get(uri);
	}
	
}
