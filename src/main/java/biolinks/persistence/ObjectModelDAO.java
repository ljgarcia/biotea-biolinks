package biolinks.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

import ws.biotea.ld2rdf.exception.RDFModelIOException;
import ws.biotea.ld2rdf.rdf.persistence.ConnectionLDModel;
import biolinks.model.Concept;

import com.hp.hpl.jena.rdf.model.Model;

import edu.stanford.smi.protege.exception.OntologyLoadException;

public abstract class ObjectModelDAO <T extends Concept> {
	private Logger logger = Logger.getLogger(this.getClass());
	/**
	 * Inserts an object into a model, returns the generated URI.
	 * @param datasetURL
	 * @param baseURL
	 * @param distribution
	 * @param modelOut
	 * @return
	 */
	public URI insert(String datasetURL, String baseURL, String annotConBaseURL, T object, Model modelOut) throws URISyntaxException {
		createAnnotationInModel(modelOut, datasetURL, baseURL, annotConBaseURL, object);
		return object.getURI();
	}
	/**
	 * Inserts an object into a model and writes the model down, returns the generated URI.
	 * @param datasetURL
	 * @param baseURL
	 * @param distribution
	 * @param fileOut
	 * @param format
	 * @param empty
	 * @return
	 */
	public URI insert(String datasetURL, String baseURL, String annotConBaseURL, T distribution, String fileOut
			, RDFFormat format, boolean empty) throws FileNotFoundException, ClassNotFoundException, OntologyLoadException, URISyntaxException {
		ConnectionLDModel conn = new ConnectionLDModel();
		Model model = conn.openJenaModel(fileOut, empty);		
		this.insert(datasetURL, baseURL, annotConBaseURL, distribution, model);
		conn.closeAndWriteJenaModel(format);
		return distribution.getURI();
	}
	/**
	 * Inserts multiple objects into a model, returns a list with the distributions inserted.
	 * @param datasetURL
	 * @param baseURL
	 * @param distributions
	 * @param modelOut
	 * @return
	 */
	public List<T> insert(String datasetURL, String baseURL, String annotConBaseURL, List<T> distributions, Model modelOut) {
		List<T> inserted = new ArrayList<T>();
		for (T dist: distributions) {
			try {
				createAnnotationInModel(modelOut, datasetURL, baseURL, annotConBaseURL, dist);				
				inserted.add(dist);
			} catch (Exception e) {
				//e.printStackTrace();
				logger.error("- ERROR - Topic distribution not inserted: There has been an error inserting annotation " + dist + ", error: " + e.getLocalizedMessage());
			}
		}
		return inserted;
	}
	/**
	 * Inserts multiple objects into a model and write the model down, returns a list with the distributions inserted.
	 * @param datasetURL
	 * @param baseURL
	 * @param distributions
	 * @param fileOut
	 * @param format
	 * @param empty
	 * @return
	 */
	public List<T> insert(String datasetURL, String baseURL, String annotConBaseURL, List<T> distributions, String fileOut
			, RDFFormat format, boolean empty) throws RDFModelIOException {
		List<T> inserted = new ArrayList<T>();
		try {			
			ConnectionLDModel conn = new ConnectionLDModel();
			Model model = conn.openJenaModel(fileOut, empty);	
			inserted = this.insert(datasetURL, baseURL, annotConBaseURL, distributions, model);
			conn.closeAndWriteJenaModel(format);
		} catch (Exception e) {
			logger.fatal("- FATAL - Topic distribution model " + fileOut + " was not closed/saved: " + e.getMessage());
			throw new RDFModelIOException(e);
		}
		logger.info("==Annotated RDF ==" + fileOut + " ANNOT: " + inserted.size());
		if (inserted.size() == 0) {
			File file = new File(fileOut);
			file.delete();
		}
		return inserted;
	}
	
	/**
	 * Serializes an object into RDF.
	 * @param model
	 * @param datasetURL
	 * @param baseURL
	 * @param annotConBaseURL
	 * @param distribution
	 * @return
	 * @throws URISyntaxException
	 */
	protected abstract URI createAnnotationInModel(Model model, String datasetURL, String baseURL, String annotConBaseURL, T distribution) throws URISyntaxException;
}
