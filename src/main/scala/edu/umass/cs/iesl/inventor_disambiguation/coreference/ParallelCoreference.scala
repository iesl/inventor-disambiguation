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
import java.util
import java.util.Date

import cc.factorie.util.Threading
import edu.umass.cs.iesl.inventor_disambiguation._
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.coreference.{CorefTask, CorefTaskWithMentions, InventorMention}
import edu.umass.cs.iesl.inventor_disambiguation.db.Datastore
import edu.umass.cs.iesl.inventor_disambiguation.load.{DelimitedFileLoader, LoadBasicCorefOutputRecord}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
 * The base trait for running coreference in multiple threads. 
 * Parallelizing across canopies.  
 */
trait ParallelCoreference {

  /**
   * Keeps track of the running times 
   */
  val times = new ArrayBuffer[(String,String)]()

  /**
   * All of the canopy groups that need to be processed  
   * @return
   */
  def allWork: Iterable[CorefTask]

  /**
   * The mechanism for outputing the coreference groups 
   * @return
   */
  def writer: CorefOutputWriter[BasicCorefOutputRecord]

  /**
   * Given a task, fetch the algorithm that will be used to run it 
   * @param task - the task
   * @return
   */
  def algorithmFromTask(task: CorefTaskWithMentions): CoreferenceAlgorithm[InventorMention]

  /**
   * Run the disambiguation algorithm on the given task. 
   * @param alg - the algorithm
   * @param task - the task
   */
  def runCoref(alg: CoreferenceAlgorithm[InventorMention], task: CorefTaskWithMentions) = alg.run()

  /**
   * Fetch the InventorMentions associated with a given task 
   * @param corefTask - the task
   * @return - the task with mentions
   */
  def getMentions(corefTask: CorefTask):CorefTaskWithMentions

  /**
   * Given a task, fetch the mentions, generate the algorithm instance, run the task 
   * and write out the results.  
   * @param task
   */
  def handleTask(task: CorefTask): Unit = {
    val wrtr = writer
    val taskWithMentions = getMentions(task)
    val alg = algorithmFromTask(taskWithMentions)
    runCoref(alg,taskWithMentions)
    wrtr.write(task, gatherResults(alg.mentions))
  }
  
  def gatherResults(mentions: Iterable[InventorMention]): Iterable[BasicCorefOutputRecord] = {
    mentions.groupBy(_.entityId.value).map(f => {
      val (firstName,middleName,lastName,suffix) = determineNames(f._2,0.8,0.8)
      f._2.map(m => BasicCorefOutputRecord(m.uuid.value,m.self.value.uuid.value,m.entityId.value,firstName,middleName,lastName,suffix))
    }).flatten
  }
  
  def determineNames(cluster: Iterable[InventorMention], middleNameThres: Double = 0.8, suffixThres: Double = 0.8, minClusterSizeForMajority: Int = 25) = {

    val clusterSize = cluster.size

    val firstNames = cluster.flatMap(_.rawName.opt.flatMap(_.nameFirst.opt))
    val middleNames = cluster.flatMap(_.rawName.opt.flatMap(_.nameMiddles.opt)).flatten
    val lastNames = cluster.flatMap(_.rawName.opt.flatMap(_.nameLast.opt))
    val suffixes = cluster.flatMap(_.rawName.opt.flatMap(_.nameSuffixes.opt)).flatten

    val firstName = firstNames.mostCommonElements.maxBySafe(_.length)
    val lastName = lastNames.mostCommonElements.maxBySafe(_.length)
    val middleName =  if (clusterSize <= minClusterSizeForMajority)
      middleNames.elementsInMajority.maxBySafe(_.length)
    else
      middleNames.elementsOccurringXPercent(middleNameThres).maxBySafe(_.length)
    val suffix =  if (clusterSize <= minClusterSizeForMajority)
      suffixes.elementsInMajority.maxBySafe(_.length)
    else
      suffixes.elementsOccurringXPercent(suffixThres).maxBySafe(_.length)
    (firstName.getOrElse(""),middleName.getOrElse(""),lastName.getOrElse(""),suffix.getOrElse(""))
  }

  /**
   * Distribute the coref tasks across many threads 
   * @param numThreads - number of threads to use
   * @return
   */
  def runInParallel(numThreads: Int) = {
    val corefStart = System.currentTimeMillis()
    times.+=(("Coreference Starting Timestamp", new Date(System.currentTimeMillis()).toString))
    Threading.parForeach(allWork.zipWithIndex,numThreads)(
        task => {
          if (task._2 % 1000 == 0)
            println(s"[ParallelCoreference] Thread ID#: ${Thread.currentThread().getId} Completed about ${task._2} tasks")
          handleTask(task._1)
        })
    val corefTime = System.currentTimeMillis() - corefStart
    times.+=(("Coreference Time", corefTime.toString))
    times.+=(("Coreference Ending Timestamp", new Date(System.currentTimeMillis()).toString))
    println(s"[ParallelCoreference] Finished Coreference in $corefTime ms. Finalizing output.")
    times.+=(("Finalization Starting Timestamp", new Date(System.currentTimeMillis()).toString))
    val finalizationStart = System.currentTimeMillis()
    finalizeOutput()
    val finalizationTime = System.currentTimeMillis() - finalizationStart
    times.+=(("Finalization Time",finalizationTime.toString))
    times.+=(("Finalization Ending Timestamp", new Date(System.currentTimeMillis()).toString))
  }

  /**
   * Collect the output and format it in some way 
   */
  def finalizeOutput(): Unit

  /**
   * Display the running time information  
   */
  def printTimes() = times.foreach(f => println(f._1 + ":\t" + f._2))

}

/**
 * Load menetions from a mongo db based on inventor ids 
 */
trait LoadMentionsFromMongo {

  val datastore: Datastore[String,InventorMention]

  def load(ids: Iterable[String]) = ids.map(datastore.get(_).head)

}

/**
 * Base class for the distributed inventor coreference  
 * @param allWork - the complete group of tasks
 * @param datastore - the interface to mongo
 * @param outputDir - where to write the output
 */
abstract class StandardParallelCoreference(override val allWork: Iterable[CorefTask],
                                           override val datastore: Datastore[String, InventorMention],
                                           outputDir: File,codec: String,convertOutputToOneBasedIds: Boolean) extends ParallelCoreference with LoadMentionsFromMongo {

  override def writer: CorefOutputWriter[BasicCorefOutputRecord] = new TextCorefOutputWriter(outputDir,codec,convertToOneBased = convertOutputToOneBasedIds)

  override def finalizeOutput(): Unit = writer.collectResults(outputDir,new File(outputDir,"all-results.txt"))

  override def getMentions(corefTask: CorefTask): CorefTaskWithMentions = new CorefTaskWithMentions(corefTask.name,corefTask.ids,load(corefTask.ids))

}

/**
 * The implementation using a single canopy. 
 * @param allWork - the complete group of tasks
 * @param datastore - the interface to mongo
 * @param opts - the model parameters
 * @param keystore - the word embedding look up table
 * @param outputDir - where to write the output
 */
class SingleCanopyParallelHierarchicalCoref(override val allWork: Iterable[CorefTask],
                                            override val datastore: Datastore[String, InventorMention],
                                            opts: InventorModelOptions,
                                            keystore: Keystore,
                                            outputDir: File, codec: String = "UTF-8",convertOutputToOneBasedIds: Boolean=true) extends StandardParallelCoreference(allWork,datastore,outputDir,codec,convertOutputToOneBasedIds) {
  override def algorithmFromTask(task: CorefTaskWithMentions): CoreferenceAlgorithm[InventorMention] = new SingleCanopyHierarchicalInventorCorefRunner(opts,keystore,task.mentions)
}

/**
 * The implementation using multiple canopies (what was submitted) 
 * @param allWork - the complete group of tasks
 * @param datastore - the interface to mongo
 * @param opts - the model parameters
 * @param keystore - the embedding lookup table
 * @param outputDir - where to write the output
 */
class MultiCanopyParallelHierarchicalCoref(override val allWork: Iterable[CorefTask],
                                            override val datastore: Datastore[String, InventorMention],
                                            opts: InventorModelOptions,
                                            keystore: Keystore,
                                            outputDir: File, codec: String = "UTF-8",convertOutputToOneBasedIds: Boolean=true) extends StandardParallelCoreference(allWork,datastore,outputDir,codec,convertOutputToOneBasedIds) {
  override def algorithmFromTask(task: CorefTaskWithMentions): CoreferenceAlgorithm[InventorMention] = new MultiCanopyHierarchicalInventorCorefRunner(opts,keystore,task.mentions)
}

/**
 * Interface for writing the coreference output 
 */
trait CorefOutputWriter[OutputRecord <: CorefOutputRecord] {

  def write(task: CorefTask, mentions: Iterable[OutputRecord])

  def collectResults(outputDir: File, collectedFile: File)

}

trait CorefOutputRecord


case class BasicCorefOutputRecord(mentionId: String,
                                  oneBasedMentionId:String,
                             rawInventorId: String,
                             rawDisambiguatedId: String,
                             firstName: String,
                             middleName: String,
                             lastName: String,
                             suffixes: String) extends CorefOutputRecord {
  
  var disambiguatedId = rawDisambiguatedId

  override def toString = s"$mentionId\t$oneBasedMentionId\t$rawInventorId\t$disambiguatedId\t$firstName\t$middleName\t$lastName\t$suffixes"
}

object BasicCorefOutputRecord {

  def apply(mentionId: String,
            rawInventorId: String,
            rawDisambiguatedId: String,
            firstName: String,
            middleName: String,
            lastName: String,
            suffixes: String):BasicCorefOutputRecord = BasicCorefOutputRecord(mentionId,{val Array(patentId,seq) = mentionId.split("-"); s"$patentId-${seq.toInt+1}"},rawInventorId,rawDisambiguatedId,firstName,middleName,lastName,suffixes)
}


object CorefOutputWriterHelper {

  def sortedNormalizedResults(results: Iterable[BasicCorefOutputRecord], convertToOneBased: Boolean = true): Iterable[BasicCorefOutputRecord] = {

    val getMentionId = if (convertToOneBased)
      (r: BasicCorefOutputRecord) => r.oneBasedMentionId
    else
      (r: BasicCorefOutputRecord) => r.mentionId

    // 1. Create the entity id map
    // The output format is to have the disambiguated ID be the mention
    // id of the first record in the cluster
    val sortedResults = results.toSeq.sortBy(f => getMentionId(f))
    val entityIdMap = new util.HashMap[String,String](100000).asScala
    sortedResults.foreach{
      case r: BasicCorefOutputRecord =>
        if (!entityIdMap.contains(r.rawDisambiguatedId)) {
          entityIdMap.put(r.rawDisambiguatedId,getMentionId(r))
        } else if (entityIdMap(r.rawDisambiguatedId) > getMentionId(r)) {
          entityIdMap.put(r.rawDisambiguatedId,getMentionId(r))
        }
    }
    // 2.  Sort the inventor ids
    sortedResults.foreach(f => f.disambiguatedId = entityIdMap(f.rawDisambiguatedId))
    sortedResults
  }
  
}

/**
 * Implementation of CorefOutput interface which writes the coref tasks results each to their own file
 * in a given directory. A subdirectory structure is create so that "ls" does not take too long
 * @param outputDirectory - the output directory
 * @param codec - the encoding of the files (default UTF8)
 */
class TextCorefOutputWriter(outputDirectory: File, codec: String = "UTF-8",convertToOneBased: Boolean = true) extends CorefOutputWriter[BasicCorefOutputRecord] {

  val validFilePathRegex = "^[1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM\\._]+$"

  def prefix(task: CorefTask) = if (task.name.matches(validFilePathRegex)) task.name.slice(0,7) else "misc"

  val extension = ".coref"

  def addExtension(string: String) = string + extension
  
  override def write(task: CorefTask, results: Iterable[BasicCorefOutputRecord]): Unit = {
    val subDirName = prefix(task)
    new File(outputDirectory,subDirName).mkdirs()
    val filename = addExtension(if (task.name.matches(validFilePathRegex)) task.name else task.name.hashCode.toString)
    val pw = new PrintWriter(new File(new File(outputDirectory,subDirName),filename),codec)
    results.foreach(f => {pw.println(f.toString); pw.flush()})
    pw.close()
  }

  override def collectResults(outputDir: File, collectedFile: File): Unit = {
    val results = CorefOutputWriterHelper.sortedNormalizedResults(outputDir.list().filter(f => new File(outputDir,f).isDirectory).flatMap(f => loadAllResults(new File(outputDir,f))),convertToOneBased)

    // main output in standard format
    val pw = new PrintWriter(collectedFile,codec)
    results.foreach(f => {pw.println(f.toString); pw.flush()})
    pw.close()

    // scorable output
    val scorableFile = new File(collectedFile.getAbsolutePath + ".toScore")
    val pwToScore = new PrintWriter(scorableFile,codec)
    results.foreach(f => {pwToScore.println(s"${f.mentionId}\t${f.disambiguatedId}"); pwToScore.flush()})
    pwToScore.close()
  }

  def loader: DelimitedFileLoader[BasicCorefOutputRecord] = LoadBasicCorefOutputRecord
  
  def loadAllResults(directory: File) = {
    val results = new ArrayBuffer[BasicCorefOutputRecord]
    directory.list().filter(_.endsWith(extension)).foreach{
      filename =>
        results ++= loader.load(new File(directory,filename),codec)
    }
    results
  }
}