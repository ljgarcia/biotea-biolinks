package biolinks.util;

import ws.biotea.ld2rdf.util.annotation.Annotator;

public enum Model {
	BIOLINKS("biolinks", "DistributionParser", "biolinks.parser", BiolinksResourceConfig.getModelURL("biolinks"))
	,UMLS("umls", "DistributionParser", "biolinks.parser", BiolinksResourceConfig.getModelURL("umls"))
	;
	
	String name;
	String parserClass;
	String classLocation;
	String modelURI;
	
	private Model(String name, String parser, String classLocation, String uri) {
		this.name = name;
		this.parserClass = parser;
		this.classLocation = classLocation;
		this.modelURI = uri;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the parser.
	 */
	public String getParserClass() {
		return this.parserClass;
	}

	/**
	 * @return the classLocation.
	 */
	public String getClassLocation() {
		return classLocation;
	}

	/**
	 * @return the uri
	 */
	public String getModelURI() {
		return modelURI;
	}
	
	public String getClassFullName(Annotator annotator) {
		return this.getClassLocation() + "." + annotator.getName().toUpperCase() + this.getParserClass(); 
	}
}

