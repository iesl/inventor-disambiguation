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


package edu.umass.cs.iesl.inventor_disambiguation.process

import java.io.{File, PrintWriter}
import java.util

import cc.factorie.util.{Threading, DefaultCmdOptions}
import edu.umass.cs.iesl.inventor_disambiguation._
import edu.umass.cs.iesl.inventor_disambiguation.coreference.{Canopies, CaseInsensitiveReEvaluatingNameProcessor, NameProcessor}
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.Inventor
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.coreference.{InventorMention, CorefTask}
import edu.umass.cs.iesl.inventor_disambiguation.load.{LoadJSONInventorMentions, LoadInventor}

import scala.collection.GenMap
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source


class GenerateCorefTaskFileOpts extends DefaultCmdOptions {
  val inventorFile = new CmdOption[String]("inventor-file", "The file that contains the inventors", true)
  val outputFile = new CmdOption[String]("output-file", "Where to write the output", true)
  val codec = new CmdOption[String]("codec", "UTF-8", "STRING", "The file encoding")
  val numThreads = new CmdOption[Int]("num-threads", 20, "INT", "The number of threads")
  val idsFile = new CmdOption[String]("ids-file", "Optional. If specified, only use those inventor's whose ID is in this file.", false)
}


object GenerateCorefTaskFile {

  
  def fromFile(file: File, codec: String,numThreads: Int, assignment: Inventor => String, nameProcessor: NameProcessor, ids: Set[String]): Iterable[(String,Iterable[String])] = {
    @volatile var maps = new ArrayBuffer[Map[String,Iterable[String]]]()
    LoadInventor.loadMultiple(file, codec, numThreads).par.foreach {
      map =>
        val r = assignmentMap(canopyAssignments(map.filter(p => ids.isEmpty || ids.contains(p.inventorID.value)), assignment,nameProcessor))
        maps += r
    }
    mergeMaps(maps)
  }

  def writeToFile(map:Iterable[(String,Iterable[String])], file: File) = {
    val pw = new PrintWriter(file, "UTF-8")
    map.foreach{
      case (name,ids) =>
        pw.println(CorefTask(name,ids).toString)
        pw.flush() 
    }
    pw.close()
  }

  def canopyAssignments(inventors: Iterator[Inventor], assignment: Inventor => String,nameProcessor: NameProcessor) = {
    inventors.map(f => { nameProcessor.process(f); (assignment(f),f.inventorID.value)})
  } 
  
  def assignmentMap(assignments: Iterator[(String,String)]) =
    assignments.toIterable.groupBy(f => f._1).mapValues(_.map(_._2))
    
  def mergeMaps(maps: Iterable[GenMap[String,Iterable[String]]]): Iterable[(String,Iterable[String])] = {
    
    val finalMap = new util.HashMap[String, ArrayBuffer[String]](maps.map(_.size).sum).asScala
    maps.foreach(_.foreach{
      case (string,iter)  => 
        if (!finalMap.contains(string))
          finalMap.put(string, new ArrayBuffer[String]())
        finalMap(string) ++= iter
    })
    // Since this was generated in parallel, we need to somehow in force an ordering
    finalMap.mapValues(f => f.sorted).toIndexedSeq.sortBy(m => (-m._2.size,m._1))
  }
  
  
  def main(args: Array[String]):Unit = {
    val opts = new GenerateCorefTaskFileOpts
    opts.parse(args)
    val ids = if (opts.idsFile.wasInvoked) Source.fromFile(opts.idsFile.value,opts.codec.value).getLines().toIterable.toSet[String] else Set[String]()
    new File(opts.outputFile.value).mkParentDirs()
    val m = fromFile(new File(opts.inventorFile.value),opts.codec.value,opts.numThreads.value,Canopies.lastNameAndFirstThreeCharsCaseInsensitive,CaseInsensitiveReEvaluatingNameProcessor,ids)
    writeToFile(m,new File(opts.outputFile.value))
  }

}

object GenerateCorefTaskFileJSON {

  def fromMultiple(numThreads: Int, mentionStreams: Iterable[Iterator[InventorMention]], assignment: InventorMention => String, ids: Set[String], nameProcessor: NameProcessor = CaseInsensitiveReEvaluatingNameProcessor) = {
    @volatile var maps = new ArrayBuffer[GenMap[String,Iterable[String]]]()
    val start = System.currentTimeMillis()
    @volatile var totalCount = 0
    println(s"[GenerateCorefTasks] Determining Blocking/Canopy assignments using ${mentionStreams.size} streams of mentions and $numThreads threads")
    Threading.parForeach(mentionStreams, numThreads)(
      mentions => {
        val subMap = new util.HashMap[String,ArrayBuffer[String]]().asScala
        var count = 0
        mentions.foreach{
          m =>
            nameProcessor.process(m.self.value)
            val canopy = new String(assignment(m))
            if (!subMap.contains(canopy))
              subMap.put(canopy,new ArrayBuffer[String]())
            subMap(canopy) += new String(m.uuid.value)
            count += 1
            if (count % 100000 == 0) {
              synchronized {
                totalCount += count
                count = 0
                print(s"\r[GenerateCorefTasks] Processed $totalCount records")
              }
            }
        }
        synchronized {maps += subMap}
      })
    val end = System.currentTimeMillis()
    println(s"\n[GenerateCorefTasks] Finished processing streams in parallel in ${end-start} ms. Merging the results.")
    GenerateCorefTaskFile.mergeMaps(maps)
  }


  def main(args: Array[String]): Unit = {
    val opts = new GenerateCorefTaskFileOpts
    opts.parse(args)
    val ids = if (opts.idsFile.wasInvoked) Source.fromFile(opts.idsFile.value,opts.codec.value).getLines().toIterable.toSet[String] else Set[String]()
    new File(opts.outputFile.value).mkParentDirs()
    val m = LoadJSONInventorMentions.loadMultiple(new File(opts.inventorFile.value),opts.codec.value,opts.numThreads.value)
    GenerateCorefTaskFile.writeToFile(fromMultiple(opts.numThreads.value,m,(im: InventorMention) => Canopies.lastNameAndFirstThreeCharsCaseInsensitive(im.self.value),ids,CaseInsensitiveReEvaluatingNameProcessor),new File(opts.outputFile.value))
  }
}