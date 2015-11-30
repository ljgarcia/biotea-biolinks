package biolinks.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import ws.biotea.ld2rdf.util.OntologyPrefix;
import ws.biotea.ld2rdf.util.annotation.AnnotationOntologyPrefix;

public class Biolink implements Concept{
	private String id;
	private URI biolinkURI, givenDocURI, analyzedDocURI, annotatorURI;
	private Double score;
	private List<AnnotatedConcept> sharedTerms = new ArrayList<AnnotatedConcept>();	
	private String modelLabel;
	private URI modelURI;
	private List<String> groups  = new ArrayList<String>();
	
	/* OWL Descriptors */
	public final static String BIOLINK_CLASS = OntologyPrefix.BIOTEA.getURL() + "Biolink";
	public final static String BIOLINK_OP_REQUIRED_DOC = OntologyPrefix.BIOTEA.getURL() + "onQueryDocument"; 
	public final static String BIOLINK_OP_RELATED_DOC = OntologyPrefix.BIOTEA.getURL() + "onRelatedDocument";
	public final static String BIOLINK_OP_ANNOTATOR = OntologyPrefix.BIOTEA.getURL() + "annotator";
	public final static String BIOLINK_DP_SCORE = OntologyPrefix.BIOTEA.getURL() + "score";
	public final static String BIOLINK_OP_CREATED_BY = AnnotationOntologyPrefix.PAV.getURL() + "createdBy";
	public final static String BIOLINK_OP_CREATED_ON = AnnotationOntologyPrefix.PAV.getURL() + "createdOn";
	public final static String BIOLINK_OP_REFERENCED_TERM = OntologyPrefix.BIOTEA.getURL() + "link";
	public final static String BIOLINK_MODEL_CLASS = OntologyPrefix.BIOTEA.getURL() + "Model";
	public final static String BIOLINK_OP_MODEL = OntologyPrefix.BIOTEA.getURL() + "hasModel";
	public final static String BIOLINK_OP_SUBJECT = OntologyPrefix.DCTERMS.getURL() + "subject";
	public final static String BIOLINK_DP_LABEL = OntologyPrefix.RDFS.getURL() + "label";
	public final static String BIOLINK_DP_GROUP = OntologyPrefix.BIOTEA.getURL() + "group";
	public final static String BIOLINK_ID = "Biolink_";
	public final static String BIOLINK_MODEL_ID = "Model_";


	/**
	 * @return the uriGivenDoc
	 */
	public URI getGivenDocURI() {
		return givenDocURI;
	}


	/**
	 * @param uriGivenDoc the uriGivenDoc to set
	 */
	public void setGivenDocURI(URI uriGivenDoc) {
		this.givenDocURI = uriGivenDoc;
	}


	/**
	 * @return the uriAnalyzedDoc
	 */
	public URI getAnalyzedDocURI() {
		return analyzedDocURI;
	}


	/**
	 * @param uriAnalyzedDoc the uriAnalyzedDoc to set
	 */
	public void setAnalyzedDocURI(URI uriAnalyzedDoc) {
		this.analyzedDocURI = uriAnalyzedDoc;
	}


	/**
	 * @return the uriSimilarity
	 */
	public URI getURI() {
		return biolinkURI;
	}


	/**
	 * @param uriSimilarity the uriSimilarity to set
	 */
	public void setURI(URI uri) {
		this.biolinkURI = uri;
	}


	/**
	 * @return the uriAnnotator
	 */
	public URI getAnnotatorURI() {
		return annotatorURI;
	}


	/**
	 * @param uriAnnotator the uriAnnotator to set
	 */
	public void setAnnotatorURI(URI uriAnnotator) {
		this.annotatorURI = uriAnnotator;
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


	/**
	 * @return the model
	 */
	public String getModelLabel() {
		return modelLabel;
	}


	/**
	 * @param model the model to set
	 */
	public void setModelLabel(String model) {
		this.modelLabel = model;
	}

	/**
	 * @return the model
	 */
	public URI getModelURI() {
		return modelURI;
	}


	/**
	 * @param model the model to set
	 */
	public void setModelURI(URI url) {
		this.modelURI = url;
	}

	/**
	 * @return the sharedTerms
	 */
	public List<AnnotatedConcept> getSharedTerms() {
		return sharedTerms;
	}


	/**
	 * @return the groups
	 */
	public List<String> getGroups() {
		return groups;
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
