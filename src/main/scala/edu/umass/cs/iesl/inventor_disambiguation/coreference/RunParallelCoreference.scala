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


package edu.umass.cs.iesl.inventor_disambiguation.coreference

import java.io.{File, PrintWriter}

import cc.factorie.app.bib.hcoref.InMemoryHashMapKeystoreOpts
import edu.umass.cs.iesl.inventor_disambiguation.db.{InMemoryPrimaryKeyDatastore, InventorMentionDB, MongoDBOpts}
import edu.umass.cs.iesl.inventor_disambiguation.load.{LoadCorefTasks, LoadJSONInventorMentions}

/**
 * The command line opts for running the parallel coreference task 
 */
trait RunParallelOpts extends MongoDBOpts with InventorModelOptions with InMemoryHashMapKeystoreOpts {
  val corefTaskFile = new CmdOption[String]("coref-task-file", "The file containing the coref tasks", true)
  val codec = new CmdOption[String]("codec", "UTF-8", "STRING", "The encoding to use.")
  val outputDir = new CmdOption[String]("output-dir", "Where to write the output", true)
  val numThreads = new CmdOption[Int]("num-threads", 20, "INT", "Number of threads to use")
}

trait  RunParallelInMemoryOpts extends RunParallelOpts {
  val inputJson = new CmdOption[String]("input-json","input file with one JSON InventorMention per line.")
}

/**
 * Executable for running with a single canopy 
 */
object RunParallelSingleCanopyCoreference {

  def main(args: Array[String]): Unit = {
    val opts = new RunParallelOpts {}
    opts.parse(args)

    val allWork = LoadCorefTasks.load(new File(opts.corefTaskFile.value),opts.codec.value).toIterable

    val db = new InventorMentionDB(opts.hostname.value, opts.port.value, opts.dbname.value, opts.collectionName.value, false)
    val ds = db.toDatastoreByUUID

    val keystore = InMemoryKeystore.fromFile(new File(opts.keystorePath.value),opts.keystoreDim.value,opts.keystoreDelim.value,opts.codec.value)

    new File(opts.outputDir.value).mkdirs()
    val parCoref = new SingleCanopyParallelHierarchicalCoref(allWork,ds,opts,keystore,new File(opts.outputDir.value))

    parCoref.runInParallel(opts.numThreads.value)
    
    val timesPW = new PrintWriter(new File(opts.outputDir.value,"timing.txt"))
    timesPW.println(parCoref.times.map(f => f._1 + "\t" + f._2).mkString(" "))
    timesPW.close()
    parCoref.printTimes()
  }
  
}

/**
 * This is the executable for running disambiguation using the
 * coreference algorithm with multiple canopies. The various coreference tasks
 * are distributed between the threads. The mentions are stored in a MongoDB collection 
 * of InventorMention objects. The coreference results for each task are written out to 
 * flat file. The results are aggregrated into a single flat file of the specified format. 
 */
object RunParallelMultiCanopyCoreference {

  def main(args: Array[String]): Unit = {
    
    // Uses command line options from factorie
    val opts = new RunParallelOpts {}
    opts.parse(args)

    // Load all of the coref tasks into memory, so they can easily be distributed amongst the different threads
    val allWork = LoadCorefTasks.load(new File(opts.corefTaskFile.value),opts.codec.value).toIterable

    // Create the interface to the MongoDB containing the inventor mentions 
    val db = new InventorMentionDB(opts.hostname.value, opts.port.value, opts.dbname.value, opts.collectionName.value, false)
    
    // The object that can query the database by the inventor-ids (i.e. patentNumber-sequenceNumber)
    val ds = db.toDatastoreByUUID
    
    // The lookup table containing the embeddings. 
    val keystore = InMemoryKeystore.fromFile(new File(opts.keystorePath.value),opts.keystoreDim.value,opts.keystoreDelim.value,opts.codec.value)

    // Create the output directory
    new File(opts.outputDir.value).mkdirs()
    
    // Initialize the coreference algorithm
    val parCoref = new MultiCanopyParallelHierarchicalCoref(allWork,ds,opts,keystore,new File(opts.outputDir.value))

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

/**
  * This is the executable for running disambiguation using the
  * coreference algorithm with multiple canopies. The various coreference tasks
  * are distributed between the threads. The mentions are stored in a MongoDB collection
  * of InventorMention objects. The coreference results for each task are written out to
  * flat file. The results are aggregrated into a single flat file of the specified format.
  */
object RunParallelMultiCanopyCoreferenceInMemory {

  def main(args: Array[String]): Unit = {

    // Uses command line options from factorie
    val opts = new RunParallelInMemoryOpts {}
    opts.parse(args)

    // Load all of the coref tasks into memory, so they can easily be distributed amongst the different threads
    val allWork = LoadCorefTasks.load(new File(opts.corefTaskFile.value),opts.codec.value).toIterable

    // Load the inventor mentions from JSON
    val inventorMentions = LoadJSONInventorMentions.load(new File(opts.inputJson.value),opts.codec.value)

    // Create a hashmap of the inventor mentions by id
    val ds = InMemoryPrimaryKeyDatastore.fromIterator(inventorMentions.map(im => (im.uuid.value,im)))

    // The lookup table containing the embeddings.
    val keystore = InMemoryKeystore.fromFile(new File(opts.keystorePath.value),opts.keystoreDim.value,opts.keystoreDelim.value,opts.codec.value)

    // Create the output directory
    new File(opts.outputDir.value).mkdirs()

    // Initialize the coreference algorithm
    val parCoref = new MultiCanopyParallelHierarchicalCoref(allWork,ds,opts,keystore,new File(opts.outputDir.value))

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