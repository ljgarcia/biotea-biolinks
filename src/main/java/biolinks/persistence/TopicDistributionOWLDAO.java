package biolinks.persistence;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

import ws.biotea.ld2rdf.util.GenerateMD5;
import ws.biotea.ld2rdf.util.ResourceConfig;
import biolinks.model.Biolink;
import biolinks.model.Topic;
import biolinks.model.TopicDistribution;
import biolinks.util.BiolinksResourceConfig;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class TopicDistributionOWLDAO extends ObjectModelDAO<TopicDistribution> {
	/**
	 * 
	 * @param model
	 * @param datasetURL
	 * @param baseURL
	 * @param annotConBaseURL
	 * @param distribution
	 * @return
	 * @throws URISyntaxException
	 */
	@Override
	protected URI createAnnotationInModel(Model model, String datasetURL, String baseURL, String annotConBaseURL, TopicDistribution distribution) throws URISyntaxException {
		String distributionClazz = TopicDistribution.DISTRIBUTION_CLASS;	
		try {
			distributionClazz = distribution.getClass().getField("DISTRIBUTION_CLASS").get(null).toString();
		} catch (Exception e) {}
		
		Property opType = model.getProperty(ResourceConfig.OP_RDF_TYPE);		
		Property opRequiredDoc = model.getProperty(TopicDistribution.DISTRIBUTION_OP_REQUIRED_DOC);
		Property opAnnotator = model.getProperty(TopicDistribution.DISTRIBUTION_OP_ANNOTATOR);
		Property dpTF = model.getProperty(TopicDistribution.BIOTEA_TOTAL_TF);
		
		String modelClazz = Biolink.BIOLINK_MODEL_CLASS;
		Property opHasModel = model.getProperty(TopicDistribution.DISTRIBUTION_OP_HAS_MODEL);
		Property opSubjectModel = model.getProperty(TopicDistribution.MODEL_OP_SUBJECT);
		Property dpLabelModel = model.getProperty(TopicDistribution.MODEL_DP_LABEL);		
		
		Property opCreatedBy = model.getProperty(TopicDistribution.DISTRIBUTION_OP_CREATED_BY);
		Property dpCreatedOn = model.getProperty(TopicDistribution.DISTRIBUTION_OP_CREATED_ON);
		
		String topicClazz = Topic.TOPIC_CLASS;
		Property opHasTopic = model.getProperty(TopicDistribution.DISTRIBUTION_OP_HAS_TOPIC);
		Property opSubject = model.getProperty(Topic.TOPIC_OP_SUBJECT);
		Property dpLabel = model.getProperty(Topic.TOPIC_DP_LABEL);
		Property dpScore = model.getProperty(Topic.TOPIC_DP_SCORE);
		
		String md5 = GenerateMD5.getInstance().getMD5Hash(distribution.getDocURI().toString() 
				+ distribution.getModelLabel() + distribution.getAnnotatorURI().toString());
		distribution.setId(TopicDistribution.DISTRIBUTION_ID + md5);
		
		Resource distributionRes;
		Resource distributionClazzRes = model.createResource(distributionClazz);
		distribution.setURI(new URI(baseURL + distribution.getId()));
		distributionRes = model.createResource(distribution.getURI().toString(), distributionClazzRes);
		if (!distributionClazz.equals(TopicDistribution.DISTRIBUTION_CLASS)) {
			distributionRes.addProperty(opType, distributionClazz);
		}		
		//Document
		distributionRes.addProperty(opRequiredDoc, model.createResource(distribution.getDocURI().toString()));
		//Annotator
		distributionRes.addProperty(opAnnotator, model.createResource(distribution.getAnnotatorURI().toString()));
		//Provenance
		Resource resCreator = model.createResource(ResourceConfig.BIOTEA_RDFIZATOR);
		distributionRes.addProperty(opCreatedBy, resCreator);
		distributionRes.addLiteral(dpCreatedOn, Calendar.getInstance());
		//total tf
		if (distribution.getTF() != null) {
			distributionRes.addProperty(dpTF, "" + distribution.getTF(), XSDDatatype.XSDint);
		}		
		//Model
		if ((distribution.getModelLabel() != null) || (distribution.getModelURI() != null)) {
			Resource modelClazzRes = model.createResource(modelClazz);
			Resource modelRes = model.createResource(
				BiolinksResourceConfig.BASE_URL_MODEL + Biolink.BIOLINK_MODEL_ID + Calendar.getInstance().getTimeInMillis(), 
				modelClazzRes);
			if (distribution.getModelLabel() != null) {
				modelRes.addLiteral(dpLabelModel, distribution.getModelLabel());
			}
			if (distribution.getModelURI() != null) {
				modelRes.addProperty(opSubjectModel, distribution.getModelURI().toString());
			}			
			distributionRes.addProperty(opHasModel, modelRes);
		}		
		//Groups, i.e., topics
		int topicCounter = 1;
		for (Topic topic: distribution.getTopics()) {
			if (((topic.getTopicLabel() != null) || (topic.getTopicURI() != null)) && (topic.getScore() != null)) {
				Resource topicClazzRes = model.createResource(topicClazz);
				Resource topicRes;
				if (topic.getTopicLabel() != null) {
					topicRes = model.createResource(
							BiolinksResourceConfig.BASE_URL_MODEL + Topic.TOPIC_ID + topic.getTopicLabel() + "_" + md5 + "_" + Calendar.getInstance().getTimeInMillis(), 
							topicClazzRes);
					topicRes.addLiteral(dpLabel, topic.getTopicLabel());
				} else {
					topicRes = model.createResource(
							BiolinksResourceConfig.BASE_URL_MODEL + Topic.TOPIC_ID + topicCounter + "_" + md5 + "_" + Calendar.getInstance().getTimeInMillis(), 
							topicClazzRes);
				}
				if (topic.getTopicURI() != null) {
					topicRes.addProperty(opSubject, topic.getTopicURI().toString());
				}
				if (topic.getScore() != null) {
					topicRes.addProperty(dpScore, "" + topic.getScore(), XSDDatatype.XSDdouble);
				}
				distributionRes.addProperty(opHasTopic, topicRes);
				topicCounter++;
			}			
		}				
		return distribution.getURI();
	}
}
