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


package edu.umass.cs.iesl.inventor_disambiguation.process

import java.io.{PrintWriter, File}

import cc.factorie.util.DefaultCmdOptions
import edu.umass.cs.iesl.inventor_disambiguation.coreference.BasicCorefOutputRecord
import edu.umass.cs.iesl.inventor_disambiguation.load.LoadBasicCorefOutputRecord
import edu.umass.cs.iesl.inventor_disambiguation.utilities.InventorNameStopWords


class PostProcessRemoveStopwordsOpts extends DefaultCmdOptions {
  val input = new CmdOption[String]("input", "The input file", true)
  val output = new CmdOption[String]("output", "The output file", true)
  val codec = new CmdOption[String]("codec", "UTF-8", "STRING", "The encoding")
}

object PostProcessRemoveStopwords {

  def removeStopwords(record: BasicCorefOutputRecord): BasicCorefOutputRecord = {
    BasicCorefOutputRecord(record.mentionId,record.rawInventorId,record.rawDisambiguatedId,record.firstName,record.middleName,record.lastName,InventorNameStopWords.removeStopwords(record.suffixes))
  }

  def removeStopwords(records: Iterator[BasicCorefOutputRecord]): Iterator[BasicCorefOutputRecord] = records.map(removeStopwords)


  def write(records: Iterator[BasicCorefOutputRecord], file: File, codec: String): Unit = {
    val pw = new PrintWriter(file,codec)
    records.foreach(f => {
      pw.println(f.toString)
      pw.flush()
    })
    pw.close()
  }

  def main(args: Array[String]): Unit = {
    val opts = new PostProcessRemoveStopwordsOpts
    opts.parse(args)
    val withoutStopwords = removeStopwords(LoadBasicCorefOutputRecord.load(new File(opts.input.value), opts.codec.value))
    write(withoutStopwords,new File(opts.output.value), opts.codec.value)
  }

}
