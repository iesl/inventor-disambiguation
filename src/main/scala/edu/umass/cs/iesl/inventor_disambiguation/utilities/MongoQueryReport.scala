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

import java.io.PrintWriter

import edu.umass.cs.iesl.inventor_disambiguation.coreference.CaseInsensitiveReEvaluatingNameProcessor
import edu.umass.cs.iesl.inventor_disambiguation.db.{InventorMentionDB, MongoDBOpts}

// Given inventor mention ids get an html page of the output

class MongoQueryReportOpts extends MongoDBOpts {
  val ids = new CmdOption[List[String]]("ids", "The ids to query for", true)
  val outputFilename = new CmdOption[String]("output-filename", "The output file", true)
}


object MongoQueryReport {

  def main(args: Array[String]): Unit = {
    val opts = new MongoQueryReportOpts()
    opts.parse(args)
    
    val db = new InventorMentionDB(opts.hostname.value,opts.port.value,opts.dbname.value,opts.collectionName.value,false)
    val ds = db.toDatastoreByUUID
    
    val rawMentions = opts.ids.value.flatMap(ds.get)
    val mentions =
      rawMentions.map {
        m =>
          CaseInsensitiveReEvaluatingNameProcessor.process(m.self.value)
          m.coInventors.opt.map(_.foreach(CaseInsensitiveReEvaluatingNameProcessor.process))
          m.lawyers.opt.map(_.foreach(CaseInsensitiveReEvaluatingNameProcessor.process))
          m.assignees.opt.map(_.foreach(CaseInsensitiveReEvaluatingNameProcessor.process))
          m
      }
    val html = mentions.map(_.toHTMLFormattedString)
    val pw = new PrintWriter(opts.outputFilename.value,"UTF-8")
    pw.println(htmlOutput(html))
    pw.close()
  }
  
  def htmlOutput(htmlStrings: Iterable[String]) = {
    val sb = new StringBuilder
    sb.append("<html>")
    sb.append("<body>")
    htmlStrings.foreach(h => {sb.append(h); sb.append("<br><br>\n\n")})
    sb.append("</body>")
    sb.append("</html>")
    sb.toString()
  }


}
