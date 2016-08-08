package biolinks.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

import ws.biotea.ld2rdf.annotation.exception.ArticleParserException;
import ws.biotea.ld2rdf.annotation.exception.NoResponseException;
import ws.biotea.ld2rdf.annotation.parser.AnnotatorParser;
import ws.biotea.ld2rdf.annotation.parser.CMAParser;
import ws.biotea.ld2rdf.exception.RDFModelIOException;
import ws.biotea.ld2rdf.rdf.model.aoextended.AnnotationE;
import ws.biotea.ld2rdf.rdf.persistence.ao.AnnotationOWLReader;
import ws.biotea.ld2rdf.rdf.persistence.ao.ConnectionLDModel;
import ws.biotea.ld2rdf.util.ResourceConfig;
import ws.biotea.ld2rdf.util.annotation.Annotator;
import ws.biotea.ld2rdf.util.annotation.ConstantConfig;

import com.hp.hpl.jena.rdf.model.Model;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import biolinks.model.Topic;
import biolinks.model.TopicDistribution;
import biolinks.persistence.ObjectModelDAO;
import biolinks.persistence.TopicDistributionOWLDAO;
import biolinks.util.BiolinksResourceConfig;

public class CMADistributionParser implements TopicDistributionParser {
	//lambda entropy classifier	
	Logger logger = Logger.getLogger(this.getClass());
	private final double PMRA_MIU = 0.013, PMRA_LAMBDA = 0.022;
	private List<AnnotationE> lstAnnotations;
	private TopicDistribution distribution;
	private AnnotatorParser annotatorParser;
	private String model;

	/**
	 * Constructor.
	 * @param fromURL
	 * @param onlyUMLS
	 * @param titleTwice
	 * @param onlyTitleAndAbstract
	 * @param model
	 */
	public CMADistributionParser(boolean fromURL, boolean onlyUMLS, boolean titleTwice, boolean onlyTitleAndAbstract, String model, ConstantConfig onto) {		
		this.annotatorParser = new CMAParser(fromURL, onlyUMLS, titleTwice, onlyTitleAndAbstract, true, onto);
		this.model = model;
	}
	
	public CMADistributionParser(String model) {
		this.model = model;
	}

	@Override
	public TopicDistribution parse(String documentId) throws IOException,
			URISyntaxException, NoResponseException, ArticleParserException {
		this.lstAnnotations = this.annotatorParser.parse(documentId);		
		this.parseDistribution();
		return this.distribution;
	}

	@Override
	public TopicDistribution parse(File file, RDFFormat format) throws IOException, URISyntaxException, NoResponseException, ClassNotFoundException, OntologyLoadException {
		this.lstAnnotations = AnnotationOWLReader.retrieveFromFile(file, format);
		this.parseDistribution();
		return this.distribution;
	}
	
	@Override
	public TopicDistribution parse(File file) throws IOException, URISyntaxException, NoResponseException, ClassNotFoundException, OntologyLoadException {
		this.lstAnnotations = AnnotationOWLReader.retrieveFromFile(file, RDFFormat.RDFXML_ABBREV);
		this.parseDistribution();
		return this.distribution;
	}
	
	/**
	 * Parses a TopicDistribution object response from a file in order to extract its annotations.
	 * @param documentId
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
	@Override
	public TopicDistribution parse(List<AnnotationE> lst) throws IOException, URISyntaxException, NoResponseException {
		this.lstAnnotations = lst;
		this.parseDistribution();
		return this.distribution;
	}
	
	/**
	 * Parses the content of a reader.
	 * @param reader
	 * @param documentId
	 * @throws IOException
	 * @throws NoResponseException
	 * @throws URISyntaxException
	 */
	private void parseDistribution() throws IOException, NoResponseException, URISyntaxException {
		if (this.lstAnnotations.size() == 0) {
			throw new NoResponseException("No annotations retrieved for topic distribution");
		}
		
		this.distribution = new TopicDistribution();
		this.distribution.setAnnotatorURI(new URI(Annotator.CMA.getServiceURI()));
		this.distribution.setDocURI(this.lstAnnotations.get(0).getResource().getUri());
		this.distribution.setModelLabel(this.model);
		this.distribution.setModelURI(new URI(BiolinksResourceConfig.getModelURL(this.model)));
		int totalTF = 0;
		for (AnnotationE annot: this.lstAnnotations) {
			totalTF += annot.getFrequency();
		}
		this.distribution.setTF(totalTF);
		
		Map<String, List<String>> groupsAndTypes = BiolinksResourceConfig.getGroupsAndTypes(this.model);
		Map<String, Double> lambdas = BiolinksResourceConfig.getLambdaEntropyClassifier(this.model);
		
		Map<String, Double> groupEntropy = new HashMap<String, Double>();
		double totalEntropy = 0;
		for (String group: groupsAndTypes.keySet()) {
			double fGroup = fGroup(group, groupsAndTypes.get(group), totalTF);
			double value = Math.pow(Math.E, lambdas.get(group) * fGroup);
			//double value = Math.pow(Math.E, 1 * fGroup);
			totalEntropy += value;
			groupEntropy.put(group, value);
		}
		
		for (String group: groupsAndTypes.keySet()) {
			Topic topic = new Topic();
			topic.setTopicLabel(group);
			double groupEntropyValue = groupEntropy.get(group);
			topic.setScore(groupEntropyValue / totalEntropy);
			if (groupEntropyValue != 1.0) {
				this.distribution.getTopics().add(topic);
			}			
		}		
	}
	
	
	/**
	 * f(group, doc) = sum <annot in group>( pow(1+pow(miu/lambda, tf(annot)-1)*pow(exp, -(miu-lambda)*tf(annot in group)), -1) * idf(annot) )
	 * @param group
	 * @param types
	 * @param totalTF
	 * @return
	 */
	private double fGroup(String group, List<String> types, int totalTF) {
		double weight = 0.0;
		for (AnnotationE annot: this.lstAnnotations) {
			if (this.belongsToGroup(types, annot)) {
				weight += Math.pow(
						1 + (
								Math.pow(this.PMRA_MIU/this.PMRA_LAMBDA, annot.getFrequency()*annot.getIDF() - 1) *
								Math.pow(Math.E, -(this.PMRA_MIU - this.PMRA_LAMBDA) * totalTF)
							)
						, -1);
			}
		}
		return weight;
	}
	
	/**
	 * True if annot has any topic with a type belonging to types.
	 * @param types
	 * @param annot
	 * @return
	 */
	private boolean belongsToGroup(List<String> types, AnnotationE annot) {
		for (ws.biotea.ld2rdf.rdf.model.ao.Topic topic: annot.getTopics()) {
			for (String sty: topic.getUmlsType()) {
				if (types.contains(sty)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public TopicDistribution serializeToFile(String fullPathName,
			RDFFormat format, ObjectModelDAO<TopicDistribution> dao,
			boolean empty, boolean blankNode) throws RDFModelIOException, FileNotFoundException, ClassNotFoundException, OntologyLoadException, URISyntaxException {
		dao.insert(ResourceConfig.BIOTEA_DATASET, BiolinksResourceConfig.BASE_URL_TOPIC_DISTRIBUTION, null, this.distribution, fullPathName, format, empty);
		return this.distribution;
	}

	@Override
	public TopicDistribution serializeToModel(Model model,
			ObjectModelDAO<TopicDistribution> dao, boolean blankNode)
			throws RDFModelIOException, URISyntaxException {
		dao.insert(ResourceConfig.BIOTEA_DATASET, BiolinksResourceConfig.BASE_URL_TOPIC_DISTRIBUTION, null, this.distribution, model);
		return this.distribution;
	}
	public static void main(String[] args) throws IOException, URISyntaxException, NoResponseException, ClassNotFoundException, OntologyLoadException, RDFModelIOException {
		TopicDistributionParser parser = new CMADistributionParser(false, true, true, true, "biolinks", ConstantConfig.AO);
		parser.parse(new File("C:/Users/Leyla/Desktop/PMID11707154.txt"));
		ObjectModelDAO<TopicDistribution> dao = new TopicDistributionOWLDAO();
		ConnectionLDModel conn = new ConnectionLDModel();
    	Model model = conn.openJenaModel();
		parser.serializeToModel(model, dao, false);
		try {
			conn.closeAndWriteJenaModel("C:/Users/Leyla/Desktop/PMID11707154.rdf", RDFFormat.RDFXML_ABBREV);
		} catch (Exception e) {
			e.printStackTrace();				
		}
	}
}
