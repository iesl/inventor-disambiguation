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

import edu.umass.cs.iesl.inventor_disambiguation._
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.PatentsViewRecord
import edu.umass.cs.iesl.inventor_disambiguation.load.Loaders

/**
 * The interface to Mongo for the PatentsViewRecords. This class is type parameterized so that it can be used with
 * any subclass of PatentsViewRecord. It is abstract because a constructor must be given for the specific data type.
 * @param hostname the hostname of the machine running the mongo server
 * @param port  the port of the machine on which mongo is running
 * @param dbname the name of the mongo database
 * @param collectionName the name of the collection
 * @param enforceIndices whether or not the specified indices should be enforced
 * @tparam T the type parameter, the datatype of the record stored in the database
 */
abstract class GeneralPatentDB[T <: PatentsViewRecord] (override val hostname: String, override val  port: Int, override val  dbname: String, override val  collectionName: String, override val enforceIndices: Boolean)
  extends MongoDatastore[T] 
  with Datastore[String,T] {

  /**
   * The indices to maintain in the database. By default this is an index on the patentID field.
   * @param cubbie
   * @return
   */
  override def indices(cubbie: T): Seq[Seq[T#AbstractSlot[Any]]] = Seq(Seq(cubbie.patentID))

  /**
   * The maximum number of database tuples to return* 
   */
  val queryLimit = 10000

  /**
   * Return up to queryLimit items that have the given key 
   * @param key the key of interest
   * @return the values
   */
  override def get(key: String): Iterable[T] = {
    val res = db.query(q => q.patentID.set(key)).limit(queryLimit).toIterable
    if (res.size == queryLimit)
      println(s"[${this.getClass.conventionalName}] WARNING: Query limit has been met for $key")
    res
  }
}

/**
 * Command line options for using/populating a GeneralPatentDB 
 */
class GeneralPatentDBOpts extends MongoDBOpts {
  val dataType = new CmdOption[String]("data-type", "The type of data to load.", true)
  val dataFile = new CmdOption[String]("data-file", "The filepath to the csv/tsv file", true)
  val codec = new CmdOption[String]("codec", "UTF-8", "STRING", "The encoding of the input file")
  val bufferSize = new CmdOption[Int]("buffered-size", "The size of the buffer to use", true)
  val numThreads = new CmdOption[Int]("num-threads", 20, "INT", "The number of threads to use in the insert")
}


/**
 * Executable for populating a GeneralPatentDB from the raw csv/tsv data files. Use the 
 * GeneralPatentDB command line options to execute. 
 */
object PopulateGeneralPatentDB {


  def main(args: Array[String]): Unit = {
    val opts = new GeneralPatentDBOpts
    opts.parse(args)
    
    println(s"[PopulateGeneralPatentDB] Will be working on ${opts.dataType.value} records")
    
    // Find the loader with the given data type
    val loader = Loaders(opts.dataType.value)

    // Start up the DB instance
    val db = new GeneralPatentDB[PatentsViewRecord](opts.hostname.value,opts.port.value,opts.dbname.value,opts.collectionName.value,false) {
      override def constructor(): PatentsViewRecord = new PatentsViewRecord()
    }
    
    // Add the records in parallel to the database.
    val records = loader.loadMultiple(new File(opts.dataFile.value), opts.codec.value,opts.numThreads.value)
    records.par.foreach {
      recordSet =>
        println(s"[PopulateGeneralPatentDB] Adding ${opts.dataType.value}s records.")
        db.bufferedInsert(recordSet,opts.bufferSize.value)
    }
    println(s"[PopulateGeneralPatentDB] Creating index.")
    db.addIndices()
  }

}