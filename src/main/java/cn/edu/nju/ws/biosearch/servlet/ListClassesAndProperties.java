package cn.edu.nju.ws.biosearch.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

import cn.edu.nju.ws.biosearch.datasource.DataSourceManager;
import cn.edu.nju.ws.biosearch.fusion.OntMappingService;
import cn.edu.nju.ws.biosearch.ontology.OntManager;

/**
 * Servlet implementation class ListClassesAndProperties
 */
@WebServlet("/listClassesAndProperties")
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
	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=UTF-8");
		String json = request.getParameter("term");
		if(json != null){
			OntManager om = OntManager.getInstance();
			List<OntClass> classes = om.listClasses();
			List<OntProperty> props = om.listProperties();
			List<String> items = new ArrayList<String>();
			JSONArray array = new JSONArray();
			
			for(OntClass cls : classes) {
				if(OntMappingService.isMapped(cls.getURI())) {
					items.add("C:" + cls.getLabel(null));
				}
			}
			
			for(OntProperty prop : props) {
				String propLabel = prop.getLabel(null);
				if(propLabel != null && !propLabel.trim().equals("")) {
					items.add("P:" + propLabel);
				}
			}
			
			Set<String> sources = DataSourceManager.getInstance().listSourceNames();
			for(String source : sources) {
				items.add("S:" + source);
			}
			
			Collections.sort(items);
			for(String item : items) {
				JSONObject it = new JSONObject();
				it.put("label", item);
				it.put("value", item);
				array.add(it);
			}
			
			response.getWriter().print(array.toString());
		}
	}
}
