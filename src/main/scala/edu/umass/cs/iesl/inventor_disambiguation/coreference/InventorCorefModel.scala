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

import cc.factorie.app.bib.hcoref.AuthorModelOptions
import cc.factorie.app.nlp.hcoref.{BagOfWordsEntropy, BagOfWordsSizePrior, ChildParentCosineDistance, CorefModel, DebuggableModel, DenseBagOfWordsEntropy, DenseCosineDistance, EmptyBagPenalty, EntitySizePrior}

/**
 * This class describes the disambiguation model which is used. It specifies the feature templates and the variables they act over.
 */
class InventorCorefModel extends CorefModel[InventorVars] with DebuggableModel[InventorVars]

object InventorCorefModel {
  def fromCmdOptions(opts:InventorModelOptions):InventorCorefModel = {
    implicit val inventorCorefModel = new InventorCorefModel
    
    // inventor names
    if(opts.bagFirstWeight.value != 0.0)inventorCorefModel += new SizeLimitingEntityNameTemplate(opts.bagFirstInitialWeight.value,opts.bagFirstNameWeight.value,opts.bagFirstWeight.value,opts.bagFirstSaturation.value,opts.bagFirstNoNamePenalty.value,opts.bagFirstSizeLimit.value,opts.bagFirstExceedSizeLimitPenalty.value, {b:InventorVars => b.firstNames}, "first initial")
    if(opts.bagMiddleWeight.value != 0.0)inventorCorefModel += new SizeLimitingEntityNameTemplate(opts.bagMiddleInitialWeight.value,opts.bagMiddleNameWeight.value,opts.bagMiddleWeight.value,opts.bagMiddleSaturation.value,opts.bagMiddleNoNamePenalty.value,opts.bagMiddleSizeLimit.value,opts.bagMiddleExceedSizeLimitPenalty.value, {b:InventorVars => b.middleNames}, "middle initial")
    if(opts.bagFirstNoNamePenalty.value != 0.0)inventorCorefModel += new EmptyBagPenalty(opts.bagFirstNoNamePenalty.value, {b:InventorVars => b.firstNames}, "first initial")
    if(opts.bagMiddleNoNamePenalty.value != 0.0)inventorCorefModel += new EmptyBagPenalty(opts.bagMiddleNoNamePenalty.value, {b:InventorVars => b.middleNames}, "middle initial")
    
    // coInventor 
    if(opts.bagCoInventorWeight.value != 0.0)inventorCorefModel += new ChildParentCosineDistance(opts.bagCoInventorWeight.value,opts.bagCoInventorShift.value, {b:InventorVars => b.coInventors}, "coInventors")
    if(opts.bagCoInventorEntropy.value != 0.0)inventorCorefModel += new BagOfWordsEntropy(opts.bagCoInventorEntropy.value, {b:InventorVars => b.coInventors}, "coInventors")
    if(opts.bagCoInventorPrior.value != 0.0)inventorCorefModel += new BagOfWordsSizePrior(opts.bagCoInventorPrior.value, {b:InventorVars => b.coInventors}, "coInventors")
    
    
    // locations
    if(opts.bagLocationsEntropy.value != 0.0)inventorCorefModel += new BagOfWordsEntropy(opts.bagLocationsEntropy.value, {b:InventorVars => b.locations}, "locations")
    if(opts.bagLocationsWeight.value != 0.0)inventorCorefModel += new ChildParentCosineDistance(opts.bagLocationsWeight.value,opts.bagLocationsShift.value, {b:InventorVars => b.locations}, "locations")
    if(opts.bagLocationsPrior.value != 0.0)inventorCorefModel += new BagOfWordsSizePrior(opts.bagLocationsPrior.value, {b:InventorVars => b.locations}, "locations")
    
    // Title Emb
    if(opts.entitySizeWeight.value != 0.0)inventorCorefModel += new EntitySizePrior(opts.entitySizeWeight.value,opts.entitySizeExponent.value)
    if(opts.bagTopicsWeight.value != 0.0)inventorCorefModel += new DenseCosineDistance(opts.bagTopicsWeight.value,opts.bagTopicsShift.value, {b:InventorVars => b.titleEmb}, "title embedding")
    if(opts.bagTopicsEntropy.value != 0.0)inventorCorefModel += new DenseBagOfWordsEntropy(opts.bagTopicsEntropy.value, {b:InventorVars => b.titleEmb}, "title embedding")


    // Keywords TODO: Grab TFIDF
    if(opts.bagKeywordsWeight.value != 0.0)inventorCorefModel += new ChildParentCosineDistance(opts.bagKeywordsWeight.value,opts.bagKeywordsShift.value, {b:InventorVars => b.keywords}, "keywords")
    if(opts.bagKeywordsEntropy.value != 0.0)inventorCorefModel += new BagOfWordsEntropy(opts.bagKeywordsEntropy.value, {b:InventorVars => b.keywords}, "keywords")
    if(opts.bagKeywordsPrior.value != 0.0)inventorCorefModel += new BagOfWordsSizePrior(opts.bagKeywordsPrior.value, {b:InventorVars => b.keywords}, "keywords")


    // Assignees
    if(opts.bagAssigneesWeight.value != 0.0)inventorCorefModel += new ChildParentCosineDistance(opts.bagAssigneesWeight.value,opts.bagAssigneesShift.value, {b:InventorVars => b.assignees}, "Assignees")
    if(opts.bagAssigneesEntropy.value != 0.0)inventorCorefModel += new BagOfWordsEntropy(opts.bagAssigneesEntropy.value, {b:InventorVars => b.assignees}, "Assignees")
    if(opts.bagAssigneesPrior.value != 0.0)inventorCorefModel += new BagOfWordsSizePrior(opts.bagAssigneesPrior.value, {b:InventorVars => b.assignees}, "Assignees")
    
    // Lawyers
    if(opts.bagLawyersWeight.value != 0.0)inventorCorefModel += new ChildParentCosineDistance(opts.bagLawyersWeight.value,opts.bagLawyersShift.value, {b:InventorVars => b.lawyers}, "Lawyers")
    if(opts.bagLawyersEntropy.value != 0.0)inventorCorefModel += new BagOfWordsEntropy(opts.bagLawyersEntropy.value, {b:InventorVars => b.lawyers}, "Lawyers")
    if(opts.bagLawyersPrior.value != 0.0)inventorCorefModel += new BagOfWordsSizePrior(opts.bagLawyersPrior.value, {b:InventorVars => b.lawyers}, "Lawyers")

    // CPC
    if(opts.bagCPCsWeight.value != 0.0)inventorCorefModel += new ChildParentCosineDistance(opts.bagCPCsWeight.value,opts.bagCPCsShift.value, {b:InventorVars => b.cpc}, "cpcs")
    if(opts.bagCPCsEntropy.value != 0.0)inventorCorefModel += new BagOfWordsEntropy(opts.bagCPCsEntropy.value, {b:InventorVars => b.cpc}, "cpcs")
    if(opts.bagCPCsPrior.value != 0.0)inventorCorefModel += new BagOfWordsSizePrior(opts.bagCPCsPrior.value, {b:InventorVars => b.cpc}, "cpcs")

    // IPCR
    if(opts.bagIPCRsWeight.value != 0.0)inventorCorefModel += new ChildParentCosineDistance(opts.bagIPCRsWeight.value,opts.bagIPCRsShift.value, {b:InventorVars => b.ipcr}, "IPCRs")
    if(opts.bagIPCRsEntropy.value != 0.0)inventorCorefModel += new BagOfWordsEntropy(opts.bagIPCRsEntropy.value, {b:InventorVars => b.ipcr}, "IPCRs")
    if(opts.bagIPCRsPrior.value != 0.0)inventorCorefModel += new BagOfWordsSizePrior(opts.bagIPCRsPrior.value, {b:InventorVars => b.ipcr}, "IPCRs")
        
    // USPC
    if(opts.bagUSPCsWeight.value != 0.0)inventorCorefModel += new ChildParentCosineDistance(opts.bagUSPCsWeight.value,opts.bagUSPCsShift.value, {b:InventorVars => b.uspc}, "USPCs")
    if(opts.bagUSPCsEntropy.value != 0.0)inventorCorefModel += new BagOfWordsEntropy(opts.bagUSPCsEntropy.value, {b:InventorVars => b.uspc}, "USPCs")
    if(opts.bagUSPCsPrior.value != 0.0)inventorCorefModel += new BagOfWordsSizePrior(opts.bagUSPCsPrior.value, {b:InventorVars => b.uspc}, "USPCs")
    
    // NBER
    if(opts.bagNBERsWeight.value != 0.0)inventorCorefModel += new ChildParentCosineDistance(opts.bagNBERsWeight.value,opts.bagNBERsShift.value, {b:InventorVars => b.nber}, "NBERs")
    if(opts.bagNBERsEntropy.value != 0.0)inventorCorefModel += new BagOfWordsEntropy(opts.bagNBERsEntropy.value, {b:InventorVars => b.nber}, "NBERs")
    if(opts.bagNBERsPrior.value != 0.0)inventorCorefModel += new BagOfWordsSizePrior(opts.bagNBERsPrior.value, {b:InventorVars => b.nber}, "NBERs")

    inventorCorefModel
  }
}

// TODO: We probably don't want this to extend authorModelOptions
trait InventorModelOptions extends AuthorModelOptions {
  
  // Name template 
  val bagFirstSizeLimit = new CmdOption[Int]("model-author-bag-first-size-limit", 30, "N", "The number of names in the first name bag that can exist without penalty")
  val bagMiddleSizeLimit = new CmdOption[Int]("model-author-bag-middle-size-limit", 30, "N", "The number of names in the first name bag that can exist without penalty")
  val bagFirstExceedSizeLimitPenalty = new CmdOption[Double]("model-author-bag-first-exceed-size-limit-penalty", 100, "N", "The penalty for exceeding the size limit of the first name bag.")
  val bagMiddleExceedSizeLimitPenalty = new CmdOption[Double]("model-author-bag-middle-exceed-size-limit-penalty", 100, "N", "The penalty for exceeding the size limit of the middle name bag.")

  //co-inventors
  val bagCoInventorWeight = new CmdOption("model-author-bag-coinventors-weight", 4.0, "N", "Penalty for bag-of-co-authors cosine distance template (author coreference model).")
  val bagCoInventorShift = new CmdOption("model-author-bag-coinventors-shift", -0.125, "N", "Shift for bag-of-co-authors cosine distance template  (author coreference model).")
  val bagCoInventorEntropy = new CmdOption("model-author-bag-coinventors-entropy", 0.125, "N", "Penalty on bag-of-co-author entropy (author coreference model).")
  val bagCoInventorPrior = new CmdOption("model-author-bag-coinventors-prior", 0.25, "N", "Bag of co-author prior penalty, formula is bag.size/bag.oneNorm*weight.")
  // locations
  val bagLocationsWeight = new CmdOption("model-author-bag-locations-weight", 2.0, "N", "Penalty for bag-of-venues cosine distance template (the author coreference model).")
  val bagLocationsShift = new CmdOption("model-author-bag-locations-shift", -0.125, "N", "Shift for bag-of-venues cosine distance template (author coreference model).")
  val bagLocationsEntropy = new CmdOption("model-author-bag-locations-entropy", 0.125, "N", "Penalty on bag-of-venue entropy (author coreference model).")
  val bagLocationsPrior = new CmdOption("model-author-bag-locations-prior", 0.25, "N", "Bag of co-author prior penalty, formula is bag.size/bag.oneNorm*weight.")

  // assignees
  val bagAssigneesWeight = new CmdOption("model-author-bag-assignees-weight", 4.0, "N", "Penalty for bag-of-assignees cosine distance template (author coreference model).")
  val bagAssigneesShift = new CmdOption("model-author-bag-assignees-shift", -0.125, "N", "Shift for bag-of-assignees cosine distance template  (author coreference model).")
  val bagAssigneesEntropy = new CmdOption("model-author-bag-assignees-entropy", 0.125, "N", "Penalty on bag-of-assignees entropy (author coreference model).")
  val bagAssigneesPrior = new CmdOption("model-author-bag-assignees-prior", 0.25, "N", "Bag of assignees prior penalty, formula is bag.size/bag.oneNorm*weight.")

  // lawyers
  val bagLawyersWeight = new CmdOption("model-author-bag-lawyers-weight", 4.0, "N", "Penalty for bag-of-lawyers cosine distance template (author coreference model).")
  val bagLawyersShift = new CmdOption("model-author-bag-lawyers-shift", -0.125, "N", "Shift for bag-of-lawyers cosine distance template  (author coreference model).")
  val bagLawyersEntropy = new CmdOption("model-author-bag-lawyers-entropy", 0.125, "N", "Penalty on bag-of-lawyers entropy (author coreference model).")
  val bagLawyersPrior = new CmdOption("model-author-bag-lawyers-prior", 0.25, "N", "Bag of lawyers prior penalty, formula is bag.size/bag.oneNorm*weight.")

  // cpc
  val bagCPCsWeight = new CmdOption("model-author-bag-cpc-weight", 2.0, "N", "Penalty for bag-of-venues cosine distance template (the author coreference model).")
  val bagCPCsShift = new CmdOption("model-author-bag-cpc-shift", -0.125, "N", "Shift for bag-of-venues cosine distance template (author coreference model).")
  val bagCPCsEntropy = new CmdOption("model-author-bag-cpc-entropy", 0.125, "N", "Penalty on bag-of-venue entropy (author coreference model).")
  val bagCPCsPrior = new CmdOption("model-author-bag-cpc-prior", 0.25, "N", "Bag of co-author prior penalty, formula is bag.size/bag.oneNorm*weight.")

  // ipcr
  val bagIPCRsWeight = new CmdOption("model-author-bag-ipcr-weight", 2.0, "N", "Penalty for bag-of-venues cosine distance template (the author coreference model).")
  val bagIPCRsShift = new CmdOption("model-author-bag-ipcr-shift", -0.125, "N", "Shift for bag-of-venues cosine distance template (author coreference model).")
  val bagIPCRsEntropy = new CmdOption("model-author-bag-ipcr-entropy", 0.125, "N", "Penalty on bag-of-venue entropy (author coreference model).")
  val bagIPCRsPrior = new CmdOption("model-author-bag-ipcr-prior", 0.25, "N", "Bag of co-author prior penalty, formula is bag.size/bag.oneNorm*weight.")

  // uspc
  val bagUSPCsWeight = new CmdOption("model-author-bag-uspc-weight", 2.0, "N", "Penalty for bag-of-venues cosine distance template (the author coreference model).")
  val bagUSPCsShift = new CmdOption("model-author-bag-uspc-shift", -0.125, "N", "Shift for bag-of-venues cosine distance template (author coreference model).")
  val bagUSPCsEntropy = new CmdOption("model-author-bag-uspc-entropy", 0.125, "N", "Penalty on bag-of-venue entropy (author coreference model).")
  val bagUSPCsPrior = new CmdOption("model-author-bag-uspc-prior", 0.25, "N", "Bag of co-author prior penalty, formula is bag.size/bag.oneNorm*weight.")
  
  // nber
  val bagNBERsWeight = new CmdOption("model-author-bag-nber-weight", 2.0, "N", "Penalty for bag-of-venues cosine distance template (the author coreference model).")
  val bagNBERsShift = new CmdOption("model-author-bag-nber-shift", -0.125, "N", "Shift for bag-of-venues cosine distance template (author coreference model).")
  val bagNBERsEntropy = new CmdOption("model-author-bag-nber-entropy", 0.125, "N", "Penalty on bag-of-venue entropy (author coreference model).")
  val bagNBERsPrior = new CmdOption("model-author-bag-nber-prior", 0.25, "N", "Bag of co-author prior penalty, formula is bag.size/bag.oneNorm*weight.")

}

