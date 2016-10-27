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

import edu.umass.cs.iesl.inventor_disambiguation.coreference.BasicCorefOutputRecord

object LoadBasicCorefOutputRecord extends TabSeparatedFileLoader[BasicCorefOutputRecord] {

  /**
   * Whether or not to skip the first line, i.e. if it is a header 
   * @return
   */
  override def skipFirstLine: Boolean = false

  /**
   * How to handle a line that has been split by the delimiter. 
   * This is one of the methods that implementations of this trait 
   * will need to define. 
   * @param split
   * @return
   */
  override def parse(split: Array[String]): Option[BasicCorefOutputRecord] = {
    Some(BasicCorefOutputRecord(split(0),split(1),split(2),split(3),split(4),split(5),split(6),split(7)))
  }

  /**
   * The line lengths (in terms of elements after the split) that are acceptable for the loader. 
   * @return
   */
  override def expectedLineLengths: Set[Int] = Set(8)

  printMessages = false
}
