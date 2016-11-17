# biotea-biolinks
Semantic similarity and topic distribution for articles annotated with biotea-annotation (https://github.com/ljgarcia/biotea-annotation). The topic distribution is based on Biolinks UMLS group reclasification.

##In action
Please visit the [Biolinks project demo](http://ljgarcia.github.io/biotea-biolinks) for more information.

##Usage
Usage: -in <input dir> -out <output dir> -compare/-distribute/-distAnnot 

-in <input dir> (mandatory if input will be processed from a directory, 
SHOULD NOT be used if input will be process from annotator URL, this dir should contain all annotation files corresponding to ids specified in <compare> file) if -compare is used, otherwise it should contain all annotator files corresponding to ids in <distribute> if -distribute is used, otherwise it should contain all annotator files corresponding to ids in <distAnnot> if -distAnnot is use
				
-out <output dir> (mandatory)
				
-compare <document ids file> (one of -compare, -distribute, or -distAnnot is mandatory), it MUST provide a full path to a file with a list of document ids ready to be processed; if <input dir> is used, document ids MUSt correspond to an annotation file in <input dir> with .rdf extension, one pair of ids baseId \t otherId per line, i.e. documents to be compared)
				
-distribute <document ids file> (one of -compare, -distribute, or -distAnnot is mandatory), it MUSt provide a full path to a file with a list of document ids ready to be processed; if <input dir> is used, document ids MUST correspond to an annotator file in <input dir> with .<ext> extension
				
-distAnnot <document ids file> (one of -compare, -distribute, or -distAnnot is mandatory), it MUSt provide a full path to a file with a list of document ids ready to be processed; if <input dir> is used, document ids MUST correspond to an annotator file in <input dir> with .<ext> extension

-ext file extension expected in files in <input dir> (mandatory only if -distAnnot AND -in are used)
				
-model grouping model to be used, <either biolinks or umls> (mandatory if -distribute or -distAnnot is used, mandatory if -compare and -groups are used together, biolinks by default)	
				
-groups <list of valid groups according to the model separated by comma> (optional, to be used only in conjunction with -compare so the similarity will be performed only taking in to account the specified groups)
				
-format <format> (optional, RDF/XML by default, used for both input and output whenever it applies), 
either XML or JSON-LD, any other value will be dismissed and XML will be used
				
-annotator <either cma or ncbo> (optional -ncbo by default), annotator
				
-onlyTA (optional, true by default), if present, only title and abstract will be processed
				
##Valid examples

* Similarity for PubMed articles (-onlyTA) from URL

-out <outDir> -compare <file with pairs pubmed_ID tab pubmed_ID> -annotator cma –onlyTA

* Similarity for PMC articles from URL

-out <outDir> -compare <file with pairs PMC_ID tab pmc_ID> -annotator cma
				
* Similarity for PubMed articles (-onlyTA) from RDF annotation files

-in <input dir> -out <output dir> -compare <file with pairs pubmed_ID tab pubmed_ID> -annotator cma -onlyTA

* Similarity for PMB articles from RDF annotation files

-in <input dir> -out <output dir> -compare <file with pairs pubmed_ID tab pubmed_ID> -annotator cma
				
* Distribution for PubMed articles (-onlyTA) from URL

-out <outDir> -format XML -distribute <file with PMC ids> -annotator cma -model biolinks -onlyTA

* Distribution for PMC articles from URL

-out <outDir> -format XML -distribute <file with PMC ids> -annotator cma -model biolinks				
				
* Distribution for PubMed articles (-onlyTA) from RDF annotation files

-in <input dir> -out <outDir> -format XML -distribute <file with PMC ids> -annotator cma -model biolinks -onlyTA

* Distribution for PMC articles from RDF annotation files
-in <input dir> -out <outDir> -format XML -distribute <file with PMC ids> -annotator cma -model biolinks
