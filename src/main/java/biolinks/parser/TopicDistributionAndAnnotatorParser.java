package biolinks.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.jena.riot.RDFFormat;

import ws.biotea.ld2rdf.annotation.exception.ArticleParserException;
import ws.biotea.ld2rdf.annotation.exception.NoResponseException;
import ws.biotea.ld2rdf.annotation.exception.UnsupportedFormatException;
import ws.biotea.ld2rdf.exception.RDFModelIOException;
import ws.biotea.ld2rdf.rdf.model.aoextended.AnnotationE;
import ws.biotea.ld2rdf.rdf.persistence.ConstantConfig;
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
	 * @throws ArticleParserException 
	 */
	public List<AnnotationE> parse(String documentId) throws IOException, URISyntaxException, NoResponseException, ArticleParserException;
	
	/**
	 * Parses an annotator parser response from a file in order to extract its annotations.
	 * @param documentId
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws ArticleParserException 
	 */
	public List<AnnotationE> parse(File file) throws IOException, URISyntaxException, NoResponseException, ArticleParserException;
	
	/**
	 * Serializes annotations and topic distribution to a file.
	 * @param fullPathName
	 * @param format
	 * @param onto
	 * @param daoTopic
	 * @param empty
	 * @param blankNode
	 * @return
	 * @throws RDFModelIOException
	 * @throws UnsupportedFormatException 
	 */
	public List<AnnotationE> serializeToFile(String fullPathName, RDFFormat format, ConstantConfig onto, ObjectModelDAO<TopicDistribution> daoTopic,boolean empty, boolean blankNode) throws RDFModelIOException, URISyntaxException, FileNotFoundException, ClassNotFoundException, OntologyLoadException, UnsupportedFormatException ;
	
	/**
	 * Serializes annotations and topic distribution to a model.
	 * @param model
	 * @param onto
	 * @param daoTopic
	 * @param blankNode
	 * @return
	 * @throws RDFModelIOException
	 * @throws UnsupportedFormatException 
	 */
	public List<AnnotationE> serializeToModel(Model model, ConstantConfig onto, ObjectModelDAO<TopicDistribution> daoTopic,boolean blankNode) throws RDFModelIOException, URISyntaxException, UnsupportedFormatException;
}
