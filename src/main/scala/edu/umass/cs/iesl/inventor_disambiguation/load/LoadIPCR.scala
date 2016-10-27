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

import edu.umass.cs.iesl.inventor_disambiguation.data_structures.classification.IPCR
import edu.umass.cs.iesl.inventor_disambiguation._


object LoadIPCR extends TabSeparatedFileLoader[IPCR] {
  override def parse(split: Array[String]): Option[IPCR] = {
    //uuid    patent_id       classification_level    section ipc_class       subclass        main_group 
    // subgroup        symbol_position classification_value    classification_status   classification_data_source  
    //    action_date     ipc_version_indicator   sequence
    val cleaned = split.map(_.clean().noneIfEmpty.flatMap(_.noneIfNULL))
    val uuid = cleaned(0).get
    val patentId = cleaned(1).get
    val classification_level = cleaned(2)
    val section = cleaned(3)
    val ipc_class = cleaned(4)
    val subclass = cleaned(5)
    val mainGroup = cleaned(6)
    val subgroup = cleaned(7)
    val symbolPosition = cleaned(8)
    val classificationValue = cleaned(9)
    val classificationStatus = cleaned(10)
    val classificationDS = cleaned(11)
    val actionDate = cleaned(12)
    val ipcVersionIndicator = cleaned(13)
    val sequence = cleaned(14).get
    Some(new IPCR(uuid,patentId,classification_level,section,ipc_class,subclass,mainGroup,subgroup,symbolPosition,classificationValue,classificationStatus,classificationDS,actionDate,ipcVersionIndicator,sequence))
  }

  override def skipFirstLine: Boolean = true

  override def expectedLineLengths: Set[Int] = Set(15)
}
