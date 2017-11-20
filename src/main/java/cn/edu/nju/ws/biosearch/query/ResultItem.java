package cn.edu.nju.ws.biosearch.query;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class ResultItem {
	
	public static class ResultItemComparator implements Comparator<ResultItem> {
		@Override
		public int compare(ResultItem arg0, ResultItem arg1) {
			return Double.compare(arg1.getScore(), arg0.getScore());
		}
	}
	
	private String uri;
	private String source;
	private String label;
	private List<String> type;
	private String typeLabel;
	private String image;
	private Map<String, String> additionalInfo;
	private Map<String, JSONArray> payload;
	private double score;

	public ResultItem(String uri) {
		this(uri, DatasetService.getSource(uri));
	}
	
	public String get(String key) {
		return additionalInfo.get(key);
	}
	
	public String getURI() {
		return uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public String getSource() {
		return source;
	}
	
	public double getScore() {
		return score;
	}
	
	public void addScore(double score) {
		this.score += score;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public ResultItem(String uri, String source) {
		this.uri = uri;
		this.source = source;
		this.label = DatasetService.getLabel(uri);
		if(label == null) {
			label = new ResourceImpl(uri).getLocalName();
		}
		if(label == null || label.equals("")) {
			label = DatasetService.simpleLabel(uri);
		}
		this.type = DatasetService.getType(uri, source);
		if(this.type.size() > 0)
			this.typeLabel = DatasetService.getLabel(this.type.get(0));
		if(typeLabel == null && this.type.size() > 0) {
			typeLabel = new ResourceImpl(type.get(0)).getLocalName();
		}
		this.image = ImageService.getImage(uri);
		this.additionalInfo = new HashMap<String, String> ();
		this.payload = new HashMap<String, JSONArray> ();
		this.score = 0.0;
	}
	
	public void put(String key, String value) {
		additionalInfo.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("uri", uri);
		if(source != null)
			json.put("source", source);
		if(label != null)
			json.put("label", label);
		if(type != null)
			json.put("type", type);
		if(typeLabel != null)
			json.put("typeLabel", typeLabel);
		if(image != null)
			json.put("image", image);
		
		for(String key : additionalInfo.keySet()) {
			json.put(key, additionalInfo.get(key));
		}

		for(String key : payload.keySet()) {
			json.put(key, payload.get(key));
		}

		return json;
	}

	public List<String> getType() {
		return type;
	}

	public void put(String key, JSONArray jsonArray) {
		payload.put(key, jsonArray);
	}
}
