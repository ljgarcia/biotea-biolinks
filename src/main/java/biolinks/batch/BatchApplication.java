package biolinks.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

import biolinks.util.Model;
import ws.biotea.ld2rdf.annotation.exception.ArticleParserException;
import ws.biotea.ld2rdf.annotation.exception.UnsupportedFormatException;
import ws.biotea.ld2rdf.util.annotation.Annotator;


public class BatchApplication {
	protected int poolSize;
    protected int maxPoolSize;
    protected long keepAliveTime;
    protected ThreadPoolExecutor threadPool = null;
    protected final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
    private static final Logger LOGGER = Logger.getLogger(BatchApplication.class);
    /**
     * Default constructor, it defines an initial pool with 5 threads, a maximum of 10 threads,
     * and a keepAlive time of 300 seconds.
     */
	public BatchApplication() {
		this(10, 10, 300);
	}
	
	/**
     * Constructor with parameters, it enables definition of the initial pool size, maximum pool size,
     * and keep alive time in seconds; it initializes the ThreadPoolExecutor.
     * @param poolSize Initial pool size
     * @param maxPoolSize Maximum pool size
     * @param keepAliveTime Keep alive time in seconds
     */
    protected BatchApplication(int poolSize, int maxPoolSize, long keepAliveTime) {
    	this.poolSize = poolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.threadPool = new ThreadPoolExecutor(poolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, queue);        
    }
    
    /**
     * Run a task with the thread pool and modifies the waiting queue list as needed.
     * @param task
     */
    protected void runTask(Runnable task) {
        threadPool.execute(task);
        LOGGER.debug("Task count: " + queue.size());
    }
    /**
     * Shuts down the ThreadPoolExecutor.
     */
    public void shutDown() {
        threadPool.shutdown();
    }

    /**
     * Informs whether or not the threads have finished all pending executions.
     * @return
     */
    public boolean isTerminated() {
    	//this.handler.getLogger().debug("Task count: " + queue.size());
        return this.threadPool.isTerminated();
    }

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();		

		String usage = "Usage: -in <input dir> (mandatory if input will be processed from a directory, "
				+ "SHOULD NOT be used if input will be process from annotator URL, "
				+ "\nthis dir should contain all annotation files corresponding to ids specified in <compare> file) if -compare is used, "
				+ "\notherwise it should contain all annotator files corresponding to ids in <distribute> if -distribute is used, "
				+ "\notherwise it should contain all annotator files corresponding to ids in <distAnnot> if -distAnnot is use"
				
				+ "\n-out <output dir> (mandatory)"
				
				+ "\n-compare <document ids file> (one of -compare, -distribute, or -distAnnot is mandatory), "
				+ "\nit MUST provide a full path to a file with a list of document ids ready to be processed; "
				+ "\nif <input dir> is used, document ids MUSt correspond to an annotation file in <input dir> with .rdf extension, "
				+ "\none pair of ids baseId \t otherId per line, i.e. documents to be compared)"
				
				+ "\n-distribute <document ids file> (one of -compare, -distribute, or -distAnnot is mandatory), "
				+ "\nit MUSt provide a full path to a file with a list of document ids ready to be processed; "
				+ "\nif <input dir> is used, document ids MUST correspond to an annotator file in <input dir> with .<ext> extension"
				
				+ "\n-distAnnot <document ids file> (one of -compare, -distribute, or -distAnnot is mandatory), "
				+ "\nit MUSt provide a full path to a file with a list of document ids ready to be processed; "
				+ "\nif <input dir> is used, document ids MUST correspond to an annotator file in <input dir> with .<ext> extension"
				+ "\n-ext file extension expected in files in <input dir>(mandatory only if -distAnnot AND -in are used)"
				
				+ "\n-model grouping model to be used, <either biolinks or umls> (mandatory if -distribute or -distAnnot is used, "
				+ "\nmandatory if -compare and -groups are used together, biolinks by default)"	
				
				+ "\n-groups <list of valid groups according to the model separated by comma> (optional, to be used only in "
				+ "\nconjuction with -compare so the similarity will be performed only taking in to account the specified groups)"
				
				+ "\n-format <format> (optional, RDF/XML by default, used for both input and output whenever it applies), "
				+ "either XML or JSON-LD, any other value will be dismissed and XML will be used"
				
				+ "\n-annotator <either cma or ncbo> (optional -ncbo by default), annotator"
				
				+ "\n-onlyTA (optional, true by default), if present, only title and abstract will be processed"
				
				+ "\n\nValid examples"
				+ "\nSimilarity for PubMed articles (-onlyTA) from URL"
				+ "\n-out <outDir> -compare <file with pairs pubmed_ID tab pubmed_ID> -annotator cma -onlyTA"
				+ "\nSimilarity for PMC articles from URL"
				+ "\n-out <outDir> -compare <file with pairs PMC_ID tab pmc_ID> -annotator cma"
				
				+ "\nSimilarity for PubMed articles (-onlyTA) from RDF annotation files"
				+ "\n-in <input dir> -out <output dir> -compare <file with pairs pubmed_ID tab pubmed_ID> -annotator cma -onlyTA"
				+ "\nSimilarity for PMB articles from RDF annotation files"
				+ "\n-in <input dir> -out <output dir> -compare <file with pairs pubmed_ID tab pubmed_ID> -annotator cma"
				
				+ "\nDistribution for PubMed articles (-onlyTA) from URL"
				+ "\n-out <outDir> -format XML -distribute <file with PMC ids> -annotator cma -model biolinks -onlyTA"
				+ "\nDistribution for PMC articles from URL"
				+ "\n-out <outDir> -format XML -distribute <file with PMC ids> -annotator cma -model biolinks"				
				
				+ "\nDistribution for PubMed articles (-onlyTA) from RDF annotation files"
				+ "\n-in <input dir> -out <outDir> -format XML -distribute <file with PMC ids> -annotator cma -model biolinks -onlyTA"
				+ "\nDistribution for PMC articles from RDF annotation files"
				+ "\n-in <input dir> -out <outDir> -format XML -distribute <file with PMC ids> -annotator cma -model biolinks"
				
				;
		if (args == null) {
			System.out.println(usage);
			System.exit(0);
		}
		//PropertyConfigurator.configure("log4j.properties");					
		
		int initPool = 10, maxPool = 10, keepAlive = 300;
		String inputDir = null, outputDir = null, idsFileLocation = null, ext = null, mode = null;
		List<String> groups = null;
		boolean onlyTA = false;
		Model model = Model.BIOLINKS;
		Annotator annotator = Annotator.NCBO;
		RDFFormat format = RDFFormat.RDFXML_ABBREV;
		
		for (int i = 0; i < args.length; i++) {
			String str = args[i];
			if (str.equalsIgnoreCase("-in")) {
				inputDir = args[++i];
			} else if (str.equalsIgnoreCase("-ext")) {
				ext = args[++i];
			} else if (str.equalsIgnoreCase("-out")) {
				outputDir = args[++i];
			} else if (str.equalsIgnoreCase("-compare") || str.equalsIgnoreCase("-distribute") || str.equalsIgnoreCase("-distAnnot")) {
				mode = str;
				idsFileLocation = args[++i];
			} else if (str.equalsIgnoreCase("-format")) {
				String fmt = args[++i];
				if (fmt.equalsIgnoreCase("JSON-LD")) {
					format = RDFFormat.JSONLD;
				}
			} else if (str.equalsIgnoreCase("-annotator")) {
				String annot = args[++i];
				try {
					annotator = Annotator.valueOf(annot.toUpperCase());
				} catch(IllegalArgumentException e) {
					annotator = Annotator.NCBO;
				}
			} else if (str.equalsIgnoreCase("-model")) {
				String temp = args[++i];
				try {
					model = Model.valueOf(temp.toUpperCase());
				} catch(IllegalArgumentException e) {
					model = Model.BIOLINKS;
				}
			} else if (str.equalsIgnoreCase("-groups")) {
				String temp = args[++i];
				try {
					groups = Arrays.asList(temp.split(","));
				} catch(IllegalArgumentException e) {
					groups = null;
				}
			} else if (str.equalsIgnoreCase("-onlyTA")) {
				onlyTA = true;
			} else if (str.equalsIgnoreCase("-initPool")) {
				initPool = Integer.parseInt(args[++i]);
			} else if (str.equalsIgnoreCase("-maxPool")) {
				maxPool = Integer.parseInt(args[++i]);
			} else if (str.equalsIgnoreCase("-keepAlive")) {
				keepAlive = Integer.parseInt(args[++i]);
			}
		}
		
		if ((idsFileLocation == null) || (outputDir == null)) {
			System.out.println(usage);
			System.exit(0);
		}

		System.out.println("Execution variables: " +
				"\nInput " + inputDir + "\nExtension: " + ext + "\nOutput " + outputDir +
				"\nDocuments to be compared, distributed by topics, or annotated-and-distributed by topics " + idsFileLocation + 
				"\nInput/Output Format " + format.getLang().getName() + "\nAnnotator " + annotator.name() + 
				"\nGrouping model " + model.name() + "\nGroups: " + groups +  	
				"\nOnly title and abstract " + onlyTA + 
				"\nInitPool " + initPool + " MaxPool " + maxPool + " KeepAlive " + keepAlive);
		
		BatchApplication app = new BatchApplication(initPool, maxPool, keepAlive);
		app.parseInput(inputDir, ext, outputDir, idsFileLocation, format, annotator, onlyTA, model, groups, mode);	
		app.shutDown();		
		while (!app.isTerminated()); //waiting
		long endTime = System.currentTimeMillis();
		System.out.println("\nTotal time: " + (endTime-startTime));
	}
	
	/**
	 * Parses the input parameters and place the output in the specified location.
	 * @param inputDir
	 * @param ext
	 * @param outputDir
	 * @param extension
	 * @param idsFileLocation
	 * @param format
	 * @param annotator
	 * @param onlyTA
	 * @param model
	 * @param mode
	 * @throws FileNotFoundException 
	 */
	public void parseInput(String inputDir, String ext, String outputDir, String idsFileLocation, RDFFormat format
			, Annotator annotator, boolean onlyTA, Model model, List<String> groups, String mode) {
		if (inputDir == null) {
			this.parseURL(outputDir, idsFileLocation, format, annotator, onlyTA, model, groups, mode);
		} else {
			this.parseDirectory(inputDir, ext, outputDir, idsFileLocation, format, annotator, onlyTA, model, groups, mode);
		}		
	}
	/**
	 * Parses a directory.
	 * @param inputDir
	 * @param outputDir
	 * @param extension
	 * @param format
	 * @param annotator
	 * @param onlyTA
	 * @throws FileNotFoundException 
	 */
	private void parseDirectory(final String inputDir, final String ext, final String outputDir, String idsFileLocation
			, final RDFFormat format, final Annotator annotator, final boolean onlyTA, final Model model, final List<String> groups
			, String mode) {
		final String dotExtension = format == RDFFormat.JSONLD ? ".json" : ".rdf";
		int count = 1;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(idsFileLocation));
			String line = "";
			int lineCounter = 1;
			while (line != null) {
				try {
					line = in.readLine();
					if (line == null) {
						break;
					}
					if (mode.equals("-compare")) {
						String[] ids = line.split("\t");
						final File baseInFile = new File(inputDir + "/" + ids[0] + dotExtension);
						final File otherInFile = new File(inputDir + "/" + ids[1] + dotExtension);
						this.runTask(new Runnable() {
			                public void run() {	
		                		SimilarityBatchController controller = new SimilarityBatchController();
								controller.comparesFromFile(baseInFile, otherInFile, outputDir, format, annotator, onlyTA
										, model.getName(), groups);
								controller = null;		                	
			                }
			            });
					} else if (mode.equals("-distribute")) {
						final File inFile = new File(inputDir + "/" + line + dotExtension);
						this.runTask(new Runnable() {
							public void run() {								
								SimilarityBatchController controller = new SimilarityBatchController();
								controller.distributesFromFile(inFile, outputDir, format, annotator, onlyTA, model.getName());
								controller = null;
							}
						});
					} else if (mode.equals("-distAnnot")) {
						final File inFile = new File(inputDir + "/" + line + "." + ext);
						this.runTask(new Runnable() {
							public void run() {
								SimilarityBatchController controller = new SimilarityBatchController();
								try {
									controller.distributesAndAnnotatesFromFile(inFile, outputDir, format, annotator, onlyTA, model.getName());
								} catch (ArticleParserException | UnsupportedFormatException e) {
									LOGGER.error(inFile.getName() + " could not be processed " + e);
								}
								controller = null;
							}
						});
					}						
				} catch (Exception e) {
					LOGGER.error("Line #" + lineCounter + "(" + line + ") could not be processed. Error was: " + e);
				} finally {
					lineCounter++;
				}
				if (count % 500 == 0) {
					System.gc();
				}
			}
			try {
				in.close();
			} catch (IOException e) {}
		} catch (FileNotFoundException e1) {
			LOGGER.error(idsFileLocation + " cannot be processed. " + e1);
		}
	}
	/**
	 * Parses a URL
	 * @param inputURL
	 * @param outputDir
	 * @param idsFileLocation
	 * @param format
	 * @param annotator
	 * @param onlyTA
	 */
	private void parseURL(final String outputDir, String idsFileLocation, final RDFFormat format, final Annotator annotator
			, final boolean onlyTA, final Model model, final List<String> groups, String mode) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(idsFileLocation));
			int count = 1;
			String line = "";
			int lineCounter = 1;			
			while (line != null) {
				try {
					line = in.readLine();
					if (line == null) {
						break;
					}
					if (mode.equals("-compare")) {
						final String[] ids = line.split("\t");
						this.runTask(new Runnable() {
			                public void run() {	
		                		SimilarityBatchController controller = new SimilarityBatchController();
								try {
									controller.comparesFromURL(ids[0], ids[1], outputDir, format, annotator, onlyTA
											, model.getName(), groups);
								} catch (ArticleParserException e) {
									LOGGER.error(ids[0] + "-" + ids[1] + " could not be processed " + e);
								}
								controller = null;		                	
			                }
			            });
					} else if (mode.equals("-distribute")) {
						final String id = line;
						this.runTask(new Runnable() {
							public void run() {								
								SimilarityBatchController controller = new SimilarityBatchController();
								try {
									controller.distributesFromURL(id, outputDir, format, annotator, onlyTA, model.getName());
								} catch (ArticleParserException e) {
									LOGGER.error(id + " could not be processed " + e);
								}
								controller = null;
							}
						});
					} else if (mode.equals("-distAnnot")) {
						final String id = line;
						this.runTask(new Runnable() {
							public void run() {
								SimilarityBatchController controller = new SimilarityBatchController();
								try {
									controller.distributesAndAnnotatesFromURL(id, outputDir, format, annotator, onlyTA, model.getName());
								} catch (ArticleParserException | UnsupportedFormatException e) {
									LOGGER.error(id + " could not be processed " + e);
								}
								controller = null;
							}
						});
					}						
				} catch (Exception e) {
					LOGGER.error("Line #" + lineCounter + "(" + line + ") could not be processed. Error was: " + e);
				} finally {
					lineCounter++;
				}
				if (count % 500 == 0) {
					System.gc();
				}
			}
			try {
				in.close();
			} catch (Exception e) {}
		} catch (FileNotFoundException e1) {
			LOGGER.error(idsFileLocation + " cannot be processed. " + e1);
		} 		
	}

}
