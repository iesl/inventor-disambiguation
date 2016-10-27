# Database ReadMe #


The data structures in this project are implemented using ```Cubbies``` a serialization format from [factorie](http://factorie.cs.umass.edu/). These structures are easily serializable to MongoDB and other formats. See the REAME file in the data_structures package for more information. 

The main interface to collections of data structures stored in a database backend is the ```Datastore``` trait: 

```Scala
trait Datastore[Key, Value] {
  def get(key: Key): Iterable[Value]
}
```

The trait defines a collection of (key, value) pairs, indexed on the key. All of the values of the pairs with the given key are returned with the get method. 

This trait is often implemented through a wrapper to a Mongo database. The interface to Mongo is through ```MongoDatastore```. This is implemented by classes such as ```GeneralPatentDB``` which can be used for any of the ```PatentsViewRecord``` objects, ```LocationDB``` the database of locations, and ```InventorMentionDB``` the database of the inventor mentions. 

