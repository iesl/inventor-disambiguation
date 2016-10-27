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


package edu.umass.cs.iesl.inventor_disambiguation.utilities

import java.io._
import cc.factorie._

object FileIO {
  
  def merge(inputs: Iterable[File], output: File, codec: String = "UTF-8") = {
    val writer = new PrintWriter(output, codec)
    inputs.foreach(i => new BufferedReader(new InputStreamReader(new FileInputStream(i), codec)).toIterator.foreach {
      line =>
        writer.println(line)
        writer.flush()
    })
    writer.close()
  }

  def deleteFiles(files: Iterable[File]): Unit = 
    files.foreach(_.delete())
  
  def delete(filenames: Iterable[String]): Unit =
    deleteFiles(filenames.map(new File(_)))
}
