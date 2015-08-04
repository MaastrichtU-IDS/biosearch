package cn.edu.nju.ws.biosearch.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import cn.edu.nju.ws.biosearch.datasource.DataSourceManager;
import cn.edu.nju.ws.biosearch.fusion.OntMappingService;
import cn.edu.nju.ws.biosearch.ontology.OntManager;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

/**
 * Servlet implementation class ListClassesAndProperties
 */
//@WebServlet("/listClassesAndProperties")
public class ListClassesAndProperties extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ListClassesAndProperties() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		this.doPost(request, response);
		
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		String json = request.getParameter("term");
		if(json != null){
			
			
//			List<String> clsArray = null;
//			List<String> propArray = null;
//			clsArray = new CompleteWrapperForCls().tip(100);
//			propArray = new CompleteWrapperForProp().tip(100);
//			JSONArray elements = new JSONArray();
//			for(String item : clsArray){
//				JSONObject jsob = new JSONObject();
//				jsob.put("name", item);
//				elements.add("C:" + item);
//			}
//			for(String item : propArray){
//				JSONObject jsob = new JSONObject();
//				jsob.put("name", item);
//				elements.add("P:" + item);
//			}
//			JSONObject result = new JSONObject();
//			result.put("result", elements);
//			response.getWriter().print(elements);
			OntManager om = OntManager.getInstance();
//			KeywordSearcher searcher = KeywordSearcher.getInstance();
			List<OntClass> classes = om.listClasses();
			List<OntProperty> props = om.listProperties();
			JSONArray array = new JSONArray();
			
			for(OntClass cls : classes) {
				if(OntMappingService.isMapped(cls.getURI())) {
					JSONObject item = new JSONObject();
					item.put("label", "C:" + cls.getLabel(null));
					item.put("value", "C:" + cls.getLabel(null));
					//array.add("C:" + cls.getLabel(null));
					array.add(item);
				}
			}
			
			for(OntProperty prop : props) {
				String propLabel = prop.getLabel(null);
				if(propLabel != null && !propLabel.trim().equals("")) {
					JSONObject item = new JSONObject();
					item.put("label", "P:" + propLabel);
					item.put("value", "P:" + propLabel);
					array.add(item);
//					if(om.isCompoundValueProperty(prop.getURI())) {
				//		array.add("P:" + propLabel + "=?");
				//		array.add("P:" + propLabel + "=[]");
//						JSONObject item1 = new JSONObject();
//						JSONObject item2 = new JSONObject();
//						item1.put("label", "P:" + propLabel + "=?");
//						item1.put("value", "P:" + propLabel + "=?");
//						array.add(item1);
//						item2.put("label", "P:" + propLabel + "=[]");
//						item2.put("value", "P:" + propLabel + "=[]");
//						array.add(item2);
//					}
				}
			}
//			Set<String> insts = searcher.listInstanceLabel();
//			for(String inst : insts) {
//				String[] splitted = inst.split("\t");
//				if(splitted.length == 2) {
//					String name = splitted[0];
//					String type = splitted[1];
//					JSONObject item = new JSONObject();
//					item.put("label", name + "[" + type + "]");
//					item.put("value", name);
//					array.add(item);
//				}
//			}
			
			Set<String> sources = DataSourceManager.getInstance().listSourceNames();
			for(String source : sources) {
				JSONObject item = new JSONObject();
				item.put("label", "S:" + source);
				item.put("value", "S:" + source);
				array.add(item);
			}

			response.getWriter().print(array.toString());
		}
	}
}
