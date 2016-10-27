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


class NBER extends PatentsViewRecord {
  
  val uuid = StringSlot("uuid")
  val categoryID = StringSlot("categoryID")
  val subcategoryID = StringSlot("subcategoryID")
  
  
  def this(uuid: String, patentID: String, categoryID: String, subcategoryID: String) = {
    this()
    this.uuid.set(uuid)
    this.patentID.set(patentID)
    this.categoryID.set(categoryID)
    this.subcategoryID.set(subcategoryID)
  }

  def this(uuid: String, patentID: String, categoryID: Option[String], subcategoryID: Option[String]) = {
    this()
    this.uuid.set(uuid)
    this.patentID.set(patentID)
    this.categoryID.set(categoryID)
    this.subcategoryID.set(subcategoryID)
  }
}

object NBER extends NBER