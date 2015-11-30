package biolinks.model;

import java.net.URI;

import ws.biotea.ld2rdf.util.OntologyPrefix;

public class Topic {
	private String id;
	private String topicLabel;
	private URI topicURI;
	private Double score;
	
	public final static String TOPIC_CLASS = OntologyPrefix.BIOTEA.getURL() + "Topic";
	public final static String TOPIC_DP_LABEL = OntologyPrefix.RDFS.getURL() + "label";
	public final static String TOPIC_OP_SUBJECT = OntologyPrefix.DCTERMS.getURL() + "subject"; 
	public final static String TOPIC_DP_SCORE = OntologyPrefix.BIOTEA.getURL() + "score";
	public final static String TOPIC_ID = "Topic_";
	
	public Topic() {
		this("", 0);
	}
	public Topic(String topic, double score) {
		this.topicLabel = topic;
		this.score = score;		
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return the topicLabel
	 */
	public String getTopicLabel() {
		return topicLabel;
	}
	/**
	 * @param topicLabel the topicLabel to set
	 */
	public void setTopicLabel(String topicLabel) {
		this.topicLabel = topicLabel;
	}
	/**
	 * @return the topicURI
	 */
	public URI getTopicURI() {
		return topicURI;
	}
	/**
	 * @param topicURI the topicURI to set
	 */
	public void setTopicURI(URI topicURI) {
		this.topicURI = topicURI;
	}
	/**
	 * @return the score
	 */
	public Double getScore() {
		return score;
	}
	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}
}
