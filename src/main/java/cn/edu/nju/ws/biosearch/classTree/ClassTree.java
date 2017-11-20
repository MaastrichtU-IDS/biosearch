package cn.edu.nju.ws.biosearch.classTree;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;

/**
 * @author "Cunxin Jia"
 *
 */
public class ClassTree {
	private List<TreeNode> nodes;

	public ClassTree() {
		nodes = new ArrayList<TreeNode>();
	}
	
	public ClassTree(TreeNode node) {
		nodes = new ArrayList<TreeNode>();
		nodes.add(node);
	}
	/**
	 * @param nodes
	 */
	public ClassTree(List<TreeNode> nodes) {
		this.nodes = nodes;
	}
	
	public void addNode(TreeNode node) {
		nodes.add(node);
	}

	/**
	 * @return the nodes
	 */
	public List<TreeNode> getNodes() {
		return nodes;
	}

	/**
	 * @param nodes the nodes to set
	 */
	public void setNodes(List<TreeNode> nodes) {
		this.nodes = nodes;
	}
	
	public void sortNodes() {
		Collections.sort(nodes);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		JSONArray jsonArray = new JSONArray();
		for(TreeNode node : nodes) {
			jsonArray.add(node);
		}
		return jsonArray.toString();
	}
}