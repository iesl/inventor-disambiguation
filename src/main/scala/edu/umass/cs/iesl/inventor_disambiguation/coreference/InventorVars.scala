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


package edu.umass.cs.iesl.inventor_disambiguation.coreference

import cc.factorie.app.nlp.hcoref.{GroundTruth, NodeVariables}
import cc.factorie.variable.{BagOfWordsVariable, DenseDoubleBagVariable, DiffList, NoopDiff}
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.coreference.InventorMention

/**
 * These are the variables that each node in the coreference tree maintains.
 * @param firstNames
 * @param middleNames
 * @param lastNames
 * @param locations
 * @param applicationIds
 * @param applicationTypes
 * @param applicationCountry
 * @param dates
 * @param patentCountry
 * @param patentTypes
 * @param title
 * @param titleEmb
 * @param keywords
 * @param coInventors
 * @param assignees
 * @param assigneeLocations
 * @param lawyers
 * @param lawyersLocations
 * @param cpc
 * @param ipcr
 * @param uspc
 * @param nber
 * @param canopy
 * @param truth
 * @param source
 */
class InventorVars(
                    // inventors
                    val firstNames: BagOfWordsVariable,
                    val middleNames: BagOfWordsVariable,
                    val lastNames: BagOfWordsVariable,
                    val locations: BagOfWordsVariable,
                    //application
                    val applicationIds: BagOfWordsVariable,
                    val applicationTypes: BagOfWordsVariable,
                    val applicationCountry: BagOfWordsVariable,
                    // Claim
                    // None
                    // Patent
                    val dates: BagOfWordsVariable,
                    val patentCountry: BagOfWordsVariable,
                    val patentTypes: BagOfWordsVariable,
                    val title: BagOfWordsVariable,
                    val titleEmb: DenseDoubleBagVariable,
                    val keywords: BagOfWordsVariable,
                    val coInventors: BagOfWordsVariable,
                    // assignee
                    val assignees: BagOfWordsVariable,
                    val assigneeLocations: BagOfWordsVariable,
                    // lawyers
                    val lawyers: BagOfWordsVariable,
                    val lawyersLocations: BagOfWordsVariable,
                    // CPC
                    val cpc: BagOfWordsVariable,
                    // IPCR
                    val ipcr: BagOfWordsVariable,
                    // USPC
                    val uspc: BagOfWordsVariable,
                    // NBER
                    val nber: BagOfWordsVariable,
                    var canopy: String,
                    val truth: BagOfWordsVariable, val source: String = "") extends NodeVariables[InventorVars] with MutableSingularCanopy with GroundTruth {

  var provenance: Option[InventorMention] = None

  def this(dim: Int) = this(new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new DenseDoubleBagVariable(dim), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), new BagOfWordsVariable(), "", new BagOfWordsVariable(), "")

  def this() = this(200)

  def getVariables = Seq(firstNames, middleNames, lastNames, locations, applicationIds, applicationTypes, applicationCountry, dates, patentCountry, patentTypes, title, titleEmb, keywords, coInventors, assignees, assigneeLocations, lawyers, lawyersLocations, cpc, ipcr, uspc, nber)

  def --=(other: InventorVars)(implicit d: DiffList) = {
    this.firstNames remove other.firstNames.value
    this.middleNames remove other.middleNames.value
    this.lastNames remove other.lastNames.value
    this.locations remove other.locations.value
    this.applicationIds remove other.applicationIds.value
    this.applicationTypes remove other.applicationTypes.value
    this.applicationCountry remove other.applicationCountry.value
    this.dates remove other.dates.value
    this.patentCountry remove other.patentCountry.value
    this.patentTypes remove other.patentTypes.value
    this.title remove other.title.value
    this.titleEmb remove other.titleEmb.value
    this.keywords remove other.keywords.value
    this.coInventors remove other.coInventors.value
    this.assignees remove other.assignees.value
    this.assigneeLocations remove assigneeLocations.value
    this.lawyers remove other.lawyers.value
    this.lawyersLocations remove other.lawyersLocations.value
    this.cpc remove other.cpc.value
    this.ipcr remove other.ipcr.value
    this.uspc remove other.uspc.value
    this.nber remove other.nber.value
    this.truth remove other.truth.value
    if (d ne null) d += NoopDiff(this) // because EntityNameTemplate (and others) have InventorVar as its neighbor, but doesn't have the bags of words as neighbors
  }

  def ++=(other: InventorVars)(implicit d: DiffList) = {
    this.firstNames add other.firstNames.value
    this.middleNames add other.middleNames.value
    this.lastNames add other.lastNames.value
    this.locations add other.locations.value
    this.applicationIds add other.applicationIds.value
    this.applicationTypes add other.applicationTypes.value
    this.applicationCountry add other.applicationCountry.value
    this.dates add other.dates.value
    this.patentCountry add other.patentCountry.value
    this.patentTypes add other.patentTypes.value
    this.title add other.title.value
    this.titleEmb add other.titleEmb.value
    this.keywords add other.keywords.value
    this.coInventors add other.coInventors.value
    this.assignees add other.assignees.value
    this.assigneeLocations add assigneeLocations.value
    this.lawyers add other.lawyers.value
    this.lawyersLocations add other.lawyersLocations.value
    this.cpc add other.cpc.value
    this.ipcr add other.ipcr.value
    this.uspc add other.uspc.value
    this.nber add other.nber.value
    this.truth add other.truth.value
    if (d ne null) d += NoopDiff(this) // because EntityNameTemplate (and others) have InventorVar as its neighbor, but doesn't have the bags of words as neighbors
  }


  def --(other: InventorVars
          )(implicit d: DiffList) = {
    new InventorVars(this.firstNames -- other.firstNames, this.middleNames -- other.middleNames, this.lastNames -- other.lastNames, this.locations -- other.locations, this.applicationIds -- other.applicationIds, this.applicationTypes -- other.applicationTypes, this.applicationCountry -- other.applicationCountry, this.dates -- other.dates, this.patentCountry -- other.patentCountry, this.patentTypes -- other.patentTypes, this.title -- other.title, this.titleEmb -- other.titleEmb, this.keywords -- other.keywords, this.coInventors -- other.coInventors, this.assignees -- other.assignees, this.assigneeLocations -- assigneeLocations, this.lawyers -- other.lawyers, this.lawyersLocations -- other.lawyersLocations, this.cpc -- other.cpc, this.ipcr -- other.ipcr, this.uspc -- other.uspc, this.nber -- other.nber, this.canopy,
      this.truth -- other.truth)
  }

  def ++(other: InventorVars)(implicit d: DiffList) = {
    new InventorVars(this.firstNames ++ other.firstNames, this.middleNames ++ other.middleNames, this.lastNames ++ other.lastNames, this.locations ++ other.locations, this.applicationIds ++ other.applicationIds, this.applicationTypes ++ other.applicationTypes, this.applicationCountry ++ other.applicationCountry, this.dates ++ other.dates, this.patentCountry ++ other.patentCountry, this.patentTypes ++ other.patentTypes, this.title ++ other.title, this.titleEmb ++ other.titleEmb, this.keywords ++ other.keywords, this.coInventors ++ other.coInventors, this.assignees ++ other.assignees, this.assigneeLocations ++ assigneeLocations, this.lawyers ++ other.lawyers, this.lawyersLocations ++ other.lawyersLocations, this.cpc ++ other.cpc, this.ipcr ++ other.ipcr, this.uspc ++ other.uspc, this.nber ++ other.nber,
      this.canopy,
      this.truth ++ other.truth)
  }
}
