package biolinks.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import ws.biotea.ld2rdf.annotation.exception.ArticleParserException;
import ws.biotea.ld2rdf.annotation.exception.NoResponseException;
import ws.biotea.ld2rdf.annotation.parser.AnnotatorParser;
import ws.biotea.ld2rdf.annotation.parser.CMAParser;
import ws.biotea.ld2rdf.exception.RDFModelIOException;
import ws.biotea.ld2rdf.rdf.model.ao.FoafAgent;
import ws.biotea.ld2rdf.rdf.model.ao.Topic;
import ws.biotea.ld2rdf.rdf.model.aoextended.AnnotationE;
import ws.biotea.ld2rdf.rdf.persistence.AnnotationOWLReader;
import ws.biotea.ld2rdf.util.ResourceConfig;
import biolinks.model.AnnotatedConcept;
import biolinks.model.Biolink;
import biolinks.persistence.ObjectModelDAO;
import biolinks.util.BiolinksResourceConfig;
import ws.biotea.ld2rdf.util.annotation.Annotator;

public class PMRASimilarityParser {
	private final double PMRA_MIU = 0.013, PMRA_LAMBDA = 0.022;
	private boolean fromURL, onlyTitleAndAbstract;
	private Annotator annotator;
	private FoafAgent creator;
	private AnnotatorParser annotParser;
	private List<AnnotationE> givenDocAnnotations, analyzedDocAnnotations;
	private Biolink biolink;
	private List<String> allGroupsSTY;
	private Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * Constructor
	 * @param fromURL
	 * @param onlyTitleAndAbstract
	 * @param annotator
	 */
	public PMRASimilarityParser(boolean fromURL, boolean onlyTitleAndAbstract, Annotator annotator) {
		this.fromURL = fromURL;
		this.onlyTitleAndAbstract = onlyTitleAndAbstract;
		this.annotator = annotator;		
	}
	
	/**
	 * Initiates the similarity serializer.
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public void init() throws URISyntaxException, MalformedURLException {		
		if (this.creator == null) {
			this.creator = new FoafAgent();
			this.creator.setId(new URI(ResourceConfig.BIOTEA_RDFIZATOR));
		}
	}
	
	/**
	 * Parses similarity between two documents, it retrieves annotations from a web service.
	 * @param givenDoc
	 * @param analyzedDoc
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws NoResponseException
	 * @throws ArticleParserException 
	 */
	public Biolink parse(String givenDoc, String analyzedDoc) throws URISyntaxException, IOException, NoResponseException, ArticleParserException {
		return this.parse(givenDoc, analyzedDoc, null, null);
		
	} 
	
	/**
	 * weighted_sim_grp(d1,d2,gr) = ( | annot(d1,gr) |  /  | annot(d1) |  +  | annot(d2) | ) * sim_grp(d1,d2,gr)
	 * @param givenDoc
	 * @param analyzedDoc
	 * @param groups
	 * @return
	 * @throws URISyntaxException 
	 * @throws NoResponseException 
	 * @throws IOException 
	 * @throws ArticleParserException 
	 */
	public Biolink parse(String givenDoc, String analyzedDoc, String model, List<String> groups) throws URISyntaxException, IOException, NoResponseException, ArticleParserException {
		this.init();
		if (this.fromURL) {
			if (this.annotator == Annotator.CMA) {
				this.annotParser = new CMAParser(this.fromURL, true, true, this.onlyTitleAndAbstract, true);
			} else if (this.annotator == Annotator.NCBO) {
				//TODO
			}
			this.givenDocAnnotations = annotParser.parse(givenDoc);
			this.analyzedDocAnnotations = annotParser.parse(analyzedDoc);
			this.parseSimilarity(givenDoc, analyzedDoc, model, groups);
			return this.biolink;
		} else {
			throw new IOException("PMRASimilarityParser.parse(String, String) parser cannot be used if it has been configured to parse from files");
		}
	}
	
	/**
	 * Parses similarity between two documents, it retrieves annotations from RDF/XML files.
	 * @param givenDoc
	 * @param analyzedDoc
	 * @return
	 * @throws OntologyLoadException 
	 * @throws ClassNotFoundException 
	 * @throws FileNotFoundException 
	 * @throws URISyntaxException 
	 */
	public Biolink parse(File givenDocFile, File analyzedDocFile, RDFFormat format) throws FileNotFoundException, ClassNotFoundException, OntologyLoadException, URISyntaxException {
		return this.parse(givenDocFile, analyzedDocFile, null, null, format);
	}
	
	/**
	 * Parses similarity between two documents, it retrieves annotations from RDF/XML files.
	 * @param givenDoc
	 * @param analyzedDoc
	 * @return
	 * @throws OntologyLoadException 
	 * @throws ClassNotFoundException 
	 * @throws FileNotFoundException 
	 * @throws URISyntaxException 
	 */
	public Biolink parse(File givenDocFile, File analyzedDocFile) throws FileNotFoundException, ClassNotFoundException, OntologyLoadException, URISyntaxException {
		return this.parse(givenDocFile, analyzedDocFile, null, null, RDFFormat.RDFXML_ABBREV);
	}
	
	/**
	 * Parses similarity between two documents, it retrieves annotations from RDF/XML files.
	 * @param givenDoc
	 * @param analyzedDoc
	 * @return
	 * @throws OntologyLoadException 
	 * @throws ClassNotFoundException 
	 * @throws FileNotFoundException 
	 * @throws URISyntaxException 
	 */
	public Biolink parse(File givenDocFile, File analyzedDocFile, String model, List<String> groups) throws FileNotFoundException, ClassNotFoundException, OntologyLoadException, URISyntaxException {
		return this.parse(givenDocFile, RDFFormat.RDFXML_ABBREV, analyzedDocFile, model, groups);
	}
	
	/**
	 * Parses similarity between two documents, it retrieves annotations from RDF/XML files.
	 * @param givenDoc
	 * @param analyzedDoc
	 * @return
	 * @throws OntologyLoadException 
	 * @throws ClassNotFoundException 
	 * @throws FileNotFoundException 
	 * @throws URISyntaxException 
	 */
	public Biolink parse(File givenDocFile, File analyzedDocFile, String model, List<String> groups, RDFFormat format) throws FileNotFoundException, ClassNotFoundException, OntologyLoadException, URISyntaxException {
		return this.parse(givenDocFile, format, analyzedDocFile, model, groups);
	}
	
	/**
	 * Parses similarity between two documents, it retrieves annotations from RDF/XML files.
	 * @param givenDoc
	 * @param analyzedDoc
	 * @return
	 * @throws OntologyLoadException 
	 * @throws ClassNotFoundException 
	 * @throws FileNotFoundException 
	 * @throws URISyntaxException 
	 */
	public Biolink parse(File givenDocFile, RDFFormat format, File analyzedDocFile, String model, List<String> groups) throws FileNotFoundException, ClassNotFoundException, OntologyLoadException, URISyntaxException {
		this.givenDocAnnotations = AnnotationOWLReader.retrieveFromFile(givenDocFile, format);
		logger.debug(this.givenDocAnnotations);
		this.analyzedDocAnnotations = AnnotationOWLReader.retrieveFromFile(analyzedDocFile, format);
		logger.debug(this.analyzedDocAnnotations);
		String givenDocId;
		int extension = givenDocFile.getName().lastIndexOf('.');
		if (extension != -1) {
			givenDocId = givenDocFile.getName().substring(0, extension);
		} else {
			givenDocId = givenDocFile.getName();
		}
		String analyzedDocId;
		extension = analyzedDocFile.getName().lastIndexOf('.');
		if (extension != -1) {
			analyzedDocId = analyzedDocFile.getName().substring(0, extension);
		} else {
			analyzedDocId = analyzedDocFile.getName();
		}
		this.parseSimilarity(givenDocId, analyzedDocId, model, groups);
		return this.biolink;
	}
	
	/**
	 * Parses PMRA similarity.
	 * weighted_sim_grp(givenDoc d1,analyzedDoc d2,gr) = ( | annot(d1,gr) |  /  | annot(d1) |  +  | annot(d2) | ) * sim_grp(d1,d2,gr) 
	 * @param givenDoc
	 * @param analyzedDoc
	 * @param model
	 * @param groups
	 * @throws URISyntaxException 
	 */
	private void parseSimilarity(String givenDocId, String analyzedDocId, String model, List<String> groups) throws URISyntaxException {
		if ((model != null) && (groups != null)) {
			this.allGroupsSTY = new ArrayList<String>();
			for (String group: groups) {
				this.allGroupsSTY.addAll(BiolinksResourceConfig.getTypes(model, group));
			} 
		} else {
			this.allGroupsSTY = new ArrayList<String>();
		}
		
		int totalTermsGivenDoc, totalTermsAnalyzedDoc;
		int fullTotalTermsGivenDoc = this.totalTermFrequency(this.givenDocAnnotations, null, null)
				, fulltotalTermsAnalyzedDoc = this.totalTermFrequency(this.analyzedDocAnnotations, null, null);
		if ((model != null) && (groups != null)) {
			totalTermsGivenDoc = this.totalTermFrequency(this.givenDocAnnotations, model, groups);
			totalTermsAnalyzedDoc = this.totalTermFrequency(this.analyzedDocAnnotations,model, groups);
		} else {
			totalTermsGivenDoc = fullTotalTermsGivenDoc;
			totalTermsAnalyzedDoc = fulltotalTermsAnalyzedDoc;
		}
		
		this.biolink = new Biolink();
		if ((model != null) && (groups != null)) {
			this.biolink.setModelLabel(BiolinksResourceConfig.getModelName(model));
			this.biolink.setModelURI(new URI(BiolinksResourceConfig.getModelURL(model)));
		}
		this.biolink.setAnnotatorURI(new URI(this.annotator.getServiceURI()));
		this.biolink.setGivenDocURI(new URI(ResourceConfig.getDocRdfUri(null, givenDocId)));
		this.biolink.setAnalyzedDocURI(new URI(ResourceConfig.getDocRdfUri(null, analyzedDocId)));
		
		if (givenDocId.equals(analyzedDocId)) {
			this.parsePartialSimilarity(givenDocId, totalTermsGivenDoc, givenDocId, totalTermsGivenDoc, true, true, model, groups);
			this.biolink.setScore(1.0);
		} else {
			double ratio = this.parsePartialSimilarity(givenDocId, totalTermsGivenDoc, givenDocId, totalTermsGivenDoc, true, false, model, groups);
			logger.debug(ratio);
			double numerator = this.parsePartialSimilarity(givenDocId, totalTermsGivenDoc, analyzedDocId, totalTermsAnalyzedDoc, false, true, model, groups);
			logger.debug(numerator);
			if ((model != null) && (groups != null)) {
				double givenDocWeight = (1.0*totalTermsGivenDoc) / fullTotalTermsGivenDoc;
				double analyzedDocWeigth = (1.0*totalTermsAnalyzedDoc) / fulltotalTermsAnalyzedDoc;
				this.biolink.setScore(ratio == 0 ? 0 : givenDocWeight * analyzedDocWeigth * (numerator/ratio));
			} else {
				this.biolink.setScore(ratio == 0 ? 0 : numerator/ratio);
			}			
		}

		if ((model != null) && (groups != null)) {
			this.biolink.getGroups().addAll(groups);
		}
	}
	
	/**
	 * Parses PMRA similarity between a document to be compared/analyzed against a given document.
	 * PMRA similarity formula, probability of being interested in analyzedDoc given a known interest in givenDoc.
	 * PMRA(analyzedDoc|givenDoc) = SUM(t=0 ... N) [P(givenDoc|term(t)) * P(analyzedDoc|term(t)) * idf(t)]
	 * weighted_sim_grp(d1,d2,gr) = ( | annot(d1,gr) |  /  | annot(d1) |  +  | annot(d2) | ) * sim_grp(d1,d2,gr)
	 * @return
	 * @throws URISyntaxException 
	 */
	private double parsePartialSimilarity(String givenDocId, int totalTermsGivenDoc, String analyzedDocId, int totalTermsAnalyzedDoc, boolean sameDoc, boolean addCommon, String model, List<String> groups) throws URISyntaxException {
		double numerator = 0;
		for (AnnotationE givenAnnot: this.givenDocAnnotations) {
			boolean add = false;
			if ((model == null) || (groups == null) || (this.isInAnyGroup(givenAnnot))) {
				if (sameDoc) {
					numerator += probDocGivenTerm(givenAnnot.getFrequency() * givenAnnot.getIDF(), totalTermsGivenDoc) 
							* probDocGivenTerm(givenAnnot.getFrequency() * givenAnnot.getIDF(), totalTermsGivenDoc) 
							* givenAnnot.getIDF();
					add = true;
				} else {
					AnnotationE analyzedAnnot = this.contains(this.analyzedDocAnnotations, givenAnnot);
					if (analyzedAnnot != null) {//common
						numerator += probDocGivenTerm(givenAnnot.getFrequency() * givenAnnot.getIDF(), totalTermsGivenDoc) 
								* probDocGivenTerm(analyzedAnnot.getFrequency() * givenAnnot.getIDF(), totalTermsAnalyzedDoc) 
								* givenAnnot.getIDF();
						add = true;				
					} /*else {
						numerator += probDocGivenTerm(givenAnnot.getFrequency() * givenAnnot.getIDF(), totalTermsGivenDoc) 
								* probDocGivenTerm(0, totalTermsAnalyzedDoc) * givenAnnot.getIDF();
						add = false;
					}*/
				}
				if (addCommon && add) {
					AnnotatedConcept annotConcept = new AnnotatedConcept();
					annotConcept.setTF(givenAnnot.getFrequency());
					annotConcept.setIDF(givenAnnot.getIDF());
					for (Topic topic: givenAnnot.getTopics()) {
						annotConcept.getTopics().add(topic.getURL());
					}				
					this.biolink.getSharedTerms().add(annotConcept);					
				}
			}
		}
				
		return numerator;
	}
	
	/**
	 * True if any of the types of any of the topics of the given annotation coincides with any type in allGroupsSTY (all of the types for all of the groups).
	 * @param annot
	 * @return
	 */
	private boolean isInAnyGroup(AnnotationE annot) {		
		for (Topic topic: annot.getTopics()) {
			if (!Collections.disjoint(topic.getUmlsType(), this.allGroupsSTY)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * True if lst contains annot, regardless the annotated document but verifying the topics intersection.
	 * @param lst
	 * @param annot
	 * @return
	 */
	private AnnotationE contains(List<AnnotationE> lst, AnnotationE annot) {
		for (AnnotationE elem: lst) {						
			if ((elem.getUri() != null) && (annot.getUri() != null) &&
					elem.getUri().toString().equals(annot.getUri().toString())) {
				return elem;
			}
			
			if (elem.getAuthor() == null) {
				if (annot.getAuthor() != null)
					continue;
			} else if (!elem.getAuthor().getUri().toString().equals(annot.getAuthor().getUri().toString()))
				continue;
			if (elem.getCreator() == null) {
				if (annot.getCreator() != null)
					continue;
			} else if (!elem.getCreator().getUri().toString().equals(annot.getCreator().getUri().toString()))
				continue;
						
			if (!this.intersects(elem.getTopics(), annot.getTopics()))
				continue;
						
			return elem;
		}
		return null;
	}
	
	/**
	 * True if there is an intersection between topics1 and topics2.
	 * @param topics1
	 * @param topics2
	 * @return
	 */
	private boolean intersects(Collection<Topic> topics1, Collection<Topic> topics2) {
		for (Topic t : topics1) {
            if(topics2.contains(t)) {
                return true;
            }
        }
		return false;
	}
	
	/**
	 * Returns the sum of all frequencies in a document if the annotation has any topic with any STY in the groups given the model.
	 * @param lst
	 * @param model
	 * @param groups
	 * @return
	 */
	private int totalTermFrequency (List<AnnotationE> lst, String model, List<String> groups) {
		int total = 0;		
		if ((model != null) && (groups != null)) {
			for (AnnotationE annot: lst) {
				if (this.isInAnyGroup(annot)) {
					total += annot.getFrequency();
				}				
			}
		} else {
			for (AnnotationE annot: lst) {
				total += annot.getFrequency();
			}
		}
		
		return total;
	}
	
	/**
	 * PMRA formula, probability of a document given a term P(doc|term).
	 * P(doc|term) = POW (1 + (POW(MIU/LAMBDA, tf(term)-1) * POW(E, -(MIU-LAMBDA) * totalTerms(doc))), -1)
	 * @param termFrequency
	 * @param totalDocTerms
	 * @return
	 */
	private double probDocGivenTerm(double termFrequency, int totalDocTerms) {
		double prob = Math.pow(
			1 + (
					Math.pow(this.PMRA_MIU/this.PMRA_LAMBDA, termFrequency-1) *
					Math.pow(Math.E, -(this.PMRA_MIU - this.PMRA_LAMBDA) * totalDocTerms)
				)
			, -1);
		return prob;
	}
	
	/**
	 * Serializes annotations to a file.
	 * @param fileName
	 * @param format
	 * @param dao
	 * @throws RDFModelIOException 
	 * @throws URISyntaxException 
	 * @throws OntologyLoadException 
	 * @throws ClassNotFoundException 
	 * @throws FileNotFoundException 
	 */
	public URI serializeToFile(String fullPathName, RDFFormat format, ObjectModelDAO<Biolink> dao, boolean empty) throws RDFModelIOException, FileNotFoundException, ClassNotFoundException, OntologyLoadException, URISyntaxException {
		return dao.insert(ResourceConfig.getBioteaDatasetURL(null, null), BiolinksResourceConfig.BASE_URL_BIOLINK, BiolinksResourceConfig.BASE_URL_ANNOTATED_CONCEPT, this.biolink, fullPathName, format, empty);
	}
	
	/**
	 * Serializes annotations to a model.
	 * @param model
	 * @param format
	 * @param dao
	 * @throws RDFModelIOException 
	 * @throws URISyntaxException 
	 */
	public URI serializeToModel(Model model, ObjectModelDAO<Biolink> dao) throws RDFModelIOException, URISyntaxException {
		return dao.insert(ResourceConfig.getBioteaDatasetURL(null, null), BiolinksResourceConfig.BASE_URL_BIOLINK, BiolinksResourceConfig.BASE_URL_ANNOTATED_CONCEPT, this.biolink, model);
	}

}
