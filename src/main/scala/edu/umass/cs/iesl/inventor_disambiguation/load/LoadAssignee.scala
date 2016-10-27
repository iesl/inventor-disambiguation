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


package edu.umass.cs.iesl.inventor_disambiguation.load

import edu.umass.cs.iesl.inventor_disambiguation.data_structures.Assignee

import edu.umass.cs.iesl.inventor_disambiguation._

object LoadAssignee extends TabSeparatedFileLoader[Assignee] {


  //"uuid","patent_id","rawlocation_id","type","name_first","name_last","organization","sequence"
  override def parse(split: Array[String]): Option[Assignee] = {
    val uuid = split(0).clean().noneIfEmpty
    val patentID = split(1).clean().noneIfEmpty
    //TODO: Add this field to the Assignee class
    //val assigneeID = split(2).clean().noneIfEmpty
    val locID = split(3).clean().noneIfEmpty
    val typ = split(4).noneIfNAorBlank.map(_.clean())
    val first = split(5).noneIfNAorBlank.map(_.clean())
    val last = split(6).noneIfNAorBlank.map(_.clean())
    val org = split(7).noneIfNAorBlank.map(_.clean())
    val seq = split(8).clean().noneIfEmpty
    Some(new Assignee(uuid.get,patentID.get,locID.get,typ,first,last,org,seq.get))
  }
  override def skipFirstLine: Boolean = true

  override def expectedLineLengths: Set[Int] = Set(9)
}
