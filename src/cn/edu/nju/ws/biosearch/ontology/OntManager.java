package cn.edu.nju.ws.biosearch.ontology;

/**
 * 
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import cn.edu.nju.ws.biosearch.datasource.DataSourceManager;
import cn.edu.nju.ws.biosearch.query.DatasetService;
import cn.edu.nju.ws.biosearch.utils.Config;
import cn.edu.nju.ws.biosearch.utils.Params;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * @author "Cunxin Jia"
 *
 */
public class OntManager {
	private static OntManager instance = null;
	private String namespace;
	private OntModel model;
	private OntModel modelNoInf; //ontology model without inference
	private Map<String, OntClass> classMap;
	private Set<String> classURIs;
	private Map<String, OntProperty> propMap;
	private List<OntProperty> propListed;
	public static OntManager getInstance() {
		if(instance == null) {
			instance = new OntManager();
		}
		return instance;
	}
	
	
	private OntManager() {
		Config conf = new Config();
		conf.setParams();
		
		model = ModelFactory.createOntologyModel();
		modelNoInf = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		InputStream input = OntManager.class.getClassLoader().getResourceAsStream(Params.ONT_PATH);
		InputStream input2 = OntManager.class.getClassLoader().getResourceAsStream(Params.ONT_PATH);
		namespace = Params.ONT_PREFIX;
		if(input != null) {	
			model.read(input, "RDF/XML-ABBREV");
		}
		if(input2 != null) {
			modelNoInf.read(input2, "RDF/XML-ABBREV");
		}
		classMap = new HashMap<String, OntClass> ();
		propMap = new HashMap<String, OntProperty> ();
		propListed = new ArrayList<OntProperty>();
		classURIs = new HashSet<String> ();
		initClassAndProp();
		try {
			if(input != null)
				input.close();
			if(input2 != null)
				input2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public OntModel getOntModel() {
		return model;
	}
	
	public void initClassAndProp() {
		ExtendedIterator<OntClass> clsIter = model.listClasses();
		while(clsIter.hasNext()) {
			OntClass cls = clsIter.next();
			classMap.put(cls.getLabel(null), cls);
			classURIs.add(cls.getURI());
		}
		ExtendedIterator<OntProperty> propIter = model.listAllOntProperties();
		while(propIter.hasNext()) {
			OntProperty prop = propIter.next();
			propMap.put(prop.getLabel(null), prop);
		}
		
		//add properties of local ontologies
		Set<String> sources = DataSourceManager.getInstance().listSourceNames();
		for(String source : sources) {
			Map<String, String> props = DatasetService.getProperties(source);
			for(String key : props.keySet()) {
				OntProperty prop = model.createOntProperty(props.get(key)); 
				prop.addLabel(key, "en");
				propMap.put(key, prop);
			}
		}
		//add properties of RDFS and DC 
		Property distribution = new PropertyImpl("http://www.w3.org/ns/dcat#distribution");
		Property retrievedOn = new PropertyImpl("http://purl.org/pav/retrievedOn");
		Property bio2rdf_uri = new PropertyImpl("http://bio2rdf.org/bio2rdf_vocabulary:uri");
		Property bio2rdf_identifier = new PropertyImpl("http://bio2rdf.org/bio2rdf_vocabulary:identifier");
		Property bio2rdf_namespace = new PropertyImpl("http://bio2rdf.org/bio2rdf_vocabulary:namespace");
		Property bio2rdf_xidentifiers = new PropertyImpl("http://bio2rdf.org/bio2rdf_vocabulary:x-identifiers.org");
		Property[] props = {
				RDF.type,
				RDFS.subClassOf, RDFS.seeAlso, RDFS.label,
				OWL.sameAs,
				DC.title, DC.description, DC.identifier, DC.publisher, DC.rights, DC.creator,
				DC.format, DC.source,
				FOAF.page,
				distribution, retrievedOn,
				bio2rdf_uri, bio2rdf_identifier, bio2rdf_namespace, bio2rdf_xidentifiers,
		};

		for(Property prop : props) {
			OntProperty ontProp = model.createOntProperty(prop.getURI()); 
			String key = prop.getLocalName();
			ontProp.addLabel(key, "en");
			propMap.put(key, ontProp);
			propListed.add(ontProp);
		}
	}
	
	public String getClassURI(String classLabel) {
		String uri = null;
		Resource cls = classMap.get(classLabel);
		if(cls == null) {
			cls = classMap.get(classLabel.toLowerCase());
		}
		if(cls != null)
			uri = cls.getURI();
		return uri;
	}
	
	public void registerClassLabel(String classLabel, String classURI) {
		Resource cls = classMap.get(classLabel);
		//System.out.println("***Type: " + classLabel + "\t" + classURI);
		if(cls == null) {
			OntClass ontCls = model.createClass(classURI);
			classMap.put(classLabel, ontCls);
		}
	}
	
	public String getPropURI(String propLabel) {
		String uri = null;
		Resource prop = propMap.get(propLabel);
		if(prop == null) {
			prop = propMap.get(propLabel.toLowerCase());
		}
		if(prop != null)
			uri = prop.getURI();
		return uri;
	}
	
	public boolean isClassURI(String URI) {
		return classURIs.contains(URI);
	}
	
	public Set<String> getAssociatedClass(String classURI) {
		Set<String> associatedClasses = new HashSet<String> ();
		//associatedClasses.add("http://ws.nju.edu.cn/nju28/jzj");
		
		if(isClassURI(classURI)) {
			Resource startClass = model.getResource(classURI);
			ResIterator stmtIter = model.listSubjectsWithProperty(RDFS.domain, startClass);
			while(stmtIter.hasNext()) {
				Resource property = stmtIter.next();
				
				if(isObjectProperty(property.toString())) {
					OntProperty objectProperty = model.getOntProperty(property.toString());
					NodeIterator nodeIter1 = model.listObjectsOfProperty(objectProperty, RDFS.range);
					while(nodeIter1.hasNext()) {
						RDFNode node = nodeIter1.next();
						if(node.isResource()) {
							Resource middleClass = node.asResource();
							if(!middleClass.toString().endsWith("ValueWithUnit")) {
								
								ResIterator resIter2 = model.listSubjectsWithProperty(RDFS.domain, middleClass);
								while(resIter2.hasNext()) {
									Resource midProp = resIter2.next();
									if(isObjectProperty(midProp.toString())) {
										NodeIterator stmtIter2 = model.listObjectsOfProperty(midProp, RDFS.domain);
										while(stmtIter2.hasNext()) {
											RDFNode associatedClass = stmtIter2.next();
											associatedClasses.add(associatedClass.toString());
//											System.out.println(associatedClass);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return associatedClasses;
	}
	
	public boolean isObjectProperty(String propURI) {
		OntProperty property = model.getOntProperty(propURI);
		if(property != null && property.isObjectProperty()) {
			return true;
		}
		return false;
	}
	
	public List<String> getForwardProp(List<String> uris) {
		List<String> props = new ArrayList<String> ();
		for(String uri : uris) {
			props.addAll(getForwardProp(uri));
		}
		return props;
	}
	
	public List<String> getForwardProp(String uri) {
		Resource s = new ResourceImpl(uri);
		List<String> props = new ArrayList<String> ();
		ResIterator iter = model.listSubjectsWithProperty(RDFS.domain, s);
		while(iter.hasNext()) {
			Resource r = iter.next();
			props.add(r.getURI());
		}
		return props;
	}
	
	public List<String> getForwardDatatypeProp(String uri) {
		Resource s = new ResourceImpl(uri);
		List<String> props = new ArrayList<String> ();
		ResIterator iter = model.listSubjectsWithProperty(RDFS.domain, s);
		while(iter.hasNext()) {
			Resource r = iter.next();
			if(isDatatypeProperty(r) || isCompoundValueProperty(r.toString())) {
				NodeIterator rangeIter = model.listObjectsOfProperty(r, RDFS.range);
				while(rangeIter.hasNext()) {
					RDFNode range = rangeIter.next();
					if(range.isResource() && ((range.asResource()).equals(XSD.xint) ||
							 (range.asResource()).getURI().equals("http://ws.nju.edu.cn/nju28/ValueWithUnit")) ) {
						props.add(r.getURI());
					}
				}
			}
		}
		return props;
	}
	
	public boolean isDatatypeProperty(Resource property) {
		boolean isDTP = false;
		NodeIterator typeIter = model.listObjectsOfProperty(property, RDF.type);
		while(typeIter.hasNext()) {
			RDFNode type = typeIter.next();
			if(type.isResource() && (type.asResource()).equals(OWL.DatatypeProperty)) {
				isDTP = true;
				break;
			}
		}
		return isDTP;
	}
	
	public List<String> getBackwardProp(String uri) {
		Resource s = new ResourceImpl(uri);
		List<String> props = new ArrayList<String> ();
		ResIterator iter = model.listSubjectsWithProperty(RDFS.range, s);
		while(iter.hasNext()) {
			Resource r = iter.next();
			props.add(r.getURI());
		}
		return props;
	}
	
	public String getLabel(String uri) {
		String label = null;
		Resource s = new ResourceImpl(uri);
		NodeIterator iter = model.listObjectsOfProperty(s, RDFS.label);
		if(iter.hasNext()) {
			RDFNode labelNode = iter.next();
			label = labelNode.toString();
		}
		return label;
	}
	
	public boolean isCompoundValueProperty(String uri) {
		Resource prop = new ResourceImpl(uri);
		NodeIterator iter = model.listObjectsOfProperty(prop, RDFS.range);
		if(!iter.hasNext()) {
			return false;
		}
		while(iter.hasNext()) {
			RDFNode node = iter.next();
			if(node instanceof Resource) {
				String rangeURI = ((Resource) node).getURI();
				if(!rangeURI.equals(namespace + "ValueWithUnit")) {
					return false;
				}
			}
		}
		return true;
	}
	
	public List<OntClass> listClasses() {
		List<OntClass> classes = new ArrayList<OntClass> (classMap.values());
		return classes;
	}
	
	public List<OntProperty> listProperties() {
//		List<OntProperty> props = new ArrayList<OntProperty> (propMap.values());
//		return props;
		return propListed;
	}
	
	public List<OntClass> listTopClasses() {
		ExtendedIterator<OntClass> iter =  model.listClasses();
		List<OntClass> clsList = new ArrayList<OntClass> ();
		while(iter.hasNext()) {
			OntClass cls = iter.next();			
			if(getNumOfSupClasses(cls) == 1) {
				clsList.add(cls);
			}
		}
		return clsList;
	}
	
	public List<String> listSuperClasses(String uri) {
		List<String> superClasses = new ArrayList<String>();
		superClasses.add(uri);
		OntClass ontClass = model.getOntClass(uri);
		if(ontClass != null) {
			ExtendedIterator<OntClass> iter = ontClass.listSuperClasses();
			while (iter.hasNext()) {
				String superClass = iter.next().getURI();
				if (superClass.equals(RDFS.Resource.getURI()))
					continue;
				superClasses.add(superClass);
			}
		}
		return superClasses;	
	}
	
	public List<OntClass> listSubClasses(OntClass cls) {
		List<OntClass> clsList = new ArrayList<OntClass> ();
//		clsList.add(cls);
		ExtendedIterator<OntClass> iter =  cls.listSubClasses();
		int clsLayer = getNumOfSupClasses(cls);
		while(iter.hasNext()) {
			OntClass subcls = iter.next();
			int subclsLayer = getNumOfSupClasses(subcls);
			if(subclsLayer == (clsLayer + 1)) {
				clsList.add(subcls);
			}
		}
		return clsList;
	}
	
	public List<String> getSuperProperties(String propURI) {
		List<String> superProps = new ArrayList<String> ();
		OntProperty prop = modelNoInf.getOntProperty(propURI);
		if(prop != null) {
			NodeIterator iter = modelNoInf.listObjectsOfProperty(prop, RDFS.subPropertyOf);
			while(iter.hasNext()) {
				RDFNode node = iter.next();
				String propString= ((Resource) node).getURI();
				superProps.add(propString);
			}
		}
		return superProps;
	}
	
	public OntClass getClassByLabel(String label) {
		OntClass cls = null;
		ExtendedIterator<OntClass> iter = model.listClasses();
		while(iter.hasNext()) {
			OntClass curCls = iter.next();
			if(curCls.getLabel(null).equals(label)) {
				cls = curCls;
				break;
			}
		}
		return cls;
	}
	
	public int getNumOfSupClasses(OntClass cls) {
		int count = 0;
		ExtendedIterator<OntClass> supClsIter = cls.listSuperClasses();
		while(supClsIter.hasNext()) {
			supClsIter.next();
			count ++;
		}
		return count;
	}
	
	public PropertyTree getPropertyHierarchy (JSONArray pvArray) {
		Map<String, PropertyTree> propTreeMap = new HashMap<String, PropertyTree> ();
		List<String> props = new ArrayList<String> ();
		for(int i = 0; i < pvArray.size(); i++) {
//			System.out.println(pvArray.get(i).toString());
			JSONObject pv = (JSONObject)pvArray.get(i);
			String prop = pv.get("prop").toString();
			String URI = pv.get("URI").toString();
			String isObject = pv.get("isObject").toString();
			Object value = pv.get("value");
			JSONObject valueJSON = new JSONObject();
			if(value instanceof JSONArray) {
				valueJSON.put("multi", value);
			}
			else if(value instanceof String) {
				valueJSON.put("single", value);
			}
//			String propLabel = getLabel(prop);
			propTreeMap.put(prop, new PropertyTree(prop, URI, valueJSON, isObject));
			props.add(prop);
		}
		
		for(int i = 0; i < props.size(); i++) {
			String prop = props.get(i);
			List<String> superProps = getSuperProperties(prop);
			for(String superProp : superProps) {
				PropertyTree subTree = propTreeMap.get(prop);
				PropertyTree tree;
				if(propTreeMap.containsKey(superProp)) {
					tree = propTreeMap.get(superProp);
				}
				else {
					String superPropLabel = getLabel(superProp);
					tree = new PropertyTree(superPropLabel, "", "");
					props.add(superProp);
				}
				tree.addSubTree(subTree);
				propTreeMap.put(superProp, tree);
				propTreeMap.remove(prop);
			}
		}
		
		PropertyTree tree = new PropertyTree("Property", "", "");
		for(PropertyTree subTree : propTreeMap.values()) {
			tree.addSubTree(subTree);
		}
		tree.sort();
		return tree;
	}
	
	
	public static void main(String[] args) {
		Config conf = new Config();
		conf.setParams();
		OntManager om = new OntManager();
		om.getAssociatedClass("http://ws.nju.edu.cn/nju28/hkmj");
		om.getAssociatedClass("http://ws.nju.edu.cn/nju28/bd");
		om.getAssociatedClass("http://ws.nju.edu.cn/nju28/ry");
//		om.getAssociatedClass("http://ws.nju.edu.cn/nju28/bs");
//		om.getAssociatedClass("http://ws.nju.edu.cn/nju28/dd");
		
	}
}
