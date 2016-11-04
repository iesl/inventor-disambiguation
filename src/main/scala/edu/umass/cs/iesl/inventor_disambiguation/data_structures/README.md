# Data Structures #


Most of the data structures defined here have a similar structure. Nearly all of them extend the class [Cubbie](https://github.com/factorie/factorie/blob/master/src/main/scala/cc/factorie/util/Cubbie.scala) in the toolkit [factorie](https://github.com/factorie/) to allow easy serializability (especially to MongoDB). The fields of these class are defined using ```Slot```s. The ```Cubbie``` class allows access to these slots just like any other field. This project has an extension of ```Cubbie```s called ```PatentsViewRecord```s which have the ability to print an easily viewably html formatting of the contents of an object to help debugging. As an example, here is a snippet of the ```Location``` class:


```Scala
class Location extends PatentsViewRecord {
  val locationID = StringSlot("locationID")
  val city = StringSlot("city")
  val state = StringSlot("state")
  val country = StringSlot("country")
  }
```  

Most of the the data structures are defined analogously to their corresponding raw data tables. 

One data structure to note is the ```InventorMention``` data structure, which is used in the disambiguation algorithm. It is defined as:

```Scala
class InventorMention extends PatentsViewRecord{
  val uuid = new StringSlot("uuid")
  val self = new CubbieSlot[Inventor]("self",() => new Inventor())
  val patent = new CubbieSlot[Patent]("patent", () => new Patent())
  val coInventors = new CubbieListSlot[Inventor]("coInventors", () => new Inventor())
  val applications = new CubbieListSlot[Application]("applications", () => new Application())
  val assignees = new CubbieListSlot[Assignee]("assignees", () => new Assignee())
  val claims = new CubbieListSlot[Claim]("claims", () => new Claim())
  val lawyers = new CubbieListSlot[Lawyer]("lawyers", () => new Lawyer())
  val cpc = new CubbieListSlot[CPC]("cpc", () => new CPC())
  val ipcr = new CubbieListSlot[IPCR]("ipcr", () => new IPCR())
  val nber = new CubbieListSlot[NBER]("nber", () => new NBER())
  val uspc = new CubbieListSlot[USPC]("uspc", () => new USPC())
  val usPatentCitations = new CubbieListSlot[USPatentCitation]("uspatentcitations", () => new USPatentCitation())
}
```

This class' various fields are used to disambiguate the inventors.


The JSON structure of ```InventorMention``` is:


```
{
    "assignees": [
        {
            "assigneeType": String,
            "organization": String,
            "patentID": String,
            "rawLocationID": String,
            "sequence": String,
            "uuid": String
        },
        {
            "assigneeType": String,
            "patentID": String,
            "rawLocationID": String,
            "sequence": String,
            "nameFirst": String,
            "nameLast": String,
            "uuid": String
        }
    ],
    "coInventors": [
        {
            "inventorID": String,
            "location": {
                "city": String,
                "country": String,
                "locationID": String,
            },
            "nameFirst": String,
            "nameLast": String,
            "nameMiddle": List[String],
            "patentID": String,
            "rawLocationID": String,
            "sequence": String,
            "uuid": String
        },
        {
            "inventorID": String,
            "location": {
                "city": String,
                "country": String,
                "locationID": String,
            },
            "nameFirst": String,
            "nameLast": String,
            "nameMiddle": List[String],
            "patentID": String,
            "rawLocationID": String,
            "sequence": String,
            "uuid": String
        }
    ],
    "cpc": [
        {
            "category": String,
            "groupID": String,
            "patentID": String,
            "sectionID": String,
            "sequence": String,
            "subgroupID": String,
            "subsectionID": String,
            "uuid": String,
        },
        {
            "category": String,
            "groupID": String,
            "patentID": String,
            "sectionID": String,
            "sequence": String,
            "subgroupID": String,
            "subsectionID": String,
            "uuid": String,
        }
    ],
    "ipcr": [
        {
            "actionDate":  String,
            "classificationDataSource":  String,
            "classificationLevel":  String,
            "classificationStatus":  String,
            "classificationValue":  String,
            "ipcClass":  String,
            "ipcVersionIndicator":  String,
            "mainGroup":  String,
            "patentID":  String,
            "section":  String,
            "sequence":  String,
            "subclass":  String,
            "subgroup":  String,
            "symbolPosition":  String,
            "uuid":  String,
        },
        {
            "actionDate":  String,
            "classificationDataSource":  String,
            "classificationLevel":  String,
            "classificationStatus":  String,
            "classificationValue":  String,
            "ipcClass":  String,
            "ipcVersionIndicator":  String,
            "mainGroup":  String,
            "patentID":  String,
            "section":  String,
            "sequence":  String,
            "subclass":  String,
            "subgroup":  String,
            "symbolPosition":  String,
            "uuid":  String,
        }
    ],
    "lawyers": [
        {
            "country":  String,
            "nameFirst":  String,
            "nameLast":  String,
            "patentID":  String,
            "sequence":  String,
            "uuid":  String,
        }
    ],
    "nber": [
        {
            "categoryID":  String,
            "patentID":  String,
            "subcategoryID":  String,
            "uuid":  String,
        }
    ],
    "patent": {
        "country":  String,
        "date":  String,
        "filename":  String,
        "kind":  String,
        "numClaims":  String,
        "number":  String,
        "patentAbstract":  String,
        "patentID":  String,
        "patentType":  String,
        "title":  String,
    },
    "rawName": {
        "nameFirst":  String,
        "nameLast":  String,
        "nameMiddles": List[String]
        "nameSuffixes": []
    },
    "self": {
        "inventorID":  String,
        "location": {
            "city":  String,
            "state": String,
            "country":  String,
            "locationID":  String,
        },
        "nameFirst":  String,
        "nameLast":  String,
        "nameMiddles": List[String],
        "nameSuffixes": List[String],
        "patentID":  String,
        "rawLocationID":  String,
        "sequence":  String,
        "uuid":  String,
    },
    "uspc": [
        {
            "mainClassID":  String,
            "patentID":  String,
            "subclassID":  String,
            "uuid":  String,
        },
        {
            "mainClassID":  String,
            "patentID":  String,
            "subclassID":  String,
            "uuid":  String,
        },
        {
            "mainClassID":  String,
            "patentID":  String,
            "subclassID":  String,
            "uuid":  String,
        }
    ],
    "uuid":  String,
}
```

## Disambiguation Features ##

The features used for disambiguation are (see: [source](../coreference/HierarchicalInventorCoref.scala#L125-L206)):

```
{
    "uuid":  String, 
    "assignees": [
        {
            "organization": String,
        },
        {
            "nameFirst": String,
            "nameLast": String
        }
    ],
    "coInventors": [
        {
            "nameFirst": String,
            "nameLast": String,
        },
    ],
    "cpc": [
        {
            "sectionID": String,
            "subsectionID": String,
        },
    ],
    "ipcr": [
        {
            "classificationLevel":  String,
            "ipcClass":  String,
            "section":  String,
        },
    ],
    "lawyers": [
        {
            "nameFirst":  String,
            "nameLast":  String,
        }
    ],
    "nber": [
        {
            "categoryID":  String,
            "subcategoryID":  String,
        }
    ],
    "patent": {
        "title":  String,
    },
    "self": {
        "inventorID":  String,
        "location": {
            "city":  String,
            "state": String,
            "country":  String,
        },
        "nameFirst":  String,
        "nameLast":  String,
        "nameMiddles": List[String],
        "nameSuffixes": List[String],
        "patentID":  String,
        "sequence":  String,
        "uuid":  String,
    },
    "uspc": [
        {
            "mainClassID":  String,
            "subclassID":  String,
        },
    ],
    
}
```
Note that the ```uuid``` at the top level is the unique id for the ```InventorMention```. This is the value that appears in the coref-tasks (blocking) files. This field is PatentNo-InventorSequence where PatentNo is the id number of the patent and InventorSequence is the order in which the inventor is listed on the patent. 