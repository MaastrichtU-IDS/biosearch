package cn.edu.nju.ws.biosearch.classTree;

/**
 * 
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import cn.edu.nju.ws.biosearch.fusion.OntMappingService;
import cn.edu.nju.ws.biosearch.ontology.OntManager;
import cn.edu.nju.ws.biosearch.query.DatasetService;

import com.hp.hpl.jena.ontology.OntClass;

/**
 * @author "Cunxin Jia"
 *
 */
public class ClassTreeGenerator {
	
	private Map<String, Integer> classMap;
	private HashSet<String> othersClasses;
	private OntManager om = OntManager.getInstance();
	
	
	public ClassTreeGenerator(Map<String, Integer> classList) {
		this.classMap = classList;
	}
	
	public ClassTree getClassTree() {
		othersClasses = new HashSet<String>();
		for(String uri : classMap.keySet()) {
			othersClasses.add(uri);
		}

		ClassTree classTree = new ClassTree();
		ArrayList<String> classGroups = ClassGroup.getInstance().getClassGroups();
		if(classGroups.size() <= 0) {
			return getOthersClassTree();
		}

		for(String classGroup : classGroups) {
			String label = classGroup;
			String sioUri = om.getClassURI(label);
			ClassTree subTree = getSubClassTree(sioUri);
			if(subTree.getNodes().size() > 0) {
				TreeNode node = new TreeNode(label, sioUri, subTree);
				classTree.addNode(node);
			}
		}
		
		String label = "others";
		String sioUri = "others";
		ClassTree subTree = getOthersClassTree();
		if(subTree.getNodes().size() > 0) {
			TreeNode node = new TreeNode(label, sioUri, subTree);
			classTree.addNode(node);
		}
		
		return classTree;
	}

	private ClassTree getSubClassTree(String sioUri) {
		ClassTree classTree = new ClassTree();
		HashSet<String> groupedClasses = new HashSet<String>();

		for(String uri : othersClasses) {
			int count = getClassCount(uri);
			if(count > 0) {
				String namespace = DatasetService.getSource(uri);
				HashSet<String> uris = OntMappingService.getMapping(sioUri, namespace);
				if(uris == null) {
					continue;
				}
				if(uris.contains(uri)) {
					String label = DatasetService.getLabel(uri);
					if(label == null || namespace == null || label.trim().equals(""))
						continue;
					label = label.replaceAll("\\[\\w+:.+\\]", "");
					String lowCaseLabel = label;
					if(lowCaseLabel.toLowerCase().startsWith(namespace.toLowerCase())) {
						label = label.substring(namespace.length()+1);
					}
					label = namespace + " " + label;
					OntManager.getInstance().registerClassLabel(label, uri);
					TreeNode node = new TreeNode(label, count, uri);
					classTree.addNode(node);
					
					groupedClasses.add(uri);
				}
			}
		}
		
		for(String uri : groupedClasses) {
			othersClasses.remove(uri);
		}

		classTree.sortNodes();
		return classTree;
	}
	
	private ClassTree getOthersClassTree() {
		ClassTree classTree = new ClassTree();
		for(String uri : othersClasses) {
			int count = getClassCount(uri);
			if(count > 0) {
				String namespace = DatasetService.getSource(uri);
				String label = DatasetService.getLabel(uri);
				if(label == null || namespace == null || label.trim().equals(""))
					continue;
				label = label.replaceAll("\\[\\w+:.+\\]", "");
				String lowCaseLabel = label;
				if(lowCaseLabel.toLowerCase().startsWith(namespace.toLowerCase())) {
					label = label.substring(namespace.length()+1);
				}
				label = namespace + " " + label;
				OntManager.getInstance().registerClassLabel(label, uri);
				TreeNode node = new TreeNode(label, count, uri);
				classTree.addNode(node);
			}
		}
		
		classTree.sortNodes();
		return classTree;
	}
	
	public int getClassCount(String uri) {
		Integer count = classMap.get(uri);
		return (count == null) ? 0 : count;
	}
	
	public ClassTree constructSubTree(OntClass cls) {
		ClassTree classTree = null;
		List<OntClass> clsList = OntManager.getInstance().listSubClasses(cls);
		if(clsList.size() != 0) {
			classTree = new ClassTree();
			for(OntClass subCls : clsList) {
				String uri = subCls.getURI();
				int count = getClassCount(uri);
				if(count > 0) {
					String label = subCls.getLabel(null);
					TreeNode node = new TreeNode(label, count, uri);
					ClassTree subTree = constructSubTree(subCls);
					node.setSubTree(subTree);
					classTree.addNode(node);
				}
			}
		}
		if(classTree != null)
			classTree.sortNodes();
		return classTree;
	}
}
