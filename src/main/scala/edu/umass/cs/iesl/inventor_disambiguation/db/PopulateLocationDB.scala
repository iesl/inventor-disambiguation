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

import edu.umass.cs.iesl.inventor_disambiguation.data_structures.Location
import edu.umass.cs.iesl.inventor_disambiguation.load.LoadLocation

/**
 * The database of locations. Locations, do not have a patent id, and so are indexed by their location id. 
 * @param hostname - the name of the machine running the server
 * @param port - the port on which the database is listening
 * @param dbname - the name of the database
 * @param collectionName - the name of the collection 
 * @param enforceIndices - whether or not to enforce the index
 */
class LocationDB(override val hostname: String, override val  port: Int, override val  dbname: String, override val  collectionName: String, override val enforceIndices: Boolean)  
  extends MongoDatastore[Location]
  with Datastore[String,Location] {

  /**
   * Specify the index to be the location ID 
   * @param cubbie
   * @return
   */
  override def indices(cubbie: Location): Seq[Seq[Location#AbstractSlot[Any]]] = Seq(Seq(cubbie.locationID))

  /**
   * Create an empty location 
   * @return
   */
  override def constructor(): Location = new Location()

  /**
   * Return the location given the key. 
   * @param key the key of interest
   * @return the values
   */
  override def get(key: String): Iterable[Location] = db.query(q => q.locationID.set(key)).limit(1).toIterable

}

/**
 * Populate the location database. Note use the GeneralPatentDB command line options for this executable.
 */
object PopulateLocationDB {


  def main(args: Array[String]): Unit = {
    val opts = new GeneralPatentDBOpts
    opts.parse(args)

    println(s"[PopulateLocationDB] Will be working on Location records")

    val loader = LoadLocation

    val db = new LocationDB(opts.hostname.value,opts.port.value,opts.dbname.value,opts.collectionName.value,false)

    val records = loader.loadMultiple(new File(opts.dataFile.value), opts.codec.value,opts.numThreads.value)
    records.par.foreach {
      recordSet =>
        println(s"[PopulateLocationDB] Adding Location records.")
        db.bufferedInsert(recordSet,opts.bufferSize.value)
    }
    println(s"[PopulateLocationDB] Creating index.")
    db.addIndices()
  }

}