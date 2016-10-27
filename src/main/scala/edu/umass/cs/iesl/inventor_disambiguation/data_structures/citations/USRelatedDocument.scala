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


package edu.umass.cs.iesl.inventor_disambiguation.data_structures.citations

import edu.umass.cs.iesl.inventor_disambiguation.data_structures.PatentsViewRecord

class USRelatedDocument extends PatentsViewRecord{
  
  val uuid = StringSlot("uuid")
  val relatedID = StringSlot("relatedID")
  val doctype = StringSlot("doctype")
  val status = StringSlot("status")
  val date = StringSlot("date")
  val number = StringSlot("number")
  val kind = StringSlot("kind")
  val country = StringSlot("country")
  val relationship = StringSlot("relationship")
  val sequence = StringSlot("sequence")
  
  def this(uuid: String, patentID: String, relatedID: String, doctype: String, status: String, date: String, number: String, kind: String, country: String, relationship: String, sequence: String) = {
    this()
    this.uuid.set(uuid)
    this.patentID.set(patentID)
    this.relatedID.set(relatedID)
    this.doctype.set(doctype)
    this.status.set(status)
    this.date.set(date)
    this.number.set(number)
    this.kind.set(kind)
    this.country.set(country)
    this.relationship.set(relationship)
    this.sequence.set(sequence)
  }
  
}
