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

import edu.umass.cs.iesl.inventor_disambiguation.data_structures.classification.{USPC, NBER, IPCR, CPC}
import edu.umass.cs.iesl.inventor_disambiguation.data_structures._
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.coreference.InventorMention
import edu.umass.cs.iesl.inventor_disambiguation.load.LoadJSONInventorMentions
import edu.umass.cs.iesl.inventor_disambiguation.utilities.PatentJsonSerialization

/**
  * Remove all fields from inventor mentions
  * that are not used in the algorithm.
  */
object RemoveUnusedFields {

  def reduce(im: InventorMention) = {
    val reduced = new InventorMention()
    reduced.uuid.set(im.uuid.value)
    reduced.assignees.set(im.assignees.opt.getOrElse(Seq()).map(a => reduceAssignee(a)))
    reduced.coInventors.set(im.coInventors.opt.getOrElse(Seq()).map(c => reduceCoInventor(c)))
    reduced.cpc.set(im.cpc.opt.getOrElse(Seq()).map(c => reduceCPC(c)))
    reduced.ipcr.set(im.ipcr.opt.getOrElse(Seq()).map(i => reduceIPCR(i)))
    reduced.lawyers.set(im.lawyers.opt.getOrElse(Seq()).map(i => reduceLawyer(i)))
    reduced.nber.set(im.nber.opt.getOrElse(Seq()).map(n => reduceNBER(n)))
    reduced.patent.set(reducePatent(im.patent.value))
    reduced.self.set(reduceSelf(im.self.value))
    reduced.uspc.set(im.uspc.opt.getOrElse(Seq()).map(n => reduceUSPC(n)))
    reduced
  }

  def reduceAssignee(a: Assignee) = {
    val reduced = new Assignee()
    reduced.organization.set(a.organization.opt)
    reduced.nameFirst.set(a.nameFirst.opt)
    reduced.nameLast.set(a.nameLast.opt)
    reduced
  }

  def reduceCoInventor(inv: Inventor) = {
    val reduced = new Inventor()
    reduced.nameFirst.set(inv.nameFirst.opt)
    reduced.nameLast.set(inv.nameLast.opt)
    reduced
  }

  def reduceCPC(cpc: CPC) = {
    val reduced = new CPC()
    reduced.sectionID.set(cpc.sectionID.opt)
    reduced.subsectionID.set(cpc.subsectionID.opt)
    reduced
  }

  def reduceIPCR(ipcr: IPCR) = {
    val reduced = new IPCR()
    reduced.classificationLevel.set(ipcr.classificationLevel.opt)
    reduced.ipcClass.set(ipcr.ipcClass.opt)
    reduced.section.set(ipcr.section.opt)
  }

  def reduceLawyer(lawyer: Lawyer) = {
    val reduced = new Lawyer()
    reduced.nameFirst.set(lawyer.nameFirst.opt)
    reduced.nameLast.set(lawyer.nameLast.opt)
    reduced
  }

  def reduceNBER(nber: NBER) = {
    val reduced = new NBER()
    reduced.categoryID.set(nber.categoryID.opt)
    reduced.subcategoryID.set(nber.subcategoryID.opt)
    reduced
  }

  def reducePatent(patent: Patent) = {
    val reduced = new Patent()
    reduced.title.set(patent.title.opt)
    reduced
  }

  def reduceSelf(self: Inventor) = {
    val reduced = new Inventor()
    reduced.uuid.set(self.uuid.opt)
    reduced.inventorID.set(self.inventorID.opt)
    if (self.location.isDefined)
      reduced.location.set(reduceLocation(self.location.value))
    reduced.nameFirst.set(self.nameFirst.opt)
    reduced.nameLast.set(self.nameLast.opt)
    reduced.nameMiddles.set(self.nameMiddles.opt)
    reduced.nameLast.set(self.nameLast.opt)
    reduced
  }

  def reduceLocation(location: Location) = {
    val reduced = new Location()
    reduced.city.set(location.city.opt)
    reduced.state.set(location.state.opt)
    reduced.country.set(location.country.opt)
    reduced
  }

  def reduceUSPC(uspc: USPC) = {
    val reduced = new USPC()
    reduced.mainclassID.set(uspc.mainclassID.opt)
    reduced.subclassID.set(uspc.subclassID.opt)
    reduced
  }


  /**
    * Reduce all of the mentions in the input json file.
    * Write results to JSON.
    * @param args
    */
  def main(args: Array[String]): Unit = {
    val mentions = LoadJSONInventorMentions.load(new File(args(0)),"UTF-8")
    val reduced = mentions.map(reduce)
    val pw = new PrintWriter(args(1),"UTF-8")
    reduced.foreach{
      case r =>
        pw.println(PatentJsonSerialization.toJsonString(r))
    }
    pw.close()
  }
}
