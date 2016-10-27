Inventor Disambiguation
========================

This project is for the coreference of patent inventor names used in the PatentsView database. This was the best performing system in the 2015 Inventor Disambiguation Workshop. The method performs clustering using a fast hierarchical method. Due to variety of data and the algorithm's sensitivity to such changes, the model feature template parameters may need to be tuned for new data sets. To tune these parameters, if you have a development set, please consider using grid searches over candidate values that you believe to be reasonable. Also, I recommend picking cases from your development set to inspect by hand to understand system performance and looking at an error analysis of the results is recommended. If you have questions or would like to discuss this more, please contact Nicholas Monath (first dot last at gmail dot com).


## Overview ##

Build:

```bash
cd inventor-disambiguation
mvn clean package
```

Run:

```bash
# Load the data
sh scripts/db/populate_mongo_db.sh
# Preprocess, create records to disambiguate
sh scripts/db/generate_inventor_mentions.sh
# Compute blocking/canopy assignments
sh scripts/coref/generate_coref_tasks.sh
# Run disambiguation
time ./scripts/coref/run_disambiguation.sh config/coref/RunConfig.config config/coref/WeightsCommonCharacteristics.config
# Post-process clean up inventor names
./scripts/process/post-process-remove-stopwords.sh data/multi-canopy-output/all-results.txt data/multi-canopy-output/all-results.txt.post-processed
```

Please email me Nicholas Monath (first dot last at gmail dot com) if you'd like the embedding file used in these experiments.

## Building the Code ##

Build using maven:

```bash
mvn clean package
```

This will create the jar file used to run the code:

```bash
target/inventor_disambiguation-1.0-SNAPSHOT-jar-with-dependencies.jar
```
The jar file includes both the inventor disambiguation project and all of its dependencies. The software can be run using this jar.

### Using as a Dependency of Another Project ###

To write software that uses this project as a dependency, use Maven to “install” the project:

```bash
mvn install
```
Add the following to the pom.xml in your project:

```xml
<dependencies>
...
        <dependency>
                <groupId>edu.umass.cs.iesl.inventor_disambiguation</groupId>
                <artifactId>inventor_disambiguation</artifactId>
                <version>1.0-SNAPSHOT</version>
        </dependency>
...
</dependencies>
```

## Usage ##

There are several stages of running the disambiguation algorithm: 

1. Load data into Mongo
2. Create “InventorMentions” (the records to disambiguate)
3. Create “CorefTasks” (a way to parallelize the processing)
4. Run disambiguation.
5. Post process inventor names

The following section provides a step-by-step guide to running the disambiguation system. It provides steps for running the entire processing pipeline from starting with the raw csv and tsv files and ending with the submitted results.

### Loading and Preprocessing the Patent Data ###

#### Downloading the raw data ####

Download the following PatentsView data files and place the following data files in the ```data``` directory:

```
cpc_current.csv
ipcr.csv
nber.csv
patent.csv
rawassignee.csv
rawinventor.csv
rawlawyer.csv
rawlocation.csv
uspc_current.csv
```

#### Starting the MongoDB Server ####

The following script is used to start the Mongo server on the AWS:

```bash
sh scripts/db/start_mongo_server_aws.sh
```

It places the server’s data in: ```data/mongodb/patentsview data/```

The port used is 27972, if this port is not available, please choose a different port and update all of the config files in the config directory with your new port number.

#### Loading the Data ####

The data is loaded into MongoDB using parallelization. Run the following script to load all of the data into mongo:

```bash
sh scripts/db/populate_mongo_db.sh
```

This step took about 37.42 minutes.

__More Details__ Each of the data types (inventors, patents, assignees, etc) has an associated data structure implementation in this project in the data structures package. Each of the data structures is serialized and stored in a MongoDB. This step loads each of the files, parsing them and doing some basic pre-processing (trimming, removing quotes, etc) and places the records in mongo. This is done using the PopulateGeneralPatentDB class for all data types exception Location, which uses a similar but more specific loader. The call to the Scala class is, for example:
where the config file CreateAssignee.config is:

```bash￼
java -Xmx20G -cp $jarpath \
edu.umass.cs.iesl.inventor_disambiguation.db.PopulateGeneralPatentDB \
--config=config/db/CreateAssignee.config --num-threads=20
--data-type=Assignee
--data-file=data/rawassignee.csv
--buffered-size=1000
--hostname=localhost
--port=27972
--dbname=patentsview_data
--collection-name=assignee
--codec=ISO-8859-1
```

This program uses command line arguments, using factorie’s command line argument parsing tool, as do most of the programs in this project. The arguments can be passed entirely in the command line or in a single file that is the input to the --config argument.

#### Creating Inventor Mentions ####

The input to our algorithm is a combination of data related to each row in the raw inventor table. We refer to this combination of related data an inventor mention. The inventor mentions in our database collect the patent, patent classification, co-inventor, assignee, lawyer, etc needed for disambiguation. This step also uses multiple threads. To generate the inventor mentions:

```bash
sh scripts/db/generate_inventor_mentions.sh
```
This step took about 66.97 minutes.


More details The shell script calls the Scala class: CreateInventorMentionDB. The input param- eters to this class are command line arguments:

```
--hostname=localhost
--port=27972
--dbname=patentsview_data
--collection-name=inventormentions
--buffered-size=1000
--application-collection-name=application
--assignee-collection-name=assignee
--cpc-collection-name=cpc
--inventor-collection-name=inventor
--ipcr-collection-name=ipcr
--lawyer-collection-name=lawyer
--location-collection-name=location
--nber-collection-name=nber
--patent-collection-name=patent
--us-patent-citation-collection-name=uspatentcitation
--uspc-collection-name=uspc
--inventor-file=data/rawinventor.csv
--num-threads=20
```

#### Creating the Coreference Task File ####

The coreference algorithm also takes a partitioning of the data into blocks or canopies. This allows us to easily parallelize the algorithm. To generate this input file run the following script:

```bash
sh scripts/coref/generate_coref_tasks.sh
```
The output of this script is a file, coref-tasks.tsv in the data directory. This step took about 2.95 minutes.
The output file is a two column tab separated file sorted by the number of mentions in the canopy:

```
LAST_jahns_FIRST_ekk 5889111-1,5596051-0,8399579-2,8304075-1,7316994-11,6683126-2
LAST_fleischmann_FIRST_tho      6780466-2,7880655-1,6499606-2,8108167-0
LAST_xue_FIRST_erz      6515146-6,5989457-3
LAST_nagone_FIRST_yui   6977872-0
```

#### Running Disambiguation ####

The command to run the disambiguation with the parameter setting is: 

```
time ./scripts/coref/run_disambiguation.sh config/coref/RunConfig.config config/coref/WeightsCommonCharacteristics.config
```

The results of this script will be stored in 

```
data/multi-canopy-output/all-results.txt
```

The running time was about 2 hours.

__More details__ The ```run_disambiguation.sh``` script contains a call to this scala class:


```bash
java -Xmx40G -cp $jarpath edu.umass.cs.iesl.inventor_disambiguation.coreference.RunParallelMultiCanopyCoreference $(cat $config_file) $(cat $weights_file)
```

The config file contains the following input settings:

```
# Coref tasks
--coref-task-file=data/coref-tasks.tsv 
# Output dir 
--output-dir=data/multi-canopy-output/ 
# Num threads
--num-threads=18
# Database
--hostname=localhost
--port=27972 
--dbname=patentsview_data 
--collection-name=inventormentions 
# Embeddings 
--embedding-file=embeddings.txt
```

and the weights file contains the feature template weights.

#### Post-processing of Inventor Names ####

There is also a post-processing script that cleans up inventor names removing words such as “deceased”. To run this script:

```bash
./scripts/process/post-process-remove-stopwords.sh <input-file> <output-file>
# For example
./scripts/process/post-process-remove-stopwords.sh \
    data/multi-canopy-output/all-results.txt \
    data/multi-canopy-output/all-results.txt.post-processed
```

#### JSON Input Format ###

```InventorMention``` objects can be serialized to json using ```PatentJsonSerialization.toJsonString```. Example usage is in ```CreateInventorMentionJSON```. Serialized JSON records can be loaded using ```LoadJSONInventorMentions```. The JSON Structure of the records exactly follows the slots in each cubbie.

Coreference can be applied to a file of json records using the following script:

```bash
sh ./scripts/coref/run_disambiguation_in_memory.sh json-file [config] [weights]
```

where the config and weight files are the same format as described for the ```run_disambiguation.sh``` script. The json-file argument should have one json record per line.

The JSON format can be viewed [here](src/main/scala/edu/umass/cs/iesl/inventor_disambiguation/data_structures/README.md)

Coreference tasks can be generated using the ```GenerateCorefTaskFileJSON``` object called by shell script ```scripts/coref/generate_coref_tasks_json.sh```.

### Implementation Overview ###

The provided patent data lends it self naturally to defining a data structure for each of the provided types of data. The package edu.umass.cs.iesl.inventor disambiguation.data structures contains the data structure implementation. Examples of data structures include: Inventors, Assignees, Locations, etc.

Nearly all of the data structures defined here have a similar structure. Nearly all of them extend the class Cubbie in the toolkit factorie to allow for easy serializability. The fields of these classes are defined using Slots. The Cubbie class allows access to these slots just like any other field. The data type of the slot defines the data type of the field. List valued slots use generics to handle the type definitions. The slots may have the type of another Cubbie object. This project has an extension of Cubbies called PatentsViewRecords which have the ability to print an easily viewable html formatting of the contents of an object to help debugging. The ```PatentsViewRecord``` maintains the ```patentID``` slot:

```Scala
class PatentsViewRecord extends Cubbie{
  val patentID = new StringSlot("patentID")
  ...
}
```

As an example, here is a snippet of the ```Patent``` class:

```Scala
class Patent extends PatentsViewRecord {
  val patentType = StringSlot("patentType")
  val number = StringSlot("number")
  val country = StringSlot("country")
  val date = StringSlot("date")
  val patentAbstract = StringSlot("patentAbstract")
  val title = StringSlot("title")
  val kind = StringSlot("kind")
  val numClaims = StringSlot("numClaims")
  val filename = StringSlot("filename")
  ...
}
```

The input data structure to the coreference algorithms is the ```InventorMention```. This is a variable that keeps track of all of the information of a particular mention that is needed to perform the disambiguation. This class is defined as:

```Scala
class InventorMention extends PatentsViewRecord{
// The mention id, e.g. patentID-SequenceNo
val uuid = new StringSlot("uuid")
val self = new CubbieSlot[Inventor]("self",() => new Inventor())
val patent = new CubbieSlot[Patent]("patent", () => new Patent())
val coInventors = new CubbieListSlot[Inventor]("coInventors", () => new Inventor())
  // Other data about the patent itself
  val assignees = new CubbieListSlot[Assignee]("assignees", () => new Assignee())
  val claims = new CubbieListSlot[Claim]("claims", () => new Claim())
  val lawyers = new CubbieListSlot[Lawyer]("lawyers", () => new Lawyer())
  // Classifications
  val cpc = new CubbieListSlot[CPC]("cpc", () => new CPC())
  val ipcr = new CubbieListSlot[IPCR]("ipcr", () => new IPCR())
  val nber = new CubbieListSlot[NBER]("nber", () => new NBER())
  val uspc = new CubbieListSlot[USPC]("uspc", () => new USPC())
  ....
}
```

The data structures representing the factor graph used in the algorithm are mostly implemented in factorie. There are representations for ```Mentions``` and ```Nodes``` in the tree structure. Each have a type parameter defining the variables used in the feature templates to compute the model score. The type parameter is defined as InventorVars. The ```InventorVars``` class contains the Bag of Words variables that are used by the feature templates described in the methods write up. Additional Bag of Words variables can simply be added as additional fields to the InventorVars class to perform future feature engineering.


#### Back-end database ####

For convenience, MongoDB was used as the back-end database. In the future SQL or another database could be used instead. MongoDB was convenient for several reasons; particularly since our data struc- tures could be directly read and written to Mongo. The Mongo server instance can sit on the same machine as the code running the coreference code. The Mongo database hosts all of the data of the raw files that is used to create the inventor mentions. The database also hosts the inventor mentions that are used in coreference.

Each of the raw files, (rawinventor.csv, patent.csv, cpc current.csv) is converted into a Mongo Col- lection with elements of the appropriate data type. The collections with patent IDs are indexed by patent ID and the Location collection is indexed by the location ID. From these collections, we cre- ate a collection of InventorMentions. The mentions are stored indexed by their ID number. The InventorMentions collection is queried by the worker threads of the coreference algorithm in parallel for the records.

Note that the code does not specifically rely on a Mongo interface. All interaction with MongoDB is implemented through an interface that makes no assumptions about the underlying storage mechanism. This interface is simply:

```Scala
trait Datastore[Key, Value] {
  def get(key: Key): Iterable[Value]
}
```

#### Coreference Implementation ####

The grid search used to tune the weights of the feature templates is currently implemented as a simple shell script that runs a number of parameter configurations and scores the results. In the future, this could easily be converted into a more sophisticated process implemented in Scala with the rest of the code.
The underlying factor graph and inference procedure are implemented using the generic code for these procedures in factorie. Of interest are the classes in the hcoref package and the ```TemplateModel``` and the ```SettingsSampler```. The specifics of the inference procedure, i.e the move generation, can be found in InventorCorefMoveGenerator, a modification of the implementation in factorie. The feature templates are implemented as ```SizeLimitingEntityNameTemplate```, ```BagOfWordsEntropy```, ```EntitySizePrior```, ```ChildParentCosineDistance``` and ```DenseCosineDistance```. The model is the class ```InventorCorefModel```. Additional templates can be added to model with a simple ```+=``` call, i.e. ```model += template```.

The classes which handle execution of the algorithms on a set of mentions are implemented with several traits and abstract classes. Most of these traits are wrappers to provide easy generalization and parallelization. There are two main kinds of wrappers, those which work at the level of InventorMentions the tuples that are stored in our database, the starting point for disambiguation, and those which work on ```Node[InventorVar]``` and ```Mention[InventorVar]``` objects, the node structures in the actual coreference tree.

The base trait for classes that work on InventorMentions is CoreferenceAlgorithm. It is defined as:

```Scala
trait CoreferenceAlgorithm[MentionType <: PatentsViewRecord] {
  val name = this.getClass.conventionalName
  def mentions: Iterable[MentionType]
  def run(): Unit
  def runPar(numThreads: Int): Unit
  def predictedClustering: Iterable[(String,String)]
}
```

The implementation of this that is used in disambiguation is ```MultiCanopyHierarchicalInventorCorefRunner```. Also of interest is the parent class ```HierarchicalInventorCorefRunner```. These classes convert the ```InventorMentions``` into ```InventorVars``` and run the algorithm with a ```HierarchicalCorefSystem```.

The ```HierarchicalCorefSystem``` is the base trait for classes that run the Metropolis Hastings based hierarchical system. The class ```MultiCanopy``` extends this trait and is used in this project.

For efficiency, the coreference work is divided into blocks or canopies and the canopies are processed in parallel. The mentions in each canopy are queried from the database at the time that they are to be processed. The trait ```ParallelCoreference``` handles distributing the tasks to multiple threads.

The code used to run the disambiguation on the entire patent database reveals how to use the coreference package:

```Scala
object RunParallelMultiCanopyCoreference {
  def main(args: Array[String]): Unit = {
    // Uses command line options from factorie
    val opts = new RunParallelOpts
    opts.parse(args)
    // Load all of the coref tasks into memory, so they can easily be distributed amongst the different threads
    val allWork = LoadCorefTasks.load(new File(opts.corefTaskFile.value),opts.codec.value).toIterable
    // Create the interface to the MongoDB containing the inventor mentions
    val db = new InventorMentionDB(opts.hostname.value, opts.port.value, opts.dbname.value, opts.collectionName.value, false)
    // The object that can query the database by the inventor-ids (i.e.patentNumber-sequenceNumber)
    val ds = db.toDatastoreByUUID
    // The lookup table containing the embeddings.
    val keystore = InMemoryKeystore.fromFile(new File(opts.keystorePath.value),
     opts.keystoreDim.value, opts.keystoreDelim.value,opts.codec.value)
    // Create the output directory
    new File(opts.outputDir.value).mkdirs()
    // Initialize the coreference algorithm
    val parCoref = new MultiCanopyParallelHierarchicalCoref(allWork, ds, opts, keystore, new File(opts.outputDir.value))
    // Run the algorithm on all the tasks
    parCoref.runInParallel(opts.numThreads.value)
    // Write the timing info
    val timesPW = new PrintWriter(new File(opts.outputDir.value,"timing.txt"))
    timesPW.println(parCoref.times.map(f => f._1 + "\t" + f._2).mkString(" "))
    timesPW.close()
    // display the timing info
    parCoref.printTimes()
  }
}
```

The ```CorefTask``` objects correspond to the blocks/canopies. These contain the IDs of the inventor mentions in a particular canopy. To parallelize, we simply have a thread pool that operates on the collection of ```CorefTasks```. The operation performed on each task is very simple and implemented independently of the coreference algorithm and the backend database:


```Scala
def handleTask(task: CorefTask): Unit = {
 // Create the output writer
  val wrtr = writer
  // Get the mentions of the task
  val taskWithMentions = getMentions(task)
  // Get an algorithm to process the task
  val alg = algorithmFromTask(taskWithMentions) 
  // perform the coreference procedure 
  runCoref(alg,getMentions(task))
  // write the output
  wrtr.write(task, alg.predictedClustering)
}
```

## Contact ##

Questions, bugs, suggestions -- please email Nicholas Monath (first dot last at gmail dot com)