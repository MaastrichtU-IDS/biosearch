package cn.edu.nju.ws.biosearch.query;

public class ResultScorer {
	public static double getScore() {
		return 0.0;
	}

	public static double getScore(String queryItems, String value) {
		double lengthRatio = (double) queryItems.length() / value.length();
		return lengthRatio;
	}
}
