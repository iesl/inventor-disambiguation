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

import java.io.{File, PrintWriter, Writer}

import cc.factorie.util.DefaultCmdOptions
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.Patent
import edu.umass.cs.iesl.inventor_disambiguation.load.{LoadPatent, LoadTDPatent}
import edu.umass.cs.iesl.inventor_disambiguation.utilities.FileIO

import scala.collection.mutable.ArrayBuffer


class GatherEmbeddingTrainingDataOpts extends DefaultCmdOptions {
  val tdPatentFile = new CmdOption[String]("td-patent-file", "The training data file of patents", false)
  val patentFile = new CmdOption[String]("patent-file", "The full patent file", false)
  val output = new CmdOption[String]("output", "The output filename", true) 
  val numThreads = new CmdOption[Int]("num-threads", "The number of threads to use", true)
}

object GatherEmbeddingTrainingData {

  def main(args: Array[String]): Unit = {

    val opts = new GatherEmbeddingTrainingDataOpts
    opts.parse(args)
    assert(opts.tdPatentFile.wasInvoked ^ opts.patentFile.wasInvoked, "Either a training data file or full data file must be given as input, but not both")

    if (opts.tdPatentFile.wasInvoked)
      processTDFile(opts.tdPatentFile.value, opts.output.value, opts.numThreads.value)
    else
      processFullFile(opts.patentFile.value, opts.output.value, opts.numThreads.value)

  }
  
  def trainingData(patents: Iterator[Patent]) = patents.map(p => (p.title.opt ++ p.patentAbstract.opt).mkString(" ").trim)
  
  def writeTrainingData(data: Iterator[String], writer: Writer) = 
    data.foreach(d => {writer.write(d); writer.write("\n"); writer.flush()})
  
  def processPar(patentSets: Iterable[Iterator[Patent]], outputFilename: String) = {
    val filenames = new ArrayBuffer[String]()
    patentSets.zipWithIndex.par.foreach{
      case (patents,id) =>
        val filename = s"$outputFilename-$id"
        synchronized(filenames += filename)
        val writer = new PrintWriter(filename, "UTF-8")
        writeTrainingData(trainingData(patents),writer)
        writer.close()
    }
    FileIO.merge(filenames.sorted.map(new File(_)),new File(outputFilename), "UTF-8")
    FileIO.delete(filenames)
  }

  def processTDFile(tdfilename: String, outputFilename: String, numThreads: Int) = {
    val patentSets = LoadTDPatent.loadMultiple(new File(tdfilename),"UTF-8",numThreads)
    processPar(patentSets,outputFilename)
  }
  
  def processFullFile(tdfilename: String, outputFilename: String, numThreads: Int) = {
    val patentSets = LoadPatent.loadMultiple(new File(tdfilename),"UTF-8",numThreads)
    processPar(patentSets,outputFilename)
  }
}
