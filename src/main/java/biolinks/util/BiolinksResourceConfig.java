package biolinks.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.jena.riot.RDFFormat;

import ws.biotea.ld2rdf.util.ResourceConfig;
import ws.biotea.ld2rdf.util.annotation.Annotator;

public class BiolinksResourceConfig {
	private static ResourceBundle res = ResourceBundle.getBundle("biolinks");
	public static String BASE_URL_BIOLINK;
	public static String BASE_URL_ANNOTATED_CONCEPT;
	public static String BASE_URL_TOPIC_DISTRIBUTION;
	public static String BASE_URL_MODEL;
	public static String BASE_URL_TOPIC;
	
	static {
		initStaticPatterns();
	}
	
	private static void initStaticPatterns() {
		if (ResourceConfig.getUseBio2RDF(null)) {
			//annotations
			BASE_URL_BIOLINK = ResourceConfig.getBioteaURL(null) + ResourceConfig.getDatasetPrefix() + "_resource:biolink_";
			BASE_URL_ANNOTATED_CONCEPT = ResourceConfig.getBioteaURL(null) + ResourceConfig.getDatasetPrefix() + "_resource:annotatedConcept";
			BASE_URL_MODEL = ResourceConfig.getBioteaURL(null) + ResourceConfig.getDatasetPrefix() + "_resource:topicModel";
			BASE_URL_TOPIC = ResourceConfig.getBioteaURL(null) + ResourceConfig.getDatasetPrefix() + "_resource:topicTopic";
			BASE_URL_TOPIC_DISTRIBUTION = ResourceConfig.getBioteaURL(null) + ResourceConfig.getDatasetPrefix() + "_resource:topicDistribution";
		} else {
			//annotations
			BASE_URL_BIOLINK = ResourceConfig.getBioteaURL(null) + "biolink/" + ResourceConfig.getDatasetPrefix() + "_resource/";
			BASE_URL_ANNOTATED_CONCEPT = ResourceConfig.getBioteaURL(null) + "annotatedConcept/" + ResourceConfig.getDatasetPrefix() + "_resource/";
			BASE_URL_MODEL = ResourceConfig.getBioteaURL(null) + "topicModel/" + ResourceConfig.getDatasetPrefix() + "_resource/";
			BASE_URL_TOPIC = ResourceConfig.getBioteaURL(null) + "topicTopic/" + ResourceConfig.getDatasetPrefix() + "_resource/";
			BASE_URL_TOPIC_DISTRIBUTION = ResourceConfig.getBioteaURL(null) + "topicDistribution/" + ResourceConfig.getDatasetPrefix() + "_resource/";
		}
    }
	
	//caching
    public static boolean getSimilarityCaching(Annotator annotator) {
    	String str = ResourceConfig.getProperty("cache.similarity." + annotator.getName());
    	String path = BiolinksResourceConfig.getSimilarityCachingPath(annotator);
    	return (str.length() == 0) ? false 
			: (path.length() == 0) ? false : Boolean.parseBoolean(str);   	
    }
    
    public static String getSimilarityCachingPath(Annotator annotator) {
    	return ResourceConfig.getProperty("cache.similarity." + annotator.getName() + "." + ResourceConfig.getDatasetPrefix() + ".path");
    }
    
    public static String getCachingFileName(String baseId, String otherId, Annotator annotator, String model, List<String> groups, RDFFormat format) {
    	String extension = format == RDFFormat.JSONLD ? ".json" : ".rdf";
    	String additional = (groups != null) && (model != null) 
			? "_" + model + "_" + groups.toString().replaceAll(ResourceConfig.CHAR_NOT_ALLOWED, "") : "";
		return BiolinksResourceConfig.getSimilarityCachingPath(annotator) + baseId + "_" + otherId + additional + extension;
    }
    
    public static boolean getDistributionCaching(Annotator annotator) {
    	String str = ResourceConfig.getProperty("cache.distribution." + annotator.getName());
    	String path = BiolinksResourceConfig.getSimilarityCachingPath(annotator);
    	return (str.length() == 0) ? false 
			: (path.length() == 0) ? false : Boolean.parseBoolean(str);   	
    }
    
    public static String getDistributionCachingPath(Annotator annotator) {
    	return ResourceConfig.getProperty("cache.distribution." + annotator.getName() + "." + ResourceConfig.getDatasetPrefix() + ".path");
    }
    
    public static String getCachingDistributionFileName(String baseId, Annotator annotator, String model, RDFFormat format) {
    	String extension = format == RDFFormat.JSONLD ? ".json" : ".rdf";
		return BiolinksResourceConfig.getDistributionCachingPath(annotator) + baseId + "_distribution_" + 
    			 model + extension;
    }
    
  //saving similarities
    public static boolean getSimilaritySaving(Annotator annotator) {
    	String str = ResourceConfig.getProperty("save.similarity." + annotator.getName());
    	String path = BiolinksResourceConfig.getSimilaritySavingPath(annotator);
    	return (str.length() == 0) ? false 
			: (path.length() == 0) ? false : Boolean.parseBoolean(str);   	
    }
    
    public static boolean getSimilaritySavingReplace(Annotator annotator) {
    	String str = ResourceConfig.getProperty("save.similarity." + annotator.getName() + ".replace");
    	return Boolean.parseBoolean(str);
    }
    
    public static String getSimilaritySavingPath(Annotator annotator) {
    	return ResourceConfig.getProperty("save.similarity." + annotator.getName() + "." + ResourceConfig.getDatasetPrefix() + ".path");
    }
    
    //saving distributions
    public static boolean getDistributionSaving(Annotator annotator) {
    	String str = ResourceConfig.getProperty("save.distribution." + annotator.getName());
    	String path = BiolinksResourceConfig.getSimilaritySavingPath(annotator);
    	return (str.length() == 0) ? false 
			: (path.length() == 0) ? false : Boolean.parseBoolean(str);   	
    }
    
    public static boolean getDistributionSavingReplace(Annotator annotator) {
    	String str = ResourceConfig.getProperty("save.distribution." + annotator.getName() + ".replace");
    	return Boolean.parseBoolean(str);
    }
    
    public static String getDistributionSavingPath(Annotator annotator) {
    	return ResourceConfig.getProperty("save.distribution." + annotator.getName() + "." + ResourceConfig.getDatasetPrefix() + ".path");
    }
    
    public static String getSavingFileName(String baseId, String otherId, String model, List<String> groups, Annotator annotator, RDFFormat format) {
    	String extension = format == RDFFormat.JSONLD ? ".json" : ".rdf";
    	String additional = (groups != null) && (model != null) 
    			? "_" + model + "_" + groups.toString().replaceAll(ResourceConfig.CHAR_NOT_ALLOWED, "") : "";
		return BiolinksResourceConfig.getSimilaritySavingPath(annotator) + baseId + "_" + otherId + additional + extension;
    }

    //Groups
    public static List<String> getModels() {
    	try {
    		return Arrays.asList(res.getString("models").split(","));
    	} catch (Exception e) {
    		String[] models =  {"umls", "biolinks"};
    		return Arrays.asList(models);
    	}
    }
    
    public static List<String> getGroups(String model) {
    	Enumeration<String> keys = res.getKeys();
    	List<String> lst = new ArrayList<String>();
    	while(keys.hasMoreElements()) {
    		String key = keys.nextElement();
    		if (key.startsWith(model+".group")) {
    			lst.add(key.substring(model.length()+1));
    		}    		
    	}
    	return lst;
    }
    
    public static List<String> getTypes(String model, String group) {
    	String key = model + ".group." + group;
    	if (res.containsKey(key)) {
    		return Arrays.asList(res.getString(key).split(","));
    	} else {
    		return new ArrayList<String>();
    	}
    }
    
    public static Map<String,List<String>> getGroupsAndTypes(String model) {
    	Enumeration<String> keys = res.getKeys();
    	Map<String, List<String>> map = new HashMap<String, List<String>>();
    	while(keys.hasMoreElements()) {
    		String key = keys.nextElement();
    		if (key.startsWith(model+".group.")) {
    			map.put(key.substring(model.length()+7), Arrays.asList(res.getString(key).split(",")));
    		}    		
    	}
    	return map;
    }
    
    public static Map<String, Double> getLambdaEntropyClassifier(String model) {
    	Enumeration<String> keys = res.getKeys();
    	Map<String, Double> map = new HashMap<String, Double>();
    	while(keys.hasMoreElements()) {
    		String key = keys.nextElement();
    		if (key.startsWith(model+".lambda.")) {
    			map.put(key.substring(model.length()+8), Double.parseDouble(res.getString(key)));
    		}    		
    	}
    	return map;
    }
    
    public static String getModelName(String model) {
    	String key = model + ".model";
    	if (res.containsKey(key)) {
    		return res.getString(key);
    	} else {
    		return "biolinks";
    	}
    }
    
    public static String getModelURL(String model) {
    	String key = model + ".url";
    	if (res.containsKey(key)) {
    		return res.getString(key);
    	} else {
    		return "http://biotea.ws/topicModel/biolinks";
    	}
    }
    
    public static boolean isInGroup(String model, String group, String sty) {
    	List<String> types = BiolinksResourceConfig.getTypes(model, group);
    	return types.contains(sty);
    }
}
