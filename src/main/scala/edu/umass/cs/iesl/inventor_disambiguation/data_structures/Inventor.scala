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


package edu.umass.cs.iesl.inventor_disambiguation.data_structures

class Inventor extends PatentsViewRecord with PersonName {
  
  val uuid = StringSlot("uuid")
  val rawLocationID = StringSlot("rawLocationID") 
  val sequence = StringSlot("sequence")
  lazy val sequenceInt = sequence.value.toInt

  // Not in the Inventor Table Schema
  val location = CubbieSlot[Location]("location", () => new Location())
  
  val inventorID = StringSlot("inventorID")

  def this(uuid: String, patentID: String, rawLocationID: String, nameFirst: String, nameLast: String, sequence: String) = {
    this()
    this.uuid.set(uuid)
    this.patentID.set(patentID)
    this.rawLocationID.set(rawLocationID)
    this.nameFirst.set(nameFirst)
    this.nameLast.set(nameLast)
    this.sequence.set(sequence)
    this.inventorID.set(patentID + "-" + sequence)
  }
  def this(uuid: String, patentID: String, rawLocationID: Option[String], nameFirst: Option[String], nameLast: Option[String], sequence: String) = {
    this()
    this.uuid.set(uuid)
    this.patentID.set(patentID)
    this.rawLocationID.set(rawLocationID)
    this.nameFirst.set(nameFirst)
    this.nameLast.set(nameLast)
    this.sequence.set(sequence)
    this.inventorID.set(patentID + "-" + sequence)
  }

  def debugString() = Iterable(this.inventorID.opt.getOrElse("*NULL*"),this.nameLast.opt.getOrElse("*NULL*"),this.nameFirst.opt.getOrElse("*NULL*"),this.nameMiddles.opt.getOrElse(Seq()).mkString(" "),this.nameSuffixes.opt.getOrElse(Seq()).mkString(" "),location.opt.map(_.debugString()).getOrElse("")).mkString("\t")
}

object Inventor extends Inventor
