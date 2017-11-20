package cn.edu.nju.ws.biosearch.query;

public class ImageService {

	public static String getImage(String uri) {
		String source = DatasetService.getSource(uri);
		if(source == null) return null;
		if(source.equals("drugbank"))
			return getDrugbankImage(uri);
		else if(source.equals("kegg"))
			return getKeggImage(uri);
		return null;
	}
	
	private static String getDrugbankImage(String uri) {
		String[] splitted = uri.split(":");
		if(splitted.length != 3) return null;
		String id = splitted[2];
		if(!id.startsWith("DB")) return null;
		String img = String.format("http://moldb.wishartlab.com/molecules/%s/image.png", id);
		return img;
	}

	private static String getKeggImage(String uri) {
		String[] splitted = uri.split(":");
		if(splitted.length != 3) return null;
		String id = splitted[2];
		String img = String.format("http://www.kegg.jp/Fig/drug/%s.gif", id);
		return img;
	}
}
