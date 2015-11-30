package biolinks.batch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

import biolinks.model.Biolink;
import biolinks.model.TopicDistribution;
import biolinks.parser.AllDistributionAndAnnotatorParser;
import biolinks.parser.CMADistributionParser;
import biolinks.parser.PMRASimilarityParser;
import biolinks.parser.TopicDistributionParser;
import biolinks.persistence.BiolinksOWLDAO;
import biolinks.persistence.ObjectModelDAO;
import biolinks.persistence.TopicDistributionOWLDAO;

import com.hp.hpl.jena.rdf.model.Model;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import ws.biotea.ld2rdf.annotation.exception.NoResponseException;
import ws.biotea.ld2rdf.annotation.exception.ParserInstantiationException;
import ws.biotea.ld2rdf.exception.RDFModelIOException;
import ws.biotea.ld2rdf.rdf.persistence.ao.AnnotationDAO;
import ws.biotea.ld2rdf.rdf.persistence.ao.AnnotationOWLDAO;
import ws.biotea.ld2rdf.rdf.persistence.ao.ConnectionLDModel;
import ws.biotea.ld2rdf.util.annotation.Annotator;

public class SimilarityBatchController {
	private static final Logger LOGGER = Logger.getLogger(SimilarityBatchController.class);
	private static final String JSON_EXTENSION = ".json";
	private static final String RDF_EXTENSION = ".rdf";
	
	public void comparesFromFile(File baseInFile, File otherInFile, String outputDir, RDFFormat format, Annotator annotator
			, boolean onlyTA, String biomodel, List<String> groups) {
		String basePath = baseInFile.toString();
		String otherPath = otherInFile.toString();
		try {
			String outExtension = format == RDFFormat.JSONLD ? JSON_EXTENSION : RDF_EXTENSION;
			String baseInName = baseInFile.getName();
			int dotPos = baseInName.lastIndexOf('.');			
			if (dotPos != -1) {
				baseInName = baseInName.substring(0,dotPos);
			}
			String otherInName = otherInFile.getName();
			dotPos = otherInName.lastIndexOf('.');
			if (dotPos != -1) {
				otherInName = otherInName.substring(0,dotPos);
			}			
			String outName = outputDir + "/" + baseInName + "_" +  otherInName + outExtension; 
			basePath = baseInFile.getCanonicalPath(); 
			otherPath = otherInFile.getCanonicalPath();
			
			ConnectionLDModel conn = new ConnectionLDModel();
    		Model model = conn.openJenaModel(outName, true, format);
    		ObjectModelDAO<Biolink> dao = new BiolinksOWLDAO();
    		PMRASimilarityParser parser;    		
    					
    		if (annotator == Annotator.CMA) {
    			if (onlyTA) {
    				parser = new PMRASimilarityParser(false, true, Annotator.CMA);
    			} else {
    				parser = new PMRASimilarityParser(false, false, Annotator.CMA);
    			}
    			if ((biomodel != null) && (groups != null)) {
    				parser.parse(baseInFile, otherInFile, biomodel, groups);
    			} else {
    				parser.parse(baseInFile, otherInFile);
    			}				
				parser.serializeToModel(model, dao);
    		} else if (annotator == Annotator.NCBO) {
    			//TODO
    		}
			
			conn.closeAndWriteJenaModel(format);
		} catch (ClassNotFoundException | OntologyLoadException | URISyntaxException | RDFModelIOException | IllegalArgumentException e) {
			LOGGER.error("There was an error processing one of " + basePath + " or " + otherPath + ". Error was: " + e);
		} catch (IOException e) {
			LOGGER.error("There was an error processing a file. Error was: " + e);
		}
	}
	
	public void comparesFromURL(String baseId, String otherId, String outputDir, RDFFormat format, Annotator annotator
			, boolean onlyTA, String biomodel, List<String> groups) {
		try {
			String extension = format == RDFFormat.JSONLD ? JSON_EXTENSION : RDF_EXTENSION;
			String outName = outputDir + "/" + baseId + "_" + otherId + extension; 
			ConnectionLDModel conn = new ConnectionLDModel();
    		Model model = conn.openJenaModel(outName, true, format);
    		ObjectModelDAO<Biolink> dao = new BiolinksOWLDAO();	
    		PMRASimilarityParser parser;    		
    		
			if (annotator == Annotator.CMA) {			
				if (onlyTA) {
					parser = new PMRASimilarityParser(true, true, Annotator.CMA);
				} else {
					parser = new PMRASimilarityParser(true, false, Annotator.CMA);
				} 		
				if ((biomodel != null) && (groups != null)) {
    				parser.parse(baseId, otherId, biomodel, groups);
    			} else {
    				parser.parse(baseId, otherId);
    			}
				parser.serializeToModel(model, dao);
			} else if (annotator == Annotator.NCBO) {
				
			}
			
			conn.closeAndWriteJenaModel(format);
		} catch (ClassNotFoundException | OntologyLoadException | URISyntaxException | RDFModelIOException e) {
			LOGGER.error("There was an error processing one of " + baseId + " or " + otherId + ". Error was: " + e);
		} catch (IOException e) {
			LOGGER.error("There was an error processing a file. Error was: " + e);
		} catch (NoResponseException e) {
			LOGGER.error("There was no response for one of " + baseId + " or " + otherId + ". Error was: " + e);
		}
	}
	
	public void distributesFromFile(File inFile, String outputDir, RDFFormat format, Annotator annotator, boolean onlyTA, String bioModel) {
		String inPath = inFile.toString();
		try {
			String outExtension = format == RDFFormat.JSONLD ? JSON_EXTENSION : RDF_EXTENSION;
			String baseInName = inFile.getName();
			int dotPos = baseInName.lastIndexOf('.');			
			if (dotPos != -1) {
				baseInName = baseInName.substring(0, dotPos);
			}			
			String outName = outputDir + "/" + baseInName + "_distribution" + outExtension; 
			inPath = inFile.getCanonicalPath(); 
			
			ConnectionLDModel conn = new ConnectionLDModel();
    		Model model = conn.openJenaModel(outName, true, format);
    		ObjectModelDAO<TopicDistribution> dao = new TopicDistributionOWLDAO();	
    		TopicDistributionParser parser;
    		
    		if (annotator == Annotator.CMA) {
    			if (onlyTA) {
    				parser = new CMADistributionParser(false, true, true, true, bioModel);
    			} else {
    				parser = new CMADistributionParser(false, true, true, false, bioModel);
    			}
				parser.parse(inFile, format);
				parser.serializeToModel(model, dao, false);
    		} else if (annotator == Annotator.NCBO) {
    			
    		}
			
			conn.closeAndWriteJenaModel(format);
		} catch (FileNotFoundException | ClassNotFoundException
				| OntologyLoadException e) {
			LOGGER.error("There was an error processing " + inPath + ". Error was: " + e);
		} catch (IOException | URISyntaxException | NoResponseException e) {
			e.printStackTrace();
			LOGGER.error("There was an error parsing " + inPath + ". Error was: " + e);
		} catch (RDFModelIOException e) {
			LOGGER.error("There was an error serializing " + inPath + ". Error was: " + e);
		}
	}
	
	public void distributesFromURL(String id, String outputDir, RDFFormat format, Annotator annotator, boolean onlyTA, String bioModel) {
		try {
			String extension = format == RDFFormat.JSONLD ? JSON_EXTENSION : RDF_EXTENSION;
			String outName = outputDir + "/" + id + "_distribution" + extension; 
			ConnectionLDModel conn = new ConnectionLDModel();
    		Model model = conn.openJenaModel(outName, true, format);
    		ObjectModelDAO<TopicDistribution> dao = new TopicDistributionOWLDAO();	
    		TopicDistributionParser parser;
    		
    		if (annotator == Annotator.CMA) {
    			if (onlyTA) {
    				parser = new CMADistributionParser(true, true, true, true, bioModel);
    			} else {
    				parser = new CMADistributionParser(true, true, true, false, bioModel);
    			}
				parser.parse(id);
				parser.serializeToModel(model, dao, false);
    		} else if (annotator == Annotator.NCBO) {
    			
    		}
			
			conn.closeAndWriteJenaModel(format);
		} catch (FileNotFoundException | ClassNotFoundException
				| OntologyLoadException e) {
			LOGGER.error("There was an error processing " + id + ". Error was: " + e);
		} catch (IOException | URISyntaxException | NoResponseException e) {
			LOGGER.error("There was an error parsing " + id + ". Error was: " + e);
		} catch (RDFModelIOException e) {
			LOGGER.error("There was an error serializing " + id + ". Error was: " + e);
		}
	}

	public void distributesAndAnnotatesFromFile(File inFile, String outputDir, RDFFormat format, Annotator annotator, boolean onlyTA, String bioModel) {
		String inPath = inFile.toString();
		try {
			String outExtension = format == RDFFormat.JSONLD ? JSON_EXTENSION : RDF_EXTENSION;
			String baseInName = inFile.getName();
			int dotPos = baseInName.lastIndexOf('.');			
			if (dotPos != -1) {
				baseInName = baseInName.substring(0, dotPos);
			}			
			String outName = outputDir + "/" + baseInName + "_annotation_and_distribution" + outExtension; 
			inPath = inFile.getCanonicalPath(); 
			
			ConnectionLDModel conn = new ConnectionLDModel();
    		Model model = conn.openJenaModel(outName, true, format);
    		ObjectModelDAO<TopicDistribution> dao = new TopicDistributionOWLDAO();
    		AnnotationDAO daoAnnot = new AnnotationOWLDAO();
    		AllDistributionAndAnnotatorParser parser;
    		
    		Object[] modelArgs = {bioModel};
    		if (annotator == Annotator.CMA) {
    			if (onlyTA) {
    				Object[] parserArgs = {false, true, true, true, true};    				
    				parser = new AllDistributionAndAnnotatorParser("CMA", bioModel, parserArgs, modelArgs);
    			} else {
    				Object[] parserArgs = {false, true, true, false, true};
    				parser = new AllDistributionAndAnnotatorParser("CMA", bioModel, parserArgs, modelArgs);
    			}
				parser.parse(inFile);
				parser.serializeToModel(model, daoAnnot, dao, false);
    		} else if (annotator == Annotator.NCBO) {
    			
    		}
			
			conn.closeAndWriteJenaModel(format);
		} catch (FileNotFoundException | ClassNotFoundException | OntologyLoadException e) {
			LOGGER.error("There was an error processing " + inPath + ". Error was: " + e);
		} catch (IOException | URISyntaxException | NoResponseException e) {
			LOGGER.error("There was an error parsing " + inPath + ". Error was: " + e);
		} catch (RDFModelIOException e) {
			LOGGER.error("There was an error serializing " + inPath + ". Error was: " + e);
		} catch (ParserInstantiationException e) {
			LOGGER.error("There was an error instantiating the distribution or annotator parser for document " + inPath + ". Error was: " + e);
		}
	}
	
	public void distributesAndAnnotatesFromURL(String id, String outputDir, RDFFormat format, Annotator annotator, boolean onlyTA, String bioModel) {
		try {
			String extension = format == RDFFormat.JSONLD ? JSON_EXTENSION : RDF_EXTENSION;
			String outName = outputDir + "/" + id + "_annotation_and_distribution" + extension;
			
			ConnectionLDModel conn = new ConnectionLDModel();
    		Model model = conn.openJenaModel(outName, true, format);
    		ObjectModelDAO<TopicDistribution> dao = new TopicDistributionOWLDAO();
    		AnnotationDAO daoAnnot = new AnnotationOWLDAO();
    		AllDistributionAndAnnotatorParser parser;
    		
    		Object[] modelArgs = {bioModel};
    		if (annotator == Annotator.CMA) {
    			if (onlyTA) {
    				Object[] parserArgs = {true, true, true, true, true};    				
    				parser = new AllDistributionAndAnnotatorParser("CMA", bioModel, parserArgs, modelArgs);
    			} else {
    				Object[] parserArgs = {true, true, true, false, true};
    				parser = new AllDistributionAndAnnotatorParser("CMA", bioModel, parserArgs, modelArgs);
    			}
				parser.parse(id);
				parser.serializeToModel(model, daoAnnot, dao, false);
    		} else if (annotator == Annotator.NCBO) {
    			
    		}
			
			conn.closeAndWriteJenaModel(format);
		} catch (FileNotFoundException | ClassNotFoundException | OntologyLoadException e) {
			LOGGER.error("There was an error processing " + id + ". Error was: " + e);
		} catch (IOException | URISyntaxException | NoResponseException e) {
			e.printStackTrace();
			LOGGER.error("There was an error parsing " + id + ". Error was: " + e);
		} catch (RDFModelIOException e) {
			LOGGER.error("There was an error serializing " + id + ". Error was: " + e);
		} catch (ParserInstantiationException e) {
			LOGGER.error("There was an error instantiating the distribution or annotator parser for document " + id + ". Error was: " + e);
		}
	}
}
