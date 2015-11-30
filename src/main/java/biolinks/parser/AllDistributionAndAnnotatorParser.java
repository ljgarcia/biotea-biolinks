package biolinks.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

import ws.biotea.ld2rdf.annotation.exception.NoResponseException;
import ws.biotea.ld2rdf.annotation.exception.ParserInstantiationException;
import ws.biotea.ld2rdf.annotation.parser.AnnotatorParser;
import ws.biotea.ld2rdf.exception.RDFModelIOException;
import ws.biotea.ld2rdf.rdf.model.aoextended.AnnotationE;
import ws.biotea.ld2rdf.rdf.persistence.ao.AnnotationDAO;
import ws.biotea.ld2rdf.rdf.persistence.ao.AnnotationOWLDAO;
import ws.biotea.ld2rdf.rdf.persistence.ao.ConnectionLDModel;
import ws.biotea.ld2rdf.util.annotation.Annotator;
import biolinks.model.TopicDistribution;
import biolinks.persistence.ObjectModelDAO;
import biolinks.persistence.TopicDistributionOWLDAO;

import com.hp.hpl.jena.rdf.model.Model;

import edu.stanford.smi.protege.exception.OntologyLoadException;

public class AllDistributionAndAnnotatorParser implements TopicDistributionAndAnnotatorParser {
	private Logger logger = Logger.getLogger(this.getClass());
	private AnnotatorParser parser;
	private TopicDistributionParser distParser;
	private List<AnnotationE> lstAnnotations;
	/*
	 boolean fromURL, boolean onlyUMLS, boolean titleTwice, boolean onlyTitleAndAbstract, boolean withSTY
	 */
	/**
	 * Constructor, creates the annotator parser according to the given annotator, args should contain the
	 * arguments requires by the given annotator.
	 * @param annotator
	 * @param model
	 * @param args
	 * @throws ParserInstantiationException
	 */
	public AllDistributionAndAnnotatorParser(String annotator, String model, Object[] parserArgs, Object[] modelArgs) throws ParserInstantiationException {
		annotator = annotator.toUpperCase();
		Annotator annot = Annotator.valueOf(annotator);
		model = model.toUpperCase();
		biolinks.util.Model mod = biolinks.util.Model.valueOf(model);
		if (annot != null) {
			Class<?>[] parserTypes = new Class[parserArgs.length];
			for ( int i = 0; i < parserTypes.length; i++ ) {
				parserTypes[i] = parserArgs[i].getClass();
			}
			Class<?>[] modelTypes = new Class[modelArgs.length];
			for ( int i = 0; i < modelTypes.length; i++ ) {
				modelTypes[i] = modelArgs[i].getClass();
			}
			try {
				parser = (AnnotatorParser) Class.forName(annot.getClassFullName()).getConstructor(parserTypes).newInstance(parserArgs);
				distParser = (TopicDistributionParser) Class.forName(mod.getClassFullName(annot)).getConstructor(modelTypes).newInstance(modelArgs);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException
					| ClassNotFoundException e) {
				logger.error("Either annotator or model parser could not be instantiated. Error: " + e);
				throw new ParserInstantiationException(e);
			}
		}
	}
	/**
	 * Parses an annotator parser response in order to extract its annotations.
	 * @param documentId
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public List<AnnotationE> parse(String documentId) throws IOException, URISyntaxException, NoResponseException {
		this.lstAnnotations = parser.parse(documentId);
		distParser.parse(this.lstAnnotations); 
		return this.lstAnnotations;
	}
	
	/**
	 * Parses an annotator parser response in order to extract its annotations.
	 * @param documentId
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	public List<AnnotationE> parse(File file) throws IOException, NoResponseException, URISyntaxException {
		this.lstAnnotations = parser.parse(file);
		distParser.parse(this.lstAnnotations);
		return this.lstAnnotations;
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
	public List<AnnotationE> serializeToFile(String fullPathName, RDFFormat format, AnnotationDAO dao, ObjectModelDAO<TopicDistribution> daoTopic, boolean empty, boolean blankNode) throws RDFModelIOException, URISyntaxException, FileNotFoundException, ClassNotFoundException, OntologyLoadException {
		List<AnnotationE> lst = parser.serializeToFile(fullPathName, format, dao, empty, blankNode);
		distParser.serializeToFile(fullPathName, format, daoTopic, false, blankNode);
		return lst;
	}
	
	/**
	 * Serializes annotations to a model.
	 * @param model
	 * @param format
	 * @param dao
	 * @throws RDFModelIOException 
	 * @throws URISyntaxException 
	 */
	public List<AnnotationE> serializeToModel(Model model, AnnotationDAO dao, ObjectModelDAO<TopicDistribution> daoTopic, boolean blankNode) throws RDFModelIOException, URISyntaxException {
		List<AnnotationE> lst = parser.serializeToModel(model, dao, blankNode);
		distParser.serializeToModel(model, daoTopic, blankNode);
		return lst;
	}

	public static void main(String[] args) throws IOException, URISyntaxException, NoResponseException, ClassNotFoundException, OntologyLoadException, RDFModelIOException, ParserInstantiationException {
		Object[] parserArgs = {false, true, true, true, true};
		Object[] modelArgs = {"biolinks"};
		AllDistributionAndAnnotatorParser parser = new AllDistributionAndAnnotatorParser("CMA", "biolinks", parserArgs, modelArgs);		
		parser.parse(new File("C:/Users/Leyla/Desktop/PMID11707154.txt"));
		AnnotationDAO daoAnnot = new AnnotationOWLDAO();
		ObjectModelDAO<TopicDistribution> dao = new TopicDistributionOWLDAO();
		ConnectionLDModel conn = new ConnectionLDModel();
    	Model model = conn.openJenaModel();
		parser.serializeToModel(model, daoAnnot, dao, false);
		try {
			conn.closeAndWriteJenaModel("C:/Users/Leyla/Desktop/PMID11707154_annotAndTopics.rdf", RDFFormat.RDFXML_ABBREV);
		} catch (Exception e) {
			e.printStackTrace();				
		}
	}
}
