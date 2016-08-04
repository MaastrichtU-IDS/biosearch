package cn.edu.nju.ws.biosearch.classTree;

/**
 * 
 */

import java.util.List;

import org.json.simple.JSONObject;

/**
 * @author "Cunxin Jia"
 *
 */
public class TreeNode implements Comparable<TreeNode>{
	private String label;
	private int count;
	private String uri;
	private ClassTree subTree;
	
	
	
	/**
	 * @param label
	 * @param uri
	 */
	public TreeNode(String label, int count, String uri) {
		this.label = label;
		this.count = count;
		this.uri = uri;
		this.subTree = null;
	}
	/**
	 * @param label
	 * @param uri
	 * @param subTree
	 */
	public TreeNode(String label,  String uri, ClassTree subTree) {
		this.label = label;
		this.uri = uri;
		this.subTree = subTree;
		
		List<TreeNode> nodes = subTree.getNodes();
		for(TreeNode node : nodes) {
			count += node.getCount();
		}
	}
	
	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}
	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return the url
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @param url the uri to set
	 */
	public void setUrl(String uri) {
		this.uri = uri;
	}
	/**
	 * @return the subTree
	 */
	public ClassTree getSubTree() {
		return subTree;
	}
	/**
	 * @param subTree the subTree to set
	 */
	public void setSubTree(ClassTree subTree) {
		this.subTree = subTree;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		JSONObject json = new JSONObject();
		json.put("label", label);
		json.put("count", count);
		json.put("uri", uri);
		if(subTree != null) {
			json.put("subTree", subTree);
		}
		return json.toString();
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TreeNode o) {
		if(this.count != o.count) {
			return o.count - this.count;
		}
		int thisNum = 0;
		int thatNum = 0;
		if(subTree != null) {
			thisNum = subTree.getNodes().size();
		}
		if(o != null && o.getSubTree() != null) {
			thatNum = o.getSubTree().getNodes().size();
		}
		return thatNum - thisNum;
	}
}


