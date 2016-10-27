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

import cc.factorie.util.DefaultCmdOptions
import cc.factorie.variable.CategoricalDomain
import edu.umass.cs.iesl.inventor_disambiguation.coreference.{Canopies, CaseInsensitiveReEvaluatingNameProcessor, NameProcessor}
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.Inventor
import edu.umass.cs.iesl.inventor_disambiguation.load.LoadInventor


class NameFrequencyStatistics(canopyFunction: Inventor => String,nameProcessor: NameProcessor) {

  val lastNames = new CategoricalDomain[String]()
  lastNames.gatherCounts = true
  val firstNames = new CategoricalDomain[String]()
  firstNames.gatherCounts = true
  val firstAndLastNames = new CategoricalDomain[String]()
  firstAndLastNames.gatherCounts = true
  val fullNames = new CategoricalDomain[String]()
  fullNames.gatherCounts = true
  val canopy = new CategoricalDomain[String]()
  canopy.gatherCounts = true
  
  def processInventor(inventor: Inventor):Unit = {
    nameProcessor.process(inventor)
    inventor.nameLast.opt.map(lastNames.index)
    inventor.nameFirst.opt.map(firstNames.index)
    val firstAndLast = Canopies.firstAndLastName(inventor,caseSensitive = true)
    firstAndLastNames.index(firstAndLast)
    val fullName = Canopies.fullName(inventor, caseSensitive = true)
    fullNames.index(fullName)
    val c = canopyFunction(inventor)
    canopy.index(c)
  }
  
  def processInventors(inventors: Iterator[Inventor]):Unit = inventors.foreach(processInventor)
  
  def processPar(work: Iterable[Iterator[Inventor]]):Unit = work.par.foreach(processInventors)
  
  def processPar(file: File, codec: String, numThreads: Int):Unit = processPar(LoadInventor.loadMultiple(file,codec,numThreads))
  
  def writeResults(outputDir: File) = {
    writeSingleFile(lastNames, new File(outputDir,"lastNames.txt"))
    writeSingleFile(firstNames, new File(outputDir,"firstNames.txt"))
    writeSingleFile(firstAndLastNames, new File(outputDir,"firstAndLastNames.txt"))
    writeSingleFile(fullNames, new File(outputDir,"fullNames.txt"))
    writeSingleFile(canopy, new File(outputDir,"canopy.txt"))
  }
  
  def writeSingleFile(domain: CategoricalDomain[String],file: File) = {
    val total = domain.countsTotal
    val pw = new PrintWriter(file, "UTF-8")
    domain.categories.sortBy(domain.count).foreach(
      category => {
        val rawCount = domain.count(category)
        val prob = rawCount.toDouble / total.toDouble
        val logProb = Math.log(prob)
        pw.println(category + "\t" + rawCount + "\t" + prob + "\t" + logProb)
        pw.flush()
      })
    pw.close()
  }
}


class NameFrequencyStatisticsOpts extends DefaultCmdOptions {
  val inventorFile = new CmdOption[String]("inventor-file", "The rawinventor.csv file", true)
  val outputDir = new CmdOption[String]("output-dir", "Where to write the results",true)
  val numThreads = new CmdOption[Int]("num-threads", 12, "INT", "Number of threads")
}


object NameFrequencyStatistics {
  
  def main(args:Array[String]): Unit = {
    val opts = new NameFrequencyStatisticsOpts
    opts.parse(args)
    
    val stats = new NameFrequencyStatistics(Canopies.lastNameAndFirstThreeCharsCaseInsensitive,CaseInsensitiveReEvaluatingNameProcessor)
    stats.processPar(new File(opts.inventorFile.value),"UTF-8",opts.numThreads.value)
    new File(opts.outputDir.value).mkdirs()
    stats.writeResults(new File(opts.outputDir.value))
  }
  
}