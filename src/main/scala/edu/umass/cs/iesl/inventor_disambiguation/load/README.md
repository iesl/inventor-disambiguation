# Loading Data #


This package contains all of the raw comma/tab separated file loaders. Each of the classes extends ```DelimitedFileLoader```. Each loader has the following basic functionality:
 
```Scala
def load(file: File, codec: String = "UTF-8"): Iterator[T] 
def load(file: File, codec: String, start: Int): Iterator[T]
def load(file: File, codec: String, start: Int, end: Int) : Iterator[T] 
def loadMultiple(file: File, codec: String, num: Int): Iterable[Iterator[T]] 
```

The ```loadMultiple``` method is useful for multithreading.

To get the loader corresponding to a particular data type, you can use the method

```Scala
Loaders.apply(name)
```

where ```name``` is the String name of the data structure of interest.