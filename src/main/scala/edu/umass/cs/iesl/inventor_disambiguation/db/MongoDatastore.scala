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

import cc.factorie.db.mongo.{LazyCubbieConverter, MongoCubbieCollection}
import cc.factorie.util.Cubbie
import com.mongodb.{BasicDBObject, MongoClient}
import edu.umass.cs.iesl.inventor_disambiguation._

/**
 * Acts as a wrapper for all of the various components involved in connecting to a mongo database  
 * @tparam T - the type parameter T
 */
trait MongoDatastore[T <: Cubbie] {


  /**
   * The hostname of the machine running the server
   * @return
   */
  def hostname: String

  /**
   * The port of the machine running the server 
   * @return
   */
  def port: Int

  /**
   * The database name of the MongoDB 
   * @return
   */
  def dbname: String

  /**
   * The collection name for this particular group of records 
   * @return
   */
  def collectionName: String

  /**
   * Formatted string of the connection 
   */
  lazy val dbString = s"$dbname.$collectionName@$hostname:$port"

  /**
   * Create a new instance of T 
   * @return
   */
  def constructor(): T

  /**
   * The indices on the collection
   * @param cubbie
   * @return
   */
  def indices(cubbie: T): Seq[Seq[T#AbstractSlot[Any]]]

  /**
   * Checks if the indices are present and if not creates the indices 
   * @return
   */
  def enforceIndices: Boolean

  /**
   * The connection to the mongo client 
   */
  lazy val mongoConn = new MongoClient(hostname, port)

  /**
   * The mongo db
   */
  lazy val mongoDB = mongoConn.getDB(dbname)

  /**
   * The collection 
   */
  lazy val dbcollection = mongoDB.getCollection(collectionName)

  /**
   * The MongoCubbieCollection wrapper to the mongo connection 
   */
  lazy val db = if (enforceIndices)
    new MongoCubbieCollection(dbcollection, constructor, indices) with LazyCubbieConverter[T]
  else
    new MongoCubbieCollection(dbcollection, constructor, (t:T) => Seq[Seq[T#AbstractSlot[Any]]]()) with LazyCubbieConverter[T]


  /**
   * Adds the given index to the collection 
   * @param indices the indices to add
   */
  def addIndices(indices: Seq[Seq[T#AbstractSlot[Any]]]):Unit = indices.foreach {
    index =>
      val dbo = new BasicDBObject()
      index.foreach(k => dbo.put(k.name, 1))
      dbcollection.ensureIndex(dbo)
  }


  /**
   * Add the indices specified in the indices function
   */
  def addIndices():Unit = addIndices(indices(constructor()))

  /**
   * Whether or not to print messages while loading into the database
   */
  val printMessages: Boolean = true

  /**
   * Print a message every time printEvery cubbies are inserted 
   */
  val printEvery: Int = 20000

  /**
   * Insert the given cubbies in groups of bufferSize. A recommend size would be ~1000
   * @param cubbies  the cubbies to insert
   * @param bufferSize insert the cubbies in groups of this size
   */
  def bufferedInsert(cubbies: Iterator[T], bufferSize: Int) = {
    val className = this.getClass.conventionalName
    val start = System.currentTimeMillis()
    println(s"[$className] Adding cubbies to $dbString. Buffer size: $bufferSize.")
    var numInserted = 0
    println(s"[$className] Num Inserted: $numInserted.")
    cubbies.grouped(bufferSize).foreach {
      group =>
        db.++=(group)
        numInserted += group.size
        if (numInserted % printEvery == 0)
          println(s"[$className] Num Inserted: $numInserted.")
    }
    println(s"\n[$className] Finished adding cubbies to $dbString. Inserted $numInserted cubbies. Total time: ${System.currentTimeMillis() - start}")
  }

}