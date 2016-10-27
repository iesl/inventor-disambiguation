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


package edu.umass.cs.iesl.inventor_disambiguation.experiment

import java.io.{File, PrintWriter}

import cc.factorie.variable.CategoricalDomain
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.Inventor
import edu.umass.cs.iesl.inventor_disambiguation.load.{LoadInventor, LoadTDInventor}

class NameSuffixesStatistics {

  val firstnameCommaSeparated = new CategoricalDomain[String]()
  firstnameCommaSeparated.gatherCounts = true
  val firstnameSpaceSeparated = new CategoricalDomain[String]()
  firstnameSpaceSeparated.gatherCounts = true
  
  val lastnameCommaSeparated = new CategoricalDomain[String]()
  lastnameCommaSeparated.gatherCounts = true
  val lastnameSpaceSeparated = new CategoricalDomain[String]()
  lastnameSpaceSeparated.gatherCounts = true
  
  def gatherInfo(inventors: Iterator[Inventor]) = 
    inventors.foreach(
       inventors => {
         inventors.nameFirst.opt.map(_.trim).foreach {
           case firstname =>
             val commaSplit = firstname.split(",")
             val spaceSplit = firstname.split("\\s+")
             if (commaSplit.length > 1)
               firstnameCommaSeparated.index(commaSplit.last)
             if (spaceSplit.length > 1) {
               spaceSplit.zipWithIndex.drop(1).map(f => f._1 + "_#" + (spaceSplit.length - f._2 - 1)).foreach(firstnameSpaceSeparated.index)
               firstnameSpaceSeparated.index(spaceSplit.drop(1).mkString(" ") + "_#all")
             }
         }
         inventors.nameLast.opt.map(_.trim).foreach {
           case lastname =>
             val commaSplit = lastname.split(",")
             val spaceSplit = lastname.split("\\s+")
             if (commaSplit.length > 1)
               lastnameCommaSeparated.index(commaSplit.last)
             if (spaceSplit.length > 1) {
               spaceSplit.zipWithIndex.drop(1).map(f => f._1 + "_#" + (spaceSplit.length - f._2 - 1)).foreach(lastnameSpaceSeparated.index)
               lastnameSpaceSeparated.index(spaceSplit.drop(1).mkString(" ") + "_#all")
             }
         }
       }
    )
  
  def writeResults(outputDir: File) = {
    val pw1 = new PrintWriter(new File(outputDir,"first_comma_separated.txt"),"UTF-8")
    firstnameCommaSeparated.map(f => f.category -> firstnameCommaSeparated.count(f.category)).sortBy(_._2).foreach(f => pw1.println(f._1 + "\t" + f._2))
    pw1.close()

    val pw2 = new PrintWriter(new File(outputDir,"first_space_separated.txt"),"UTF-8")
    firstnameSpaceSeparated.map(f => f.category -> firstnameSpaceSeparated.count(f.category)).sortBy(_._2).foreach(f => pw2.println(f._1 + "\t" + f._2))
    pw2.close()
    
    
    val pw3 = new PrintWriter(new File(outputDir,"last_comma_separated.txt"),"UTF-8")
    lastnameCommaSeparated.map(f => f.category -> lastnameCommaSeparated.count(f.category)).sortBy(_._2).foreach(f => pw3.println(f._1 + "\t" + f._2))
    pw3.close()

    val pw4 = new PrintWriter(new File(outputDir,"last_space_separated.txt"),"UTF-8")
    lastnameSpaceSeparated.map(f => f.category -> lastnameSpaceSeparated.count(f.category)).sortBy(_._2).foreach(f => pw4.println(f._1 + "\t" + f._2))
    pw4.close()
  }
  
}



object TDNameSuffixes {
  
  def main (args: Array[String]) {
    val input = new File(args(0))
    val output = new File(args(1))
    val suff = new NameSuffixesStatistics
    suff.gatherInfo(LoadTDInventor.load(input))
    suff.writeResults(output)
  }
  
}


object FullFileSuffixes {

  def main (args: Array[String]) {
    val input = new File(args(0))
    val output = new File(args(1))
    val suff = new NameSuffixesStatistics
    suff.gatherInfo(LoadInventor.load(input))
    suff.writeResults(output)
  }
}

class AccentStatistics {
  
  val accentedLastNames = new CategoricalDomain[String]()
  val accentedFirstNames = new CategoricalDomain[String]()
  accentedLastNames.gatherCounts = true
  accentedFirstNames.gatherCounts = true

  val accentRegex = "\\{[a-zA-Z0-9]+ over \\([a-zA-Z0-9\\s]+\\)\\}".r
  
  def gatherInfo(inventor: Inventor):Unit = {
    inventor.nameLast.opt.foreach {
      name =>
        if (accentRegex.findAllIn(name).nonEmpty)
          accentedLastNames.index(name)
    }
    inventor.nameFirst.opt.foreach {
      name =>
        if (accentRegex.findAllIn(name).nonEmpty)
          accentedFirstNames.index(name)
    }
  }
  
  def gatherInfo(inventors: Iterator[Inventor]):Unit = inventors.foreach(gatherInfo)


  def writeResults(outputDir: File) = {
    val pw1 = new PrintWriter(new File(outputDir,"first_accented.txt"),"UTF-8")
    accentedFirstNames.map(f => f.category -> accentedFirstNames.count(f.category)).sortBy(_._2).foreach(f => pw1.println(f._1 + "\t" + f._2))
    pw1.close()

    val pw2 = new PrintWriter(new File(outputDir,"last_accented.txt"),"UTF-8")
    accentedLastNames.map(f => f.category -> accentedLastNames.count(f.category)).sortBy(_._2).foreach(f => pw2.println(f._1 + "\t" + f._2))
    pw2.close()
  }
  
}


object TDNameAccents {

def main (args: Array[String]) {
val input = new File(args(0))
val output = new File(args(1))
val suff = new AccentStatistics
suff.gatherInfo(LoadTDInventor.load(input))
suff.writeResults(output)
}

}


object FullFileAccents {

  def main (args: Array[String]) {
    val input = new File(args(0))
    val output = new File(args(1))
    val suff = new AccentStatistics
    suff.gatherInfo(LoadInventor.load(input))
    suff.writeResults(output)
  }
}