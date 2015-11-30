package biolinks.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import ws.biotea.ld2rdf.util.OntologyPrefix;
import ws.biotea.ld2rdf.util.annotation.AnnotationOntologyPrefix;

public class TopicDistribution implements Concept {
	private String id;
	private URI distributionURI, docURI, annotatorURI;
	private Integer tf;
	private String modelLabel;
	private URI modelURI;
	private List<Topic> topics;
	
	public final static String DISTRIBUTION_CLASS = OntologyPrefix.BIOTEA.getURL() + "TopicDistribution";
	public final static String BIOTEA_TOTAL_TF = OntologyPrefix.BIOTEA.getURL() + "totalTF";
	public final static String DISTRIBUTION_OP_REQUIRED_DOC = OntologyPrefix.BIOTEA.getURL() + "onDocument";
	public final static String DISTRIBUTION_OP_ANNOTATOR = OntologyPrefix.BIOTEA.getURL() + "annotator";
	public final static String DISTRIBUTION_OP_CREATED_BY = AnnotationOntologyPrefix.PAV.getURL() + "createdBy";
	public final static String DISTRIBUTION_OP_CREATED_ON = AnnotationOntologyPrefix.PAV.getURL() + "createdOn";
	public final static String DISTRIBUTION_OP_HAS_MODEL = OntologyPrefix.BIOTEA.getURL() + "hasModel";
	public final static String DISTRIBUTION_OP_HAS_TOPIC = OntologyPrefix.BIOTEA.getURL() + "hasTopic";
	public final static String MODEL_OP_SUBJECT = OntologyPrefix.DCTERMS.getURL() + "subject";
	public final static String MODEL_DP_LABEL = OntologyPrefix.RDFS.getURL() + "label";
	public final static String DISTRIBUTION_ID = "Distribution_";
	
	public TopicDistribution() {
		this.topics = new ArrayList<Topic>();
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
	 * @return the distributionURI
	 */
	public URI getURI() {
		return distributionURI;
	}

	/**
	 * @param distributionURI the distributionURI to set
	 */
	public void setURI(URI distributionURI) {
		this.distributionURI = distributionURI;
	}

	/**
	 * @return the docURI
	 */
	public URI getDocURI() {
		return docURI;
	}

	/**
	 * @param docURI the docURI to set
	 */
	public void setDocURI(URI docURI) {
		this.docURI = docURI;
	}

	/**
	 * @return the annotatorURI
	 */
	public URI getAnnotatorURI() {
		return annotatorURI;
	}

	/**
	 * @param annotatorURI the annotatorURI to set
	 */
	public void setAnnotatorURI(URI annotatorURI) {
		this.annotatorURI = annotatorURI;
	}

	/**
	 * @return the tf
	 */
	public Integer getTF() {
		return tf;
	}

	/**
	 * @param tf the tf to set
	 */
	public void setTF(int tf) {
		this.tf = tf;
	}

	/**
	 * @return the modelLabel
	 */
	public String getModelLabel() {
		return modelLabel;
	}

	/**
	 * @param modelLabel the modelLabel to set
	 */
	public void setModelLabel(String modelLabel) {
		this.modelLabel = modelLabel;
	}

	/**
	 * @return the modelURI
	 */
	public URI getModelURI() {
		return modelURI;
	}

	/**
	 * @param modelURI the modelURI to set
	 */
	public void setModelURI(URI modelURI) {
		this.modelURI = modelURI;
	}

	/**
	 * @return the topics
	 */
	public List<Topic> getTopics() {
		return topics;
	}
}