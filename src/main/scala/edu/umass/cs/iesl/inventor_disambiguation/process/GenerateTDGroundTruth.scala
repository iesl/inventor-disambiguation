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

import java.io.{PrintWriter, File}
import java.util

import edu.umass.cs.iesl.inventor_disambiguation.data_structures.Inventor
import edu.umass.cs.iesl.inventor_disambiguation.load.CommaSeparateFileLoader

import scala.collection.JavaConverters._
import scala.util.Random


object GenerateTDGroundTruth {
  
  //patent_number,name_first,name_last,city,state,country,id_ens,id_is
  case class TDInventorRow(id: String, patent_number: String, name_first: String, name_last: String, city: String, state: String, country: String, id_ens: String, id_is: String, sequence: String) {
    var groundTruthID: String = null
    
    val ens_key = "id_ens_" + id_ens
    val is_key = "id_is_" + id_is
  }
  
  val loader = new CommaSeparateFileLoader[TDInventorRow] {
    override def parse(split: Array[String]): Option[TDInventorRow] = {
      val id = split(0) + "-" + split(8)
      val res = Some(TDInventorRow(id, split(0), split(1), split(2), split(3), split(4), split(5), split(6), split(7), split(8)))
      res
    }

    override def skipFirstLine: Boolean = true

    override def expectedLineLengths: Set[Int] = Set(9)
  }
  
  def getPointID(record: TDInventorRow) = record.id 

  def getPointID(inventor: Inventor) = inventor.uuid.value

  def main(args: Array[String]): Unit = {
    
    val inputFile = new File(args(0))
    val outputFile = new File(args(1))

    val records = generateRecords(inputFile)
    
    val writer = new PrintWriter(outputFile,"UTF-8")
    val countWithLabel = records.count(_.groundTruthID != null)
    val countNoLabel = records.length - countWithLabel
    
    records.filter(_.groundTruthID != null).foreach {
      record =>
        val pointID = Iterable(record.patent_number,record.name_first,record.name_last,record.city,record.state,record.country).mkString("_").replaceAll("\\s","")
        val clusterLabel = record.groundTruthID
        writer.print(pointID)
        writer.print("\t")
        writer.print(clusterLabel)
        writer.println()
        writer.flush()
    }
    writer.close()
    println(s"[${this.getClass.getSimpleName}] Processed ${records.length} records. Number with ground truth: $countWithLabel. Number without ground truth: $countNoLabel")

  }
  
  def generateRecords(tdInventorFile: File) = {

    val random = new Random(1900) // provide seed so ids will be the same each time this is run.

    val records = loader.load(tdInventorFile).toIndexedSeq

    val correctedIDs = new util.HashMap[String,String]().asScala

    // First loop over the records. If  and assign a random uuid to those records which have both ids defined.

    // 1. Loop over each record. Assign groundTruthID of whichever E&S or IS id is defined. If both are defined, assign a random uuid and record the correspond E&S and IS ids.
    records.filter(r => r.id_ens.nonEmpty || r.id_is.nonEmpty).foreach{
      record =>
        if (record.id_ens.nonEmpty && record.id_is.nonEmpty) {
          println(s"\n[${this.getClass.getSimpleName}] WARNING: Record: $record has both an id_ens: ${record.id_ens} and an id_is: ${record.id_is}.")
          if (correctedIDs.contains(record.ens_key) && correctedIDs.contains(record.is_key)) {
            assert(correctedIDs(record.ens_key) == correctedIDs(record.is_key), "Corrected IDs must be consistent")
            record.groundTruthID = correctedIDs(record.ens_key)
          } else if (!correctedIDs.contains(record.ens_key) && !correctedIDs.contains(record.is_key)) {
            val uuid = random.alphanumeric.take(20).mkString("")
            record.groundTruthID = uuid
            correctedIDs.put(record.ens_key, uuid)
            correctedIDs.put(record.is_key, uuid)
          } else {
            throw new Exception(s"Something has gone wrong. We have a corrected id for one of the two ids for the record $record but not both.")
          }
        } else {
          record.groundTruthID = if (record.id_ens.nonEmpty) record.ens_key else record.is_key
        }
    }

    // 2. Loop over record again. Correct the IDs for records with one id in common with a newly mapped key if necessary
    records.filter(_.groundTruthID != null).foreach {
      record =>
        record.groundTruthID = correctedIDs.getOrElse(record.groundTruthID,record.groundTruthID)
    }
    records
  }
  
  def generate(tdInventorFile: File) =
    generateRecords(tdInventorFile).filter(_.groundTruthID != null).map(r => (getPointID(r),r.groundTruthID))

}
