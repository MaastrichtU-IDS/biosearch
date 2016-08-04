package cn.edu.nju.ws.biosearch.ontology;

/**
 * 
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * @author "Cunxin Jia"
 *
 */

public class PropertyTree implements Comparable<Object> {
	String property;
	String URI;
	String isObject;
	String source;
	JSONObject value;
	List<PropertyTree> children;
	
	public PropertyTree (String property, String URI, String isObject, String source) {
		this.property = property;
		this.URI = URI;
		this.isObject = isObject;
		this.source = source;
		children = new ArrayList<PropertyTree> ();
	}
	
	public PropertyTree(String property, String URI, JSONObject value, String isObject, String source) {
		this.property = property;
		this.URI = URI;
		this.isObject = isObject;
		this.value = value;
		this.source = source;
		children = new ArrayList<PropertyTree> ();
	}
	
	public List<PropertyTree> getChildren () {
		return children;
	}
	
	public String getProperty() {
		return property;
	}
	
	public String getURI() {
		return URI;
	}
	
	public String getSource() {
		return source;
	}

	public void addSubTree(PropertyTree subTree) {
		children.add(subTree);
	}
	
	public void print() {
		System.out.println(property);
		for(PropertyTree subTree : children) {
			subTree.print();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		JSONObject json = new JSONObject();
		json.put("property", property);
		if(!URI.equals("")) {
			json.put("URI", URI);
		}
		if(!isObject.equals("")) {
			json.put("isObject", isObject);
		}
		if(!source.equals("")) {
			json.put("source", source);
		}
		if(value != null)
			json.put("value", value);
		if(children.size() > 0) {
			JSONArray subtreeJSON = new JSONArray();
			for(PropertyTree subtree : children) {
				subtreeJSON.add(subtree);
			}
			json.put("subtree", subtreeJSON);
		}
		return json.toString();
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		PropertyTree pt = (PropertyTree) o;
		if(this.URI != null && pt.getURI() != null) {
			return this.URI.compareTo(pt.getURI());
		} else {
			return this.property.compareTo(pt.getProperty());
		}
	}
	
	public void sort() {
		Collections.sort(children);
		for(int i = 0; i < children.size(); i ++) {
			children.get(i).sort();
		}
	}
}