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


package edu.umass.cs.iesl.inventor_disambiguation.data_structures.classification

import edu.umass.cs.iesl.inventor_disambiguation.data_structures.PatentsViewRecord


class IPCR extends PatentsViewRecord {
  
  val uuid = StringSlot("uuid")
  val classificationLevel = StringSlot("classificationLevel")
  val section = StringSlot("section")
  val ipcClass = StringSlot("ipcClass")
  val subclass = StringSlot("subclass")
  val mainGroup = StringSlot("mainGroup")
  val subgroup = StringSlot("subgroup")
  val symbolPosition = StringSlot("symbolPosition")
  val classificationValue = StringSlot("classificationValue")
  val classificationStatus = StringSlot("classificationStatus")
  val clasificationDataSource = StringSlot("classificationDataSource")
  val actionDate = StringSlot("actionDate")
  val ipcVersionIndicator = StringSlot("ipcVersionIndicator")
  val sequence = StringSlot("sequence")
  
  def this(uuid: String, patentID: String, classificationLevel: String, section: String, 
           ipcClass: String, subclass: String, mainGroup: String, subgroup: String,
           symbolPosition: String, classificationValue: String, classificationStatus: String,
           classificationDataSource: String, actionDate: String, ipcVersionIndicator: String,
          sequence: String) = {
    this()
    this.uuid.set(uuid)
    this.patentID.set(patentID)
    this.classificationLevel.set(classificationLevel)
    this.section.set(section)
    this.ipcClass.set(ipcClass)
    this.subclass.set(subclass)
    this.mainGroup.set(mainGroup)
    this.subgroup.set(subgroup)
    this.symbolPosition.set(symbolPosition)
    this.classificationValue.set(classificationValue)
    this.classificationStatus.set(classificationStatus)
    this.clasificationDataSource.set(classificationDataSource)
    this.actionDate.set(actionDate)
    this.ipcVersionIndicator.set(ipcVersionIndicator)
    this.sequence.set(sequence)
  }

  def this(uuid: String, patentID: String, classificationLevel: Option[String], section: Option[String],
           ipcClass: Option[String], subclass: Option[String], mainGroup: Option[String], subgroup: Option[String],
           symbolPosition: Option[String], classificationValue: Option[String], classificationStatus: Option[String],
           classificationDataSource: Option[String], actionDate: Option[String], ipcVersionIndicator: Option[String],
           sequence: String) = {
    this()
    this.uuid.set(uuid)
    this.patentID.set(patentID)
    this.classificationLevel.set(classificationLevel)
    this.section.set(section)
    this.ipcClass.set(ipcClass)
    this.subclass.set(subclass)
    this.mainGroup.set(mainGroup)
    this.subgroup.set(subgroup)
    this.symbolPosition.set(symbolPosition)
    this.classificationValue.set(classificationValue)
    this.classificationStatus.set(classificationStatus)
    this.clasificationDataSource.set(classificationDataSource)
    this.actionDate.set(actionDate)
    this.ipcVersionIndicator.set(ipcVersionIndicator)
    this.sequence.set(sequence)
  }
}

object IPCR extends IPCR