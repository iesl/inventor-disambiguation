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

import edu.umass.cs.iesl.inventor_disambiguation.data_structures.citations.USPatentCitation

import edu.umass.cs.iesl.inventor_disambiguation._

object LoadUSPatentCitation extends TabSeparatedFileLoader[USPatentCitation] {
  override def parse(split: Array[String]): Option[USPatentCitation] = {
//    uuid    patent_id       citation_id     date    name    kind    country category        sequence
    val cleaned = split.map(_.clean().noneIfEmpty.flatMap(_.noneIfNULL))
    val uuid = cleaned(0).get
    val patent_id = cleaned(1).get
    val citation_id = cleaned(2)
    val date = cleaned(3).flatMap(_.noneIfEmptyDate)
    val name = cleaned(4)
    val kind = cleaned(5)
    val country = cleaned(6)
    val category = cleaned(7)
    val sequence = cleaned(8).get
    Some(new USPatentCitation(uuid,patent_id,citation_id,date,name,kind,country,category,sequence))
  }

  override def skipFirstLine: Boolean = true

  override def expectedLineLengths: Set[Int] = Set(9)
}
