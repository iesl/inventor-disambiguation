# Coreference Implementation Notes #


The implementation of the coreference systems is broken up into several traits and abstract classes. Most of these traits are wrappers to provide easy generalization and parallelization. There are two main kinds of wrappers, those which work at the level of ```InventorMentions``` the tuples that are stored in our database, the starting point for disambiguation, and those which work on ```Node[InventorVar]``` and ```Mention[InventorVar]``` objects, the node structures in the actual coreference tree. 
 
 The base trait for classes that work on ```InventorMentions``` is ```CoreferenceAlgorithm```. It is defined as:
 
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

The ```HierarchicalCorefSystem``` is the base trait for classes that run the MCMC based hierarchical system. The class ```MultiCanopy``` extends this trait and is used in this project. 


The parallelization of the coreference is handled in the trait ```ParallelCoreference```. This class distributes ```CorefTask``` objects, which are collections of mentions to multiple worker threads.
 
The disambiguation runs are executed using the ```RunParallelMultiCanopyCoreference``` program. This program takes command line arguments of the coref-task file, an output directory, model parameters, among others.



## Modifying Code to use SQL instead of Mongo ##

MongoDB is used to store the inventor mentions. However, this can be changed to SQL or any other database backend. 

The interface to the database backend is the ```Datastore``` trait.


```Scala
trait Datastore[Key, Value] {
  def get(key: Key): Iterable[Value]
}
```

The coreference algorithms take an object of this trait as a parameter that allows access to the inventor mentions by their inventor id (a string). The inventor ids are expected to be of the format patent number, hyphen, sequence number.

```Scala
class MultiCanopyParallelHierarchicalCoref(override val allWork: Iterable[CorefTask],
                                            override val datastore: Datastore[String, InventorMention],
                                            opts: InventorModelOptions,r
                                            keystore: Keystore,
                                            outputDir: File)
```

These datastore objects can be implemented with a connection to a SQL server (using JDBC or similar interfaces). 
 
Currently this is implemented with a connection to Mongo via the ```InventorMentionDB``` class.

