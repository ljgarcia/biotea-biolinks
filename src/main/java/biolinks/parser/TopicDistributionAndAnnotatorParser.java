package biolinks.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.jena.riot.RDFFormat;

import ws.biotea.ld2rdf.annotation.exception.NoResponseException;
import ws.biotea.ld2rdf.exception.RDFModelIOException;
import ws.biotea.ld2rdf.rdf.model.aoextended.AnnotationE;
import ws.biotea.ld2rdf.rdf.persistence.ao.AnnotationDAO;
import biolinks.model.TopicDistribution;
import biolinks.persistence.ObjectModelDAO;

import com.hp.hpl.jena.rdf.model.Model;

import edu.stanford.smi.protege.exception.OntologyLoadException;

public interface TopicDistributionAndAnnotatorParser {
	/**
	 * Parses an annotator parser response from a URL in order to extract its annotations.
	 * @param documentId
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public List<AnnotationE> parse(String documentId) throws IOException, URISyntaxException, NoResponseException;
	
	/**
	 * Parses an annotator parser response from a file in order to extract its annotations.
	 * @param documentId
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public List<AnnotationE> parse(File file) throws IOException, URISyntaxException, NoResponseException;
	
	/**
	 * Serializes annotations and topic distribution to a file.
	 * @param fullPathName
	 * @param format
	 * @param dao
	 * @param daoTopic
	 * @param empty
	 * @param blankNode
	 * @return
	 * @throws RDFModelIOException
	 */
	public List<AnnotationE> serializeToFile(String fullPathName, RDFFormat format, AnnotationDAO dao, ObjectModelDAO<TopicDistribution> daoTopic,boolean empty, boolean blankNode) throws RDFModelIOException, URISyntaxException, FileNotFoundException, ClassNotFoundException, OntologyLoadException ;
	
	/**
	 * Serializes annotations and topic distribution to a model.
	 * @param model
	 * @param dao
	 * @param daoTopic
	 * @param blankNode
	 * @return
	 * @throws RDFModelIOException
	 */
	public List<AnnotationE> serializeToModel(Model model, AnnotationDAO dao, ObjectModelDAO<TopicDistribution> daoTopic,boolean blankNode) throws RDFModelIOException, URISyntaxException;
}
