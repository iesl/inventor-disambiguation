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

import java.io.{BufferedWriter, File, FileOutputStream, OutputStreamWriter}
import java.util.zip.GZIPOutputStream

import edu.umass.cs.iesl.inventor_disambiguation.data_structures.classification.{CPC, IPCR, NBER, USPC}
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.coreference.InventorMention
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.{Assignee, Inventor, Lawyer, Patent}
import edu.umass.cs.iesl.inventor_disambiguation.db.{CreateInventorMentionDBOpts, GeneralPatentDB, InventorMentionDB, LocationDB}
import edu.umass.cs.iesl.inventor_disambiguation.load.LoadInventor
import edu.umass.cs.iesl.inventor_disambiguation.utilities.PatentJsonSerialization

class CreateInventorMentionJSONOpts extends CreateInventorMentionDBOpts {
  val jsonFile = new CmdOption[String]("json-file", "Where to write the json output.")
}


object CreateInventorMentionJSON {

  def main(args: Array[String]): Unit = {

    val opts = new CreateInventorMentionJSONOpts
    opts.parse(args)

    val assigneeDB = new GeneralPatentDB[Assignee](opts.hostname.value, opts.port.value, opts.dbname.value, opts.assigneeCollectionName.value, true) {
      override def constructor(): Assignee = new Assignee()
    }

    val cpcDB = new GeneralPatentDB[CPC](opts.hostname.value, opts.port.value, opts.dbname.value, opts.cpcCollectionName.value, true) {
      override def constructor(): CPC = new CPC()
    }

    val inventorDB = new GeneralPatentDB[Inventor](opts.hostname.value, opts.port.value, opts.dbname.value, opts.inventorCollectionName.value, true) {
      override def constructor(): Inventor = new Inventor()
    }

    val ipcrDB = new GeneralPatentDB[IPCR](opts.hostname.value, opts.port.value, opts.dbname.value, opts.ipcrCollectionName.value, true) {
      override def constructor(): IPCR = new IPCR()
    }

    val lawyerDB = new GeneralPatentDB[Lawyer](opts.hostname.value, opts.port.value, opts.dbname.value, opts.lawyerCollectionName.value, true) {
      override def constructor(): Lawyer = new Lawyer()
    }

    val locationDB = new LocationDB(opts.hostname.value, opts.port.value, opts.dbname.value, opts.locationCollectionName.value, true)

    val nberDB = new GeneralPatentDB[NBER](opts.hostname.value, opts.port.value, opts.dbname.value, opts.nberCollectionName.value, true) {
      override def constructor(): NBER = new NBER()
    }

    val patentDB = new GeneralPatentDB[Patent](opts.hostname.value, opts.port.value, opts.dbname.value, opts.patentCollectionName.value, true) {
      override def constructor(): Patent = new Patent()
    }

    val uspcDB = new GeneralPatentDB[USPC](opts.hostname.value, opts.port.value, opts.dbname.value, opts.uspcCollectionName.value, true) {
      override def constructor(): USPC = new USPC()
    }

    val inventorsPar = LoadInventor.loadMultiple(new File(opts.inventorFile.value),opts.codec.value,opts.numThreads.value)
    val db = new InventorMentionDB(opts.hostname.value, opts.port.value, opts.dbname.value, opts.collectionName.value, false)

    val output = if (opts.jsonFile.value.endsWith(".gz"))
      new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(opts.jsonFile.value))))
    else
      new BufferedWriter(new OutputStreamWriter(new FileOutputStream(opts.jsonFile.value)))
    var count = 0
    inventorsPar.foreach(
      inventors => {
        val inventorMentions = inventors.map(inventor => InventorMention.fromDatastores(inventor, assigneeDB, cpcDB, inventorDB, ipcrDB, lawyerDB, locationDB, nberDB, patentDB, uspcDB))
        inventorMentions.foreach{
          im =>
            if (count % 100 == 0)
            print(s"\rWrote: $count lines.")
            output.write(PatentJsonSerialization.toJsonString(im))
            output.write("\n")
            count += 1
        }
      }
    )
    output.close()
  }

}