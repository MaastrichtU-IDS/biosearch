package cn.edu.nju.ws.biosearch.classTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

public class ClassGroup {
	private static volatile ClassGroup inst = null;
	private ArrayList<String> classGroups;
	
	public static ClassGroup getInstance() {
		if(inst == null) {
			synchronized(ClassGroup.class) {
				if(inst == null) {
					inst = new ClassGroup();
				}
			}
		}
		return inst;
	}
	
	private ClassGroup() {
		readClassGroups();
	}
	
	private  void readClassGroups() {
		classGroups = new ArrayList<String>();
		Properties props = new Properties();
		try {
			props.load(ClassGroup.class.getClassLoader().getResourceAsStream("config/classfiltergroups.properties"));
			String groups = (String) props.get("GROUPS");
			if(groups == null) return;
			String[] groupsArray = groups.split(";");
			for(String group: groupsArray) {
				classGroups.add(group);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Collections.sort(classGroups);
	}
	
	public ArrayList<String> getClassGroups() {
		return classGroups;
	}
}