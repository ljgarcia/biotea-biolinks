package biolinks.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import ws.biotea.ld2rdf.util.OntologyPrefix;

public class AnnotatedConcept {
	private String id;
	private URI uri;
	private List<URI> topics = new ArrayList<URI>();
	private Integer tf;
	private Double idf;
	
	public final static String ANNOT_CON_CLASS = OntologyPrefix.BIOTEA.getURL() + "SemanticAnnotation";
	public final static String BIOTEA_OCURRENCES = OntologyPrefix.BIOTEA.getURL() + "tf";
	public final static String BIOTEA_IDF = OntologyPrefix.BIOTEA.getURL() + "idf";
	public final static String ANNOT_CON_OP_CONCEPT = OntologyPrefix.DCTERMS.getURL() + "references";
	public final static String ANNOT_CON_ID = "AnnotatedConcept_";

	/**
	 * @return the uri
	 */
	public URI getUri() {
		return uri;
	}

	/**
	 * @param uri the uri to set
	 */
	public void setUri(URI uri) {
		this.uri = uri;
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
	 * @return the idf
	 */
	public Double getIDF() {
		return idf;
	}

	/**
	 * @param idf the idf to set
	 */
	public void setIDF(double idf) {
		this.idf = idf;
	}

	/**
	 * @return the conceptURI
	 */
	public List<URI> getTopics() {
		return topics;
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

}
