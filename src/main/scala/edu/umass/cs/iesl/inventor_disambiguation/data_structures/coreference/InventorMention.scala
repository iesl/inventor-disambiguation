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


package edu.umass.cs.iesl.inventor_disambiguation.data_structures.coreference

import edu.umass.cs.iesl.inventor_disambiguation.coreference.ReEvaluatingNameProcessor
import edu.umass.cs.iesl.inventor_disambiguation.data_structures._
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.citations.USPatentCitation
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.classification.{CPC, IPCR, NBER, USPC}
import edu.umass.cs.iesl.inventor_disambiguation.db.Datastore
import edu.umass.cs.iesl.inventor_disambiguation._

/**
 * Inventor mentions are the main input to the disambiguation algorithm. These objects
 * are stored in a mongo database and retrieved by their inventor's id.  
 * The mentions contain the different pieces of data pertaining to a particular inventor.
 * Like the other data structures in this project, the inventor mention is descent of the 
 * data type Cubbie (from factorie) which provides easy serialization to Mongo. 
 */
class InventorMention extends PatentsViewRecord{

  val uuid = new StringSlot("uuid")

  val self = new CubbieSlot[Inventor]("self",() => new Inventor())

  // The inventors original name
  val rawName = new CubbieSlot[PersonNameRecord]("rawName", () => new PersonNameRecord())

  val patent = new CubbieSlot[Patent]("patent", () => new Patent())
  val coInventors = new CubbieListSlot[Inventor]("coInventors", () => new Inventor())

  lazy val coInventorLastnames = coInventors.value.flatMap(_.nameLast.opt)
  lazy val coInventorLastnamesBag = coInventorLastnames.counts

  val entityId = new StringSlot("entityId")
  val goldEntityId = new StringSlot("goldEntityId")
  
  // Other data about the patent itself
  
  // more than one app per patent
  val applications = new CubbieListSlot[Application]("applications", () => new Application())
  
  val assignees = new CubbieListSlot[Assignee]("assignees", () => new Assignee())
  
  val claims = new CubbieListSlot[Claim]("claims", () => new Claim())
  
  val lawyers = new CubbieListSlot[Lawyer]("lawyers", () => new Lawyer())
  
  // classifications
  
  val cpc = new CubbieListSlot[CPC]("cpc", () => new CPC())
  
  val ipcr = new CubbieListSlot[IPCR]("ipcr", () => new IPCR())
  
  val nber = new CubbieListSlot[NBER]("nber", () => new NBER())

  val uspc = new CubbieListSlot[USPC]("uspc", () => new USPC())
  
  // Citations
  
  val usPatentCitations = new CubbieListSlot[USPatentCitation]("uspatentcitations", () => new USPatentCitation())

  lazy val isFirstInventor: Boolean = self.value.sequenceInt == 0

  lazy val isLastInventor: Boolean = {
    val maxIndex = coInventors.value.map(_.sequenceInt).maxBySafe(f => f)
    maxIndex.isDefined && maxIndex.get < self.value.sequenceInt
  }

  def this(self: Inventor, patent: Patent, coInventors: Seq[Inventor]) = {
    this()
    this.self.set(self)
    this.uuid.set(self.inventorID.opt)
    this.patent.set(patent)
    this.coInventors.set(coInventors)
  }
}

/**
 * Mentions to generate an inventor mention from Datastores.
 */
object InventorMention {

  def fromDatastores(self: Inventor, patentDB: Datastore[String,Patent], inventorDB: Datastore[String,Inventor]): InventorMention = {

    val patentID = self.patentID.value
    val maybePatent = patentDB.get(patentID)
    if (maybePatent.isEmpty)
      println(s"[${this.getClass.getSimpleName}] WARNING:  We must have a patent for the inventor ${self.uuid.value}, ${self.nameFirst.value}, ${self.nameLast.value} with patent ${self.patentID.value}")
    val mention = new InventorMention()
    mention.self.set(self)

    // Take the raw name split it up into middle and suffixes
    mention.rawName.set(new PersonNameRecord(self.nameFirst.opt,self.nameMiddles.opt,self.nameLast.opt,self.nameSuffixes.opt))
    ReEvaluatingNameProcessor.process(mention.rawName.value)

    mention.uuid.set(self.inventorID.opt)
    mention.patent.set(maybePatent.headOption)
    val coInventors = inventorDB.get(patentID).filter(_.inventorID.value != self.inventorID.value)
    mention.coInventors.set(coInventors.toSeq)
  }

  def fromDatastores(self: Inventor, patentDB: Datastore[String,Patent], inventorDB: Datastore[String,Inventor], uspcDB: Datastore[String,USPC]): InventorMention = {
    val mention = fromDatastores(self,patentDB,inventorDB)
    mention.uspc.set(uspcDB.get(self.patentID.value).toList)
    mention
  }


  def fromDatastores(self: Inventor,
                     assigneeDB: Datastore[String,Assignee],
                     cpcDB: Datastore[String,CPC],
                     inventorDB: Datastore[String,Inventor],
                     ipcrDB: Datastore[String,IPCR],
                     lawyerDB: Datastore[String,Lawyer],
                     locationDB: Datastore[String, Location],
                     nberDB: Datastore[String,NBER],
                     patentDB: Datastore[String,Patent],
                     uspcDB: Datastore[String,USPC]): InventorMention = {

    val mention = fromDatastores(self,patentDB,inventorDB)
    mention.assignees.set(assigneeDB.get(self.patentID.value).toList)
    mention.cpc.set(cpcDB.get(self.patentID.value).toList)
    mention.ipcr.set(ipcrDB.get(self.patentID.value).toList)
    mention.lawyers.set(lawyerDB.get(self.patentID.value).toList)

    // Set the self inventor's location:
    mention.self.value.rawLocationID.opt.foreach {
      locId =>
        mention.self.value.location.set(locationDB.get(locId).headOption)
    }

    // Set the co-inventor's locations:
    mention.coInventors.value.foreach( inv => inv.rawLocationID.opt.foreach {
      locId =>
        inv.location.set(locationDB.get(locId).headOption)
    })

    mention.nber.set(nberDB.get(self.patentID.value).toList)
    mention.cpc.set(cpcDB.get(self.patentID.value).toList)
    mention.uspc.set(uspcDB.get(self.patentID.value).toList)
    mention
  }

}




