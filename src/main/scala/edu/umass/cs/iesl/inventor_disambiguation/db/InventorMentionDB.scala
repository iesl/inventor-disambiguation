/* Copyright (C) 2016 University of Massachusetts Amherst.
   This file is part of “inventor_disambiguation”

   This work was done for the USPTO inventor disambiguation workshop
   organized under the PatentsView initiative (www.patentsview.org).
   The algorithm was the best performing at the workshop according
   to the workshop judges' criteria of disambiguation performance,
   running time, and usability.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */


package edu.umass.cs.iesl.inventor_disambiguation.db

import java.io.File

import cc.factorie.util.Threading
import edu.umass.cs.iesl.inventor_disambiguation.data_structures._
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.classification.{CPC, IPCR, NBER, USPC}
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.coreference.InventorMention
import edu.umass.cs.iesl.inventor_disambiguation.load.LoadInventor

/**
 * The database of inventor mentions used for coreference.  
 * @param hostname the name of the machine running the mongo server
 * @param port the port of the machine running the mongo server
 * @param dbname the database name 
 * @param collectionName the collection name
 * @param enforceIndices whether or not to inforce the index (on the inventor ids)
 */
class InventorMentionDB(override val hostname: String,
                             override val  port: Int,
                             override val  dbname: String,
                             override val  collectionName: String,
                             override val enforceIndices: Boolean)  extends MongoDatastore[InventorMention] {

  /**
   * The constructor of an empty inventor mention 
   * @return
   */
  override def constructor(): InventorMention = new InventorMention()

  /**
   * The indices of the database. An index on the uuid of the inventor mention, which is the inventor id (the patent id, a hyphen, and the sequence id) 
   * @param cubbie
   * @return
   */
  override def indices(cubbie: InventorMention): Seq[Seq[InventorMention#AbstractSlot[Any]]] = Seq(Seq(cubbie.uuid))

  /**
   * One might have multiple indices on this table, for this reason, the class itself does not implement the datastore method
   * but instead allows the creation of a datastore. This datastore allows for the lookup of inventor mentions based on their
   * uuid. 
   * @return
   */
  def toDatastoreByUUID = new Datastore[String,InventorMention] {
    override def get(key: String): Iterable[InventorMention] = {
      db.query(q => q.uuid.set(key)).limit(1).toIterable
    }
  }
}

/**
 * Command line options for creating the inventor mention database 
 */
class CreateInventorMentionDBOpts extends MongoDBOpts {

  val applicationCollectionName = new CmdOption[String]("application-collection-name", "The filepath to the patents.csv file", false)
  val assigneeCollectionName = new CmdOption[String]("assignee-collection-name", "The filepath to the patents.csv file", true)
  val cpcCollectionName = new CmdOption[String]("cpc-collection-name", "The filepath to the patents.csv file", true)
  val inventorCollectionName = new CmdOption[String]("inventor-collection-name", "The filepath to the patents.csv file", true)
  val ipcrCollectionName = new CmdOption[String]("ipcr-collection-name", "The filepath to the patents.csv file", true)
  val lawyerCollectionName = new CmdOption[String]("lawyer-collection-name", "The filepath to the patents.csv file", true)
  val locationCollectionName = new CmdOption[String]("location-collection-name", "The filepath to the patents.csv file", true)
  val nberCollectionName = new CmdOption[String]("nber-collection-name", "The filepath to the patents.csv file", true)
  val patentCollectionName = new CmdOption[String]("patent-collection-name", "The filepath to the patents.csv file", true)
  val usPatentCitationCollectionName = new CmdOption[String]("us-patent-citation-collection-name", "The filepath to the patents.csv file", false)
  val uspcCollectionName = new CmdOption[String]("uspc-collection-name", "The filepath to the patents.csv file", true)
  val bufferSize = new CmdOption[Int]("buffered-size", "The size of the buffer to use", true)
  val inventorFile = new CmdOption[String]("inventor-file", "The filepath to the rawinventor.csv file", true)
  val codec = new CmdOption[String]("codec", "UTF-8", "STRING", "The encoding of the file")
  val numThreads = new CmdOption[Int]("num-threads", 20, "INT", "NumThreads")
}


/**
 * Executable for generating the inventor mention table. Uses the CreateInventorMentionDBOpts command line options
 */
object CreateInventorMentionDB {

  def insert(mentions: Iterator[InventorMention], db: InventorMentionDB, bufferSize: Int) =
    insertPar(Iterable(mentions),db,bufferSize)

  def insertPar(mentionStreams: Iterable[Iterator[InventorMention]], db: InventorMentionDB, bufferSize: Int): Unit =
    insertPar(mentionStreams.size,mentionStreams,db, bufferSize)

  def insertPar(numThreads: Int, mentionStreams: Iterable[Iterator[InventorMention]], db: InventorMentionDB, bufferSize: Int):Unit =
    Threading.parForeach(mentionStreams,numThreads)(stream => db.bufferedInsert(stream,bufferSize))



  def main(args: Array[String]): Unit = {

    val opts = new CreateInventorMentionDBOpts
    opts.parse(args)

    val assigneeDB = new GeneralPatentDB[Assignee](opts.hostname.value, opts.port.value, opts.dbname.value, opts.assigneeCollectionName.value, true) {
      override def constructor(): Assignee = new Assignee()
    }

    val cpcDB = new GeneralPatentDB[CPC](opts.hostname.value, opts.port.value, opts.dbname.value, opts.cpcCollectionName.value, true) {
      override def constructor(): CPC = new CPC()
    }
    
    val inventorDB = new GeneralPatentDB[Inventor](opts.hostname.value, opts.port.value, opts.dbname.value, opts.inventorCollectionName.value, true) {
      override def constructor(): Inventor = new Inventor()
    }
    
    val ipcrDB = new GeneralPatentDB[IPCR](opts.hostname.value, opts.port.value, opts.dbname.value, opts.ipcrCollectionName.value, true) {
      override def constructor(): IPCR = new IPCR()
    }
    
    val lawyerDB = new GeneralPatentDB[Lawyer](opts.hostname.value, opts.port.value, opts.dbname.value, opts.lawyerCollectionName.value, true) {
      override def constructor(): Lawyer = new Lawyer()
    }

    val locationDB = new LocationDB(opts.hostname.value, opts.port.value, opts.dbname.value, opts.locationCollectionName.value, true)

    val nberDB = new GeneralPatentDB[NBER](opts.hostname.value, opts.port.value, opts.dbname.value, opts.nberCollectionName.value, true) {
      override def constructor(): NBER = new NBER()
    }

    val patentDB = new GeneralPatentDB[Patent](opts.hostname.value, opts.port.value, opts.dbname.value, opts.patentCollectionName.value, true) {
      override def constructor(): Patent = new Patent()
    }

    val uspcDB = new GeneralPatentDB[USPC](opts.hostname.value, opts.port.value, opts.dbname.value, opts.uspcCollectionName.value, true) {
      override def constructor(): USPC = new USPC()
    }

    val inventorsPar = LoadInventor.loadMultiple(new File(opts.inventorFile.value),opts.codec.value,opts.numThreads.value)
    val db = new InventorMentionDB(opts.hostname.value, opts.port.value, opts.dbname.value, opts.collectionName.value, false)
    
    inventorsPar.par.foreach(
     inventors => {
       val inventorMentions = inventors.map(inventor => InventorMention.fromDatastores(inventor, assigneeDB, cpcDB, inventorDB, ipcrDB, lawyerDB, locationDB, nberDB, patentDB, uspcDB))
       db.bufferedInsert(inventorMentions, opts.bufferSize.value)
     }
    )
    db.addIndices()
  }

}