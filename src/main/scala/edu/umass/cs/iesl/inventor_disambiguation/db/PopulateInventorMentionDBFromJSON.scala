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

package edu.umass.cs.iesl.inventor_disambiguation.db

import java.io.File

import edu.umass.cs.iesl.inventor_disambiguation.data_structures.coreference.InventorMention
import edu.umass.cs.iesl.inventor_disambiguation.load.LoadJSONInventorMentions


class PopulateInventorMentionDBFromJSONOpts extends GeneralPatentDBOpts {
  val jsonFile = new CmdOption[List[String]]("json-file", "Either a single file (parallelized within the file), multiple files (parallelized across files), or a single directory (parallelized across files in the directory)", false)
  val numLines = new CmdOption[Int]("num-lines", "The number of lines in the json file.")
}

object PopulateInventorMentionDBFromJSON {

  def main(args: Array[String]): Unit = {
    val opts = new PopulateInventorMentionDBFromJSONOpts()
    opts.parse(args)


    val inputFiles = opts.jsonFile.value.map(new File(_))

    val mentions: Iterable[Iterator[InventorMention]] = if (inputFiles.length == 1) {
      if (inputFiles.head.isDirectory) {
        LoadJSONInventorMentions.fromDir(inputFiles.head,opts.codec.value)
      } else {
        LoadJSONInventorMentions.loadMultiple(inputFiles.head,opts.codec.value,opts.numThreads.value, if (opts.numLines.wasInvoked) Some(opts.numLines.value) else None)
      }
    } else {
      LoadJSONInventorMentions.fromFiles(inputFiles,opts.codec.value)
    }
    val db = new InventorMentionDB(opts.hostname.value,opts.port.value,opts.dbname.value,opts.collectionName.value,false)

    CreateInventorMentionDB.insertPar(mentions,db,opts.bufferSize.value)
    db.addIndices()

  }
}


