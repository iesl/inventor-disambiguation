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

import cc.factorie.util.Cubbie

/**
 * The PersonName trait gives the Cubbie the fields of first, middle, last, and last name suffixes.
 */
trait PersonName extends Cubbie{

  val nameFirst = StringSlot("nameFirst")
  val nameMiddles = StringListSlot("nameMiddles")
  val nameLast = StringSlot("nameLast")
  val nameSuffixes = StringListSlot("nameSuffixes")

  
  def sameLastName(other: PersonName): Boolean = this.nameLast.opt.isDefined && other.nameLast.opt.isDefined && this.nameLast.value == other.nameLast.value

  def sameFirstName(other: PersonName): Boolean = this.nameFirst.opt.isDefined && other.nameFirst.opt.isDefined && this.nameFirst.value == other.nameFirst.value
  
  def sameMiddleNames(other: PersonName): Boolean = this.nameMiddles.opt.isDefined && other.nameMiddles.opt.isDefined && this.nameMiddles.value.toSet[String] == other.nameMiddles.value.toSet[String]
  
  def sameSuffixes(other: PersonName): Boolean = this.nameSuffixes.opt.isDefined && other.nameSuffixes.opt.isDefined && this.nameSuffixes.value.toSet[String] == other.nameSuffixes.value.toSet[String]
  
  def sameFirstAndLastName(other: PersonName): Boolean = sameFirstName(other) && sameLastName(other)
  
  def sameFullName(other: PersonName): Boolean = sameFirstAndLastName(other) && sameMiddleNames(other)
}

class PersonNameRecord extends PersonName {

  def this(first: String, middles: Seq[String], last: String, suffixes: Seq[String]) = {
    this()
    this.nameFirst.set(first)
    this.nameMiddles.set(middles)
    this.nameLast.set(last)
    this.nameSuffixes.set(suffixes)
  }

  def this(first: Option[String], middles: Option[Seq[String]], last: Option[String],suffixes: Option[Seq[String]]) = {
    this()
    this.nameFirst.set(first)
    this.nameMiddles.set(middles)
    this.nameLast.set(last)
    this.nameSuffixes.set(suffixes)
  }

}