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

import java.io.File

import edu.umass.cs.iesl.inventor_disambiguation._
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.coreference.InventorMention
import edu.umass.cs.iesl.inventor_disambiguation.utilities.PatentJsonSerialization
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization

object LoadJSONInventorMentions {

  implicit val formats = Serialization.formats(NoTypeHints)

  def loadLine(string: String): Option[InventorMention] = PatentJsonSerialization.fromJsonString(string, () => new InventorMention())

  def loadLines(lines: Iterator[String]) = lines.flatMap(loadLine)

  def load(file: File, codec: String) = {
    println(s"[LoadJSONInventorMentions] Loading from ${file.getAbsolutePath}")
    loadLines(file.lines(codec))
  }

  def load(file: File, codec: String, start: Int, end: Int) = loadLines(file.lines(codec,start,end))

  def load(file: File, codec: String, start: Int) = loadLines(file.lines(codec,start))

  def loadMultiple(file: File, codec: String, numThreads: Int, numLines: Option[Int] = None) = {
    val start = System.currentTimeMillis()
    val numLinesInFile = if (numLines.nonEmpty) numLines.get else file.numLines
    val end = System.currentTimeMillis()
    println(s"[${this.getClass.getCanonicalName}] There are $numLinesInFile in ${file.getName} (found in ${end-start} ms)")
    val blockSize = numLinesInFile/numThreads
    println(s"[${this.getClass.getCanonicalName}] Each of the $numThreads iterators will have about $blockSize items")
    val startingIndices = (0 until numThreads).map(_ * blockSize)
    startingIndices.dropRight(1).zip(startingIndices.drop(1)).map(f => load(file,codec,f._1,f._2)) ++ Iterable(load(file,codec,startingIndices.last))
  }

  def fromFiles(files: Iterable[File], codec: String) = files.map(f => load(f,codec))

  def fromDir(dir: File,codec: String) = {
    println(s"[LoadJSONInventorMentions] Loading from directory ${dir.getAbsolutePath}")
    fromFiles(dir.listFiles.filterNot(_.getName.startsWith("\\.")),codec)
  }

}
