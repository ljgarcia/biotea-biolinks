package biolinks.ws.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ws.biotea.ld2rdf.annotation.exception.ErrorResource;
import ws.biotea.ld2rdf.annotation.exception.NoResponseException;
import ws.biotea.ld2rdf.annotation.exception.ParameterException;
import ws.biotea.ld2rdf.exception.RDFModelIOException;
import ws.biotea.ld2rdf.rdf.persistence.ao.ConnectionLDModel;
import ws.biotea.ld2rdf.util.annotation.Annotator;
import biolinks.model.Biolink;
import biolinks.model.TopicDistribution;
import biolinks.parser.CMADistributionParser;
import biolinks.parser.PMRASimilarityParser;
import biolinks.parser.TopicDistributionParser;
import biolinks.persistence.BiolinksOWLDAO;
import biolinks.persistence.ObjectModelDAO;
import biolinks.persistence.TopicDistributionOWLDAO;
import biolinks.util.BiolinksResourceConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.hpl.jena.rdf.model.Model;

import edu.stanford.smi.protege.exception.OntologyLoadException;

@RestController
public class SimilarityWSController {
	private static final Logger LOGGER = Logger.getLogger(SimilarityWSController.class);
	private final static String PARAMETER_ERROR = "Please verify your parameters. <baseId> is mandatory. "
			+ "<otherId> is mandatory. [db] is optional, accepted values are 'pubmed' or 'pmc' (default value). "
			+ "[annotator] is optional, accepted values are 'cma', 'ncbo'(default). "
			+ "[format] is optional, accepted values 'xml' or 'json'(default)";
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from Biolinks web services, powered by Spring Boot!";
    }
    
    @RequestMapping(value= "/biolinks/similarity", method = RequestMethod.GET)
    public @ResponseBody void getSimilarity(HttpServletResponse response
    		, @RequestParam(value = "db", required = true, defaultValue = "pmc") String db
    		, @RequestParam(value = "baseId", required = true) String baseId
    		, @RequestParam(value = "otherId", required = true) String otherId
    		, @RequestParam(value = "annotator", required = true, defaultValue = "ncbo") String annotator
    		, @RequestParam(value = "format", required = true, defaultValue = "xml") String format) 
    throws Exception {
    	annotator = annotator.toUpperCase();
    	StringBuffer langFormat = new StringBuffer();
    	Model model = this.processSimilarity(response, annotator, db, format, baseId, otherId, langFormat, null, null);
    	model.write(response.getWriter(), langFormat.toString());
    }
    
    @RequestMapping(value= "/biolinks/groupSimilarity", method = RequestMethod.GET)
    public @ResponseBody void getGroupSimilarity(HttpServletResponse response
    		, @RequestParam(value = "db", required = true, defaultValue = "pmc") String db
    		, @RequestParam(value = "baseId", required = true) String baseId
    		, @RequestParam(value = "otherId", required = true) String otherId
    		, @RequestParam(value = "annotator", required = true, defaultValue = "ncbo") String annotator
    		, @RequestParam(value = "format", required = true, defaultValue = "xml") String format
    		, @RequestParam(value = "model", required = true, defaultValue = "biolinks") String bioModel
    		, @RequestParam(value = "groups", required = true) String groups) 
    throws Exception {
    	annotator = annotator.toUpperCase();
    	StringBuffer langFormat = new StringBuffer();
    	
    	if (!BiolinksResourceConfig.getModels().contains(bioModel)) {
			throw new ParameterException(PARAMETER_ERROR);
		}
    	
    	List<String> groupsArray = Arrays.asList(groups.split(","));
    	if(!BiolinksResourceConfig.getGroups(bioModel).containsAll(groupsArray)) {
    		throw new ParameterException(PARAMETER_ERROR);
    	}
    	
    	Model model = this.processSimilarity(response, annotator, db, format, baseId, otherId, langFormat, bioModel, groupsArray);
    	model.write(response.getWriter(), langFormat.toString());
    }
    
    @RequestMapping(value= "/biolinks/topicDistribution", method = RequestMethod.GET)
    public @ResponseBody void getTopicDistribution(HttpServletResponse response
    		, @RequestParam(value = "db", required = true, defaultValue = "pmc") String db
    		, @RequestParam(value = "baseId", required = true) String baseId
    		, @RequestParam(value = "annotator", required = true, defaultValue = "ncbo") String annotator
    		, @RequestParam(value = "format", required = true, defaultValue = "xml") String format
    		, @RequestParam(value = "model", required = true, defaultValue = "biolinks") String bioModel
	) throws Exception {
    	annotator = annotator.toUpperCase();
    	StringBuffer langFormat = new StringBuffer();
    	
    	if (!BiolinksResourceConfig.getModels().contains(bioModel)) {
			throw new ParameterException(PARAMETER_ERROR);
		}
    	
    	RDFFormat rdfFormat = this.verifyParameters(response, annotator, db, format, langFormat);
    	ConnectionLDModel conn = new ConnectionLDModel();
    	Model model = conn.openJenaModel();
    	
    	boolean cached = false;
		try {   		
			String fileName = "";
			fileName = BiolinksResourceConfig.getCachingDistributionFileName(baseId, Annotator.valueOf(annotator)
				, bioModel, rdfFormat);
    		if (BiolinksResourceConfig.getDistributionCaching(Annotator.valueOf(annotator))) {
    			File file = new File (fileName);    		
    			if (file.exists()) {  
    				FileInputStream fis = new FileInputStream(file);
    				StreamUtils.copy(fis, response.getOutputStream());
    				cached = true;    				
    			}    			
			}
		} catch (IOException e) {
			e.printStackTrace();
			cached = false;
		}
		
		try {
			if (!cached) {  		
				ObjectModelDAO<TopicDistribution> dao = new TopicDistributionOWLDAO();			
	    		
	    		//Verify annotator
	    		if (Annotator.valueOf(annotator) == Annotator.CMA) {
	    			TopicDistributionParser parser;
	    			if (db.equals("pubmed")) {
	    				parser = new CMADistributionParser(true, true, true, true, bioModel);
	    			} else {
	    				parser = new CMADistributionParser(true, true, true, false, bioModel);
	    			}
					parser.parse(baseId);
					parser.serializeToModel(model, dao, false);
	    		} else if (Annotator.valueOf(annotator) == Annotator.NCBO) {
	    			
	    		}   
	    		
	    		String extension = rdfFormat == RDFFormat.JSONLD ? ".json" : ".rdf";
	    		String fileName = BiolinksResourceConfig.getDistributionSavingPath(Annotator.valueOf(annotator)) + baseId + "_distribution_" + bioModel + extension;
	    		boolean replace = BiolinksResourceConfig.getDistributionSavingReplace(Annotator.valueOf(annotator));
	    		this.saveToFile(fileName, replace, BiolinksResourceConfig.getDistributionSaving(Annotator.valueOf(annotator)), Annotator.valueOf(annotator), db, baseId, null, bioModel, null, rdfFormat, conn);
	    		model.write(response.getWriter(), langFormat.toString());
			}
    	} catch (RDFModelIOException | URISyntaxException | IOException e) {
    		LOGGER.error(e);
    		throw e;
		} catch (IllegalArgumentException e) {
			throw new ParameterException(PARAMETER_ERROR);
		} catch (Exception e) {
			throw e;
		}
    }
    
    private RDFFormat verifyParameters(HttpServletResponse response, String annotator, String db, String format, StringBuffer langFormat) throws ParameterException {
    	//Verify parameters
		if ( !(db.equals("pubmed") || db.equals("pmc"))) {
			throw new ParameterException(PARAMETER_ERROR);
		}
		if ( !(annotator.equals("CMA") || annotator.equals("NCBO"))) {
			throw new ParameterException(PARAMETER_ERROR);
		}
		
		RDFFormat rdfFormat;
		if (format.equals("json")) {
			response.setContentType("application/json;charset=UTF-8");
			rdfFormat = RDFFormat.JSONLD;
			langFormat.append("JSON-LD");
		} else {
			response.setContentType("text/xml;charset=UTF-8");
			rdfFormat = RDFFormat.RDFXML_ABBREV;
			langFormat.append("RDF/XML-ABBREV");
		}
		return rdfFormat;
    }

    private Model processSimilarity(HttpServletResponse response, String annotator, String db, String format, String baseId, String otherId, StringBuffer langFormat, String bioModel, List<String> groups) throws Exception {
    	RDFFormat rdfFormat = this.verifyParameters(response, annotator, db, format, langFormat);
		boolean cached = false;
		try {   		
			String fileName = "";
			if ((bioModel != null) && (groups != null)) {
				fileName = BiolinksResourceConfig.getCachingFileName(baseId, otherId, Annotator.valueOf(annotator)
					, bioModel, groups, rdfFormat);
			} else {
				fileName = BiolinksResourceConfig.getCachingFileName(baseId, otherId, Annotator.valueOf(annotator)
					, null, null, rdfFormat);
			}
    		if (BiolinksResourceConfig.getSimilarityCaching(Annotator.valueOf(annotator))) {
    			File file = new File (fileName);    		
    			if (file.exists()) {  
    				FileInputStream fis = new FileInputStream(file);
    				StreamUtils.copy(fis, response.getOutputStream());
    				cached = true;    				
    			}    			
			}
		} catch (IOException e) {
			e.printStackTrace();
			cached = false;
		}
		
		try {
			if (!cached) {
	    		ConnectionLDModel conn = new ConnectionLDModel();
	    		Model model = conn.openJenaModel();
	    		ObjectModelDAO<Biolink> dao = new BiolinksOWLDAO();			
	    		
	    		//Verify annotator
	    		if (Annotator.valueOf(annotator) == Annotator.CMA) {
	    			PMRASimilarityParser parser;
	    			if (db.equals("pubmed")) {
	    				parser = new PMRASimilarityParser(true, true, Annotator.CMA);
	    			} else {
	    				parser = new PMRASimilarityParser(true, false, Annotator.CMA);
	    			}
					parser.parse(baseId, otherId, bioModel, groups);
					parser.serializeToModel(model, dao);
	    		} else if (Annotator.valueOf(annotator) == Annotator.NCBO) {
	    			
	    		}   
	    		
	    		String outName = BiolinksResourceConfig.getSavingFileName(baseId, otherId, bioModel
	    				, groups, Annotator.valueOf(annotator), rdfFormat);
	    		boolean replace = BiolinksResourceConfig.getSimilaritySavingReplace(Annotator.valueOf(annotator));
	    		this.saveToFile(outName, replace, BiolinksResourceConfig.getSimilaritySaving(Annotator.valueOf(annotator)), Annotator.valueOf(annotator), db, baseId, otherId, bioModel, groups, rdfFormat, conn);
			
    			return model; 
			}
    	} catch (ClassNotFoundException | OntologyLoadException | RDFModelIOException | URISyntaxException | IOException e) {
    		LOGGER.error(e);
    		throw e;
		} catch (IllegalArgumentException e) {
			throw new ParameterException(PARAMETER_ERROR);
		} catch (Exception e) {
			throw e;
		}
		return null;
    }
    
    @ExceptionHandler(ParameterException.class)
	public void handleParameterError(Exception exception, HttpServletResponse response) throws IOException {
    	LOGGER.error(exception);
    	
		ErrorResource error = new ErrorResource(HttpStatus.BAD_REQUEST, exception.getMessage(), 
				"/biolinks/similarity", exception.getClass().getName());

		response.setContentType("application/json;charset=UTF-8");
		ObjectMapper mapper = new ObjectMapper();		
		mapper.writeValue(response.getWriter(), error);		
	}
    
    @ExceptionHandler(NoResponseException.class)
	public void handleNoResponseError(Exception exception, HttpServletResponse response) throws IOException {
    	LOGGER.error(exception);
    	
		ErrorResource error = new ErrorResource(HttpStatus.NO_CONTENT, exception.getMessage(), 
				"/biolinks/similarity", exception.getClass().getName());

		response.setContentType("application/json;charset=UTF-8");
		ObjectMapper mapper = new ObjectMapper();		
		mapper.writeValue(response.getWriter(), error);		
	}
    
    /**
     * Save to file, if any error just log and go ahead with the service response.
     * @param annotator
     * @param db
     * @param id
     * @param extension
     * @param format
     * @param conn
     */
    private void saveToFile(String outName, boolean replace, boolean save, Annotator annotator, String db, String baseId, String otherId, String model, List<String> groups, RDFFormat format, ConnectionLDModel conn) {    	
    	if (save) {    		
    		File file = new File(outName);
    		if ((file.exists() && replace) 
				|| (!file.exists()) ){
    			try {
					conn.closeAndWriteJenaModel(outName, format);
				} catch (Exception e) {
					e.printStackTrace();
					LOGGER.warn(annotator.getName() + " annotations/distribution could not be saved for " + db + ":" + baseId + ", " + db + ":" + otherId + " articles, Error: " + e);
				}
    		}
    	}
    }
}