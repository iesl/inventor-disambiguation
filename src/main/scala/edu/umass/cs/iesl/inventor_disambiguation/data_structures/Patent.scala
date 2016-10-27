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

import cc.factorie.app.nlp.lexicon.StopWords
import edu.umass.cs.iesl.inventor_disambiguation._

class Patent extends PatentsViewRecord {
 
  val patentType = StringSlot("patentType")
  val number = StringSlot("number")
  val country = StringSlot("country")
  val date = StringSlot("date")
  val patentAbstract = StringSlot("patentAbstract")
  val title = StringSlot("title")
  val kind = StringSlot("kind")
  val numClaims = StringSlot("numClaims")
  val filename = StringSlot("filename")

  lazy val tokenizedTitleWithoutStopwords: Iterable[String] = title.opt.map(_.removePunctuation.split(" ").toIndexedSeq.filterNot(StopWords.containsWord)).getOrElse(Iterable[String]())
  lazy val tokenizedTitleWithoutStopwordsCounts = tokenizedTitleWithoutStopwords.counts

  def this(patentID: String, patentType: String, number: String, country: String, date: String, patentAbstract: String, title: String, kind: String, numClaims: String, filename: String) = {
    this()
    this.patentID.set(patentID)
    this.patentType.set(patentType)
    this.number.set(number)
    this.country.set(country)
    this.date.set(date)
    this.patentAbstract.set(patentAbstract)
    this.title.set(title)
    this.kind.set(kind)
    this.numClaims.set(numClaims)
    this.filename.set(filename)
 }

}

object Patent extends Patent


