package biolinks.persistence;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.jena.riot.RDFFormat;

import ws.biotea.ld2rdf.exception.RDFModelIOException;
import biolinks.model.Biolink;

import com.hp.hpl.jena.rdf.model.Model;

import edu.stanford.smi.protege.exception.OntologyLoadException;

@Deprecated
public interface BiolinksDAO {
	/**
	 * Inserts a biolink into a model, returns the generated URI.
	 * @param datasetURL
	 * @param baseURL
	 * @param biolink
	 * @param modelOut
	 * @return
	 */
	public URI insertBiolink(String datasetURL, String baseURL, String annotConBaseURL, Biolink biolink, Model modelOut) throws URISyntaxException;
	/**
	 * Inserts a biolink into a model and writes the model down, returns the generated URI.
	 * @param datasetURL
	 * @param baseURL
	 * @param biolink
	 * @param fileOut
	 * @param format
	 * @param empty
	 * @return
	 */
	public URI insertBiolink(String datasetURL, String baseURL, String annotConBaseURL, Biolink biolink, String fileOut
			, RDFFormat format, boolean empty) throws FileNotFoundException, ClassNotFoundException, OntologyLoadException, URISyntaxException;
	/**
	 * Inserts multiple biolinks into a model, returns a list with the biolinks inserted.
	 * @param datasetURL
	 * @param baseURL
	 * @param biolinks
	 * @param modelOut
	 * @return
	 */
	public List<Biolink> insertBiolinks(String datasetURL, String baseURL, String annotConBaseURL, List<Biolink> biolinks, Model modelOut);
	/**
	 * Inserts multiple biolinks into a model and write the model down, returns a list with the biolinks inserted.
	 * @param datasetURL
	 * @param baseURL
	 * @param biolinks
	 * @param fileOut
	 * @param format
	 * @param empty
	 * @return
	 */
	public List<Biolink> insertBiolinks(String datasetURL, String baseURL, String annotConBaseURL, List<Biolink> biolinks, String fileOut
			, RDFFormat format, boolean empty) throws RDFModelIOException;
}
