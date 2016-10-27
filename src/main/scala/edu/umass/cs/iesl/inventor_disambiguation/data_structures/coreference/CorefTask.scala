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

/**
 * A coreference task is an object that stores a collection of ids that will by run 
 * by the parallel coreference algorithm. Typically, these ids correspond to a single
 * canopy group 
 * @param name - the name of the task
 * @param ids - the ids in the task
 */
case class CorefTask(name: String, ids: Iterable[String]) {
  assert(!name.contains("\t"), "The task name must not contain a tab")
  assert(!ids.exists(_.contains(",")), "None of the ids may contain commas")
  override def toString: String = {
    name + "\t" + ids.mkString(",")
  }
}

object CorefTask{
  /**
   * Parse a raw string and return a coref task 
   * @param raw - input string
   * @return the corresponding task
   */
  def apply(raw: String): CorefTask = raw.split("\t") match {
    case Array(name,idString) =>
      CorefTask(name,idString.split(","))
  }
}

/**
 * A coref task with the inventor mentions corresponding to the ids
 * @param name - the name of the task
 * @param ids - the ids in the task
 * @param mentions - the mentions
 */
class CorefTaskWithMentions(name: String, ids: Iterable[String], val mentions: Iterable[InventorMention]) extends CorefTask(name,ids)