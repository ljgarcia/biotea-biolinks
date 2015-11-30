package biolinks.persistence;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import ws.biotea.ld2rdf.util.GenerateMD5;
import ws.biotea.ld2rdf.util.ResourceConfig;
import biolinks.model.Biolink;
import biolinks.model.AnnotatedConcept;
import biolinks.util.BiolinksResourceConfig;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;


public class BiolinksOWLDAO extends ObjectModelDAO<Biolink> {
	/**
	 * 
	 * @param model
	 * @param datasetURL
	 * @param baseURL
	 * @param annotConBaseURL
	 * @param biolink
	 * @return
	 * @throws URISyntaxException
	 */
	protected URI createAnnotationInModel(Model model, String datasetURL, String baseURL, String annotConBaseURL, Biolink biolink) throws URISyntaxException {
		String biolinkClazz = Biolink.BIOLINK_CLASS;	
		try {
			biolinkClazz = biolink.getClass().getField("BIOLINK_CLASS").get(null).toString();
		} catch (Exception e) {}
		
		Property opType = model.getProperty(ResourceConfig.OP_RDF_TYPE);		
		Property opRequiredDoc = model.getProperty(Biolink.BIOLINK_OP_REQUIRED_DOC);
		Property opRelatedDoc = model.getProperty(Biolink.BIOLINK_OP_RELATED_DOC);
		Property opAnnotator = model.getProperty(Biolink.BIOLINK_OP_ANNOTATOR);		
		Property dpScore = model.getProperty(Biolink.BIOLINK_DP_SCORE);
		
		Property opCreatedBy = model.getProperty(Biolink.BIOLINK_OP_CREATED_BY);
		Property dpCreatedOn = model.getProperty(Biolink.BIOLINK_OP_CREATED_ON);
		
		String semTermClazz = AnnotatedConcept.ANNOT_CON_CLASS;
		Property opReferencedTerm = model.getProperty(Biolink.BIOLINK_OP_REFERENCED_TERM);
		Property dpTF = model.getProperty(AnnotatedConcept.BIOTEA_OCURRENCES);
		Property dpIDF = model.getProperty(AnnotatedConcept.BIOTEA_IDF);
		Property opReferencedConcept = model.getProperty(AnnotatedConcept.ANNOT_CON_OP_CONCEPT);
		
		String modelClazz = Biolink.BIOLINK_MODEL_CLASS;
		Property opModel = model.getProperty(Biolink.BIOLINK_OP_MODEL);
		Property opSubject = model.getProperty(Biolink.BIOLINK_OP_SUBJECT);
		Property dpLabel = model.getProperty(Biolink.BIOLINK_DP_LABEL);
		Property dpGroup = model.getProperty(Biolink.BIOLINK_DP_GROUP);
		
		biolink.setId(
				Biolink.BIOLINK_ID + GenerateMD5.getInstance().getMD5Hash(Biolink.BIOLINK_ID + 
					biolink.getGivenDocURI().getPath() + "_" + biolink.getAnalyzedDocURI() + "_" + 
					biolink.getAnnotatorURI().getPath()).replaceAll(ResourceConfig.CHAR_NOT_ALLOWED, "-"));
		
		Resource biolinkRes;
		Resource biolinkClazzRes = model.createResource(biolinkClazz);
		biolink.setURI(new URI(baseURL + biolink.getId()));
		biolinkRes = model.createResource(biolink.getURI().toString(), biolinkClazzRes);
		if (!biolinkClazz.equals(Biolink.BIOLINK_CLASS)) {
			biolinkRes.addProperty(opType, biolinkClazz);
		}		
		//Documents
		biolinkRes.addProperty(opRequiredDoc, model.createResource(biolink.getGivenDocURI().toString()));
		biolinkRes.addProperty(opRelatedDoc, model.createResource(biolink.getAnalyzedDocURI().toString()));
		//Annotator
		biolinkRes.addProperty(opAnnotator, model.createResource(biolink.getAnnotatorURI().toString()));
		//score
		if (biolink.getScore() != null) {
			biolinkRes.addProperty(dpScore, "" + biolink.getScore(), XSDDatatype.XSDdouble);
		}
		//Provenance
		Resource resCreator = model.createResource(ResourceConfig.BIOTEA_RDFIZATOR);
		biolinkRes.addProperty(opCreatedBy, resCreator);
		biolinkRes.addLiteral(dpCreatedOn, Calendar.getInstance());
		//Annotated concepts
		if (!biolink.getSharedTerms().isEmpty()) {
			Resource annotatedConClazzRes = model.createResource(semTermClazz);
			for (AnnotatedConcept annotatedCon: biolink.getSharedTerms()) {
				StringBuffer allTopics = new StringBuffer();
				for (URI uri: annotatedCon.getTopics()) {
					allTopics.append(uri.toString() + "-");					
				}
				annotatedCon.setId(
						AnnotatedConcept.ANNOT_CON_ID + GenerateMD5.getInstance().getMD5Hash(AnnotatedConcept.ANNOT_CON_ID + 
						allTopics.toString() + "_" + biolink.getAnalyzedDocURI() +
						annotatedCon.getTF() + "_" + annotatedCon.getIDF())
						.replaceAll(ResourceConfig.CHAR_NOT_ALLOWED, "-"));
				annotatedCon.setUri(new URI(annotConBaseURL + annotatedCon.getId()));
				Resource annotatedConRes = model.createResource(annotatedCon.getUri().toString(), annotatedConClazzRes);
				annotatedConRes.addProperty(opType, annotatedConClazzRes);
								
				if (annotatedCon.getTF() != null) {
					annotatedConRes.addProperty(dpTF, "" + annotatedCon.getTF(), XSDDatatype.XSDint);
				}		
				if (annotatedCon.getIDF() != null) {
					annotatedConRes.addProperty(dpIDF, "" + annotatedCon.getIDF(), XSDDatatype.XSDdouble);
				}
				
				for (URI topic: annotatedCon.getTopics()) {
					annotatedConRes.addProperty(opReferencedConcept, model.createResource(topic.toString()));
				}
				
				biolinkRes.addProperty(opReferencedTerm, annotatedConRes);
			}
		}
		//Model and groups
		if ((biolink.getModelLabel() != null) || (biolink.getModelURI() != null)) {
			Resource modelClazzRes = model.createResource(modelClazz);
			Resource modelRes = model.createResource(
				BiolinksResourceConfig.BASE_URL_MODEL + Biolink.BIOLINK_MODEL_ID + Calendar.getInstance().getTimeInMillis(), 
				modelClazzRes);
			if (biolink.getModelLabel() != null) {
				modelRes.addLiteral(dpLabel, biolink.getModelLabel());
			}
			if (biolink.getModelURI() != null) {
				modelRes.addProperty(opSubject, biolink.getModelURI().toString());
			}			
			biolinkRes.addProperty(opModel, modelRes);
		}
		for (String group: biolink.getGroups()) {
			biolinkRes.addLiteral(dpGroup, group);
		}
		return biolink.getURI();
	}
}
