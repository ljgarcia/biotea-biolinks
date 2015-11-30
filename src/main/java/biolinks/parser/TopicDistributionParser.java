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
import biolinks.model.TopicDistribution;
import biolinks.persistence.ObjectModelDAO;

import com.hp.hpl.jena.rdf.model.Model;

import edu.stanford.smi.protege.exception.OntologyLoadException;

public interface TopicDistributionParser {
	/**
	 * Parses a TopicDistribution object response from a URL in order to extract its annotations.
	 * @param documentId
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public TopicDistribution parse(String documentId) throws IOException, URISyntaxException, NoResponseException;
	
	/**
	 * Parses a TopicDistribution object response from a file in order to extract its annotations.
	 * @param documentId
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws OntologyLoadException 
	 * @throws ClassNotFoundException 
	 */
	public TopicDistribution parse(File file, RDFFormat format) throws IOException, URISyntaxException, NoResponseException, ClassNotFoundException, OntologyLoadException;

	/**
	 * Parses a TopicDistribution object response from a file in order to extract its annotations.
	 * @param documentId
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws OntologyLoadException 
	 * @throws ClassNotFoundException 
	 */
	public TopicDistribution parse(File file) throws IOException, URISyntaxException, NoResponseException, ClassNotFoundException, OntologyLoadException;

	/**
	 * Parses a TopicDistribution object response from a file in order to extract its annotations.
	 * @param documentId
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public TopicDistribution parse(List<AnnotationE> lst) throws IOException, URISyntaxException, NoResponseException;
	
	/**
	 * Serializes T objects to a file.
	 * @param fullPathName
	 * @param format
	 * @param dao
	 * @param empty
	 * @param blankNode
	 * @return
	 * @throws RDFModelIOException
	 */
	public TopicDistribution serializeToFile(String fullPathName, RDFFormat format, ObjectModelDAO<TopicDistribution> dao, boolean empty, boolean blankNode) throws RDFModelIOException, FileNotFoundException, ClassNotFoundException, OntologyLoadException, URISyntaxException;
	
	/**
	 * Serializes T objects to a model.
	 * @param model
	 * @param dao
	 * @param blankNode
	 * @return
	 * @throws RDFModelIOException
	 */
	public TopicDistribution serializeToModel(Model model, ObjectModelDAO<TopicDistribution> dao, boolean blankNode) throws RDFModelIOException, URISyntaxException;
}
