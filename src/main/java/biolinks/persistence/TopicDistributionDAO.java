package biolinks.persistence;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.jena.riot.RDFFormat;

import ws.biotea.ld2rdf.exception.RDFModelIOException;
import biolinks.model.TopicDistribution;

import com.hp.hpl.jena.rdf.model.Model;

import edu.stanford.smi.protege.exception.OntologyLoadException;

@Deprecated
public interface TopicDistributionDAO {
	/**
	 * Inserts a topic distribution into a model, returns the generated URI.
	 * @param datasetURL
	 * @param baseURL
	 * @param distribution
	 * @param modelOut
	 * @return
	 */
	public URI insertDistribution(String datasetURL, String baseURL, String annotConBaseURL, TopicDistribution distribution, Model modelOut) throws URISyntaxException;
	/**
	 * Inserts a topic distribution into a model and writes the model down, returns the generated URI.
	 * @param datasetURL
	 * @param baseURL
	 * @param distribution
	 * @param fileOut
	 * @param format
	 * @param empty
	 * @return
	 */
	public URI insertBiolink(String datasetURL, String baseURL, String annotConBaseURL, TopicDistribution distribution, String fileOut
			, RDFFormat format, boolean empty) throws FileNotFoundException, ClassNotFoundException, OntologyLoadException, URISyntaxException;
	/**
	 * Inserts multiple topic distributions into a model, returns a list with the distributions inserted.
	 * @param datasetURL
	 * @param baseURL
	 * @param distributions
	 * @param modelOut
	 * @return
	 */
	public List<TopicDistribution> insertDistributions(String datasetURL, String baseURL, String annotConBaseURL, List<TopicDistribution> distributions, Model modelOut);
	/**
	 * Inserts multiple topic distributions into a model and write the model down, returns a list with the distributions inserted.
	 * @param datasetURL
	 * @param baseURL
	 * @param distributions
	 * @param fileOut
	 * @param format
	 * @param empty
	 * @return
	 */
	public List<TopicDistribution> insertDistributions(String datasetURL, String baseURL, String annotConBaseURL, List<TopicDistribution> distributions, String fileOut
			, RDFFormat format, boolean empty) throws RDFModelIOException;
}
