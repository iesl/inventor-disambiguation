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

import cc.factorie.app.nlp.hcoref._
import cc.factorie.variable.{DenseDoubleBagVariable, DiffList, BagOfWordsVariable}
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.Inventor
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.coreference.InventorMention
import edu.umass.cs.iesl.inventor_disambiguation._

import scala.util.Random

/**
 * Runs the coref algorithm on the given mentions using the single canopy already assigned to the mentions 
 * @param opts - the model parameters
 * @param mentions - the mentions ro run coref on. 
 */
class SingleCanopyInventorCorefSystem(opts: InventorModelOptions,override val mentions: Iterable[Mention[InventorVars]]) extends HierarchicalCorefSystem[InventorVars] {

  implicit val random = new Random(0)

  override def model: CorefModel[InventorVars] = InventorCorefModel.fromCmdOptions(opts)

  override def sampler(ments: Iterable[Node[InventorVars]]): CorefSampler[InventorVars] =  new CorefSampler[InventorVars](model, ments, estimateIterations(ments))
    with QuietAutoStoppingSampler[InventorVars]
    with CanopyPairGenerator[InventorVars]
    with InventorCorefMoveGenerator[InventorVars]
    with CorefWatch[InventorVars]
    with DebugCoref[InventorVars] with PrintlnLogger {
    def autoStopThreshold = 10000
    def newInstance(implicit d: DiffList) = new Node[InventorVars](new InventorVars)
  }
}

/**
 * Runs the coref algorithm on the given mentions in the multiple-canopy mode, using the given list of canopy functions 
 * @param opts - the model parameters
 * @param mentions - the mentions to run on
 * @param canopyFunctions - the canopy functions to use
 */
class MultiCanopyInventorCorefSystem(opts: InventorModelOptions,override val mentions: Iterable[Mention[InventorVars]],override val canopyFunctions: Iterable[Inventor => String]) extends MultiCanopy[InventorVars,Inventor] {

  implicit val random = new Random(0)

  override def model: CorefModel[InventorVars] = InventorCorefModel.fromCmdOptions(opts)

  override def sampler(ments: Iterable[Node[InventorVars]]): CorefSampler[InventorVars] =  new CorefSampler[InventorVars](model, ments, estimateIterations(ments))
    with QuietAutoStoppingSampler[InventorVars]
    with CanopyPairGenerator[InventorVars]
    with InventorCorefMoveGenerator[InventorVars]
    with CorefWatch[InventorVars]
    with DebugCoref[InventorVars]  with PrintlnLogger{
    def autoStopThreshold = 10000
    def newInstance(implicit d: DiffList) = new Node[InventorVars](new InventorVars)
  }

  override def getCanopyInput(vars: InventorVars): Inventor = vars.provenance.get.self.value

  override def determineNextRound(mentions: Iterable[Node[InventorVars]]): Iterable[Node[InventorVars]] = mentions.map(_.root).stableRemoveDuplicates()
}


/**
 * This is the base class for running the hierarchical coreference algorithm. It acts as a wrapper, converting the raw 
 * inventor mentions into the node data structures that are used in coreference
 * @param opts - the model parameters
 * @param keystore - the word embedding lookup dictionary
 * @param rawMentions - the mentions as they were stored in the mongo table
 */
abstract class HierarchicalInventorCorefRunner(opts: InventorModelOptions, keystore: Keystore, rawMentions: Iterable[InventorMention]) extends CoreferenceAlgorithm[InventorMention] with IndexableMentions[InventorMention] {


  /**
   * Apply the name processing pipeline  
   */
  override val mentions =
    rawMentions.map {
      m =>
        CaseInsensitiveReEvaluatingNameProcessor.process(m.self.value)
        m.coInventors.opt.map(_.foreach(CaseInsensitiveReEvaluatingNameProcessor.process))
        m.lawyers.opt.map(_.foreach(CaseInsensitiveReEvaluatingNameProcessor.process))
        m.assignees.opt.map(_.foreach(CaseInsensitiveReEvaluatingNameProcessor.process))
        m
    }

  /**
   * mapping of mentions by their id numbers 
   */
  lazy val mentionIndex = mentions.groupBy(_.uuid.value).mapValues(_.head)
  
  override def getMention(id: String): InventorMention = mentionIndex(id)

  /**
   * Convert the mentions into the datatype, InventorVars 
   */
  lazy val inventorVarMentions = mentions.map(toMentionInventorVars)
  
  override lazy val predictedClustering: Iterable[(String, String)] = inventorVarMentions.map(f => (f.uniqueId,f.entity.uniqueId))

  def determineCanopy(inventor: Inventor): String = Canopies.lastNameAndFirstThreeCharsCaseInsensitive(inventor)

  /**
   * Given a single mention, convert it into the InventorVars BoW and embedding format for input into the disambiguation algorithm 
   * @param mention
   * @return
   */
  def toMentionInventorVars(mention: InventorMention) =  {
    // First names
    val firstNames = new BagOfWordsVariable()
    mention.self.value.nameFirst.opt.foreach(f => f.trimBegEnd().noneIfEmpty.foreach(firstNames.add(_)(null)))

    // middle names here
    val middleNames = new BagOfWordsVariable()
    mention.self.value.nameMiddles.opt.foreach(ms => ms.foreach(m => m.trimBegEnd().noneIfEmpty.foreach(middleNames.add(_)(new DiffList))))

    // topics = embedding of title
    val topics = new DenseDoubleBagVariable()
    val titleWords = mention.patent.opt.map(_.title.opt.map(_.alphanumericAndSpacesOnly.split(" ")).getOrElse(Array())).getOrElse(Array())
    if (titleWords.length > 0)
      topics.add(keystore.generateVector(titleWords))(new DiffList)

    val locations = new BagOfWordsVariable()
    mention.self.value.location.opt.map(l => {
      if (l.city.opt.isDefined || l.state.isDefined)
        locations.add((l.city.opt ++ l.state.opt ++ l.country.opt).mkString(","))(new DiffList)
    })


    // coinventors
    val coinventors = new BagOfWordsVariable()
    mention.coInventors.value.foreach {
      coinventor =>
        val name = (coinventor.nameLast.opt ++ coinventor.nameFirst.opt).mkString(" ").trimBegEnd().noneIfEmpty
        name.foreach(coinventors.add(_)(null))
    }

    // assignees
    val assignees = new BagOfWordsVariable()
    mention.assignees.opt.foreach(_.foreach {
      assignee =>
        val name = (assignee.nameLast.opt ++ assignee.nameFirst.opt).mkString(" ").trimBegEnd().noneIfEmpty
        name.foreach(assignees.add(_)(null))
        assignee.organization.opt.foreach(f => f.trimBegEnd().noneIfEmpty.foreach(assignees.add(_)(new DiffList)))
    })

    // lawyers
    val lawyers = new BagOfWordsVariable()
    mention.lawyers.opt.foreach(_.foreach {
      lawyer =>
        val name = (lawyer.nameLast.opt ++ lawyer.nameFirst.opt).mkString(" ").trimBegEnd().noneIfEmpty
        name.foreach(lawyers.add(_)(null))
    })

    val uspcs = new BagOfWordsVariable()
    mention.uspc.opt.foreach(_.foreach {
      label =>
        (label.mainclassID.opt ++ label.subclassID.opt).mkString("").noneIfEmpty.foreach(uspcs.add(_)(new DiffList))
    })


    val ipcrs = new BagOfWordsVariable()
    mention.ipcr.opt.foreach(_.foreach {
      label =>
        (label.classificationLevel.opt ++ label.section.opt ++ label.ipcClass.opt).mkString("").noneIfEmpty.foreach(ipcrs.add(_)(new DiffList))
    })

    val cpcs = new BagOfWordsVariable()
    mention.cpc.opt.foreach(_.foreach {
      label =>
        (label.sectionID.opt ++ label.subsectionID.opt).mkString("").noneIfEmpty.foreach(cpcs.add(_)(new DiffList))
    })

    val nbers = new BagOfWordsVariable()
    mention.nber.opt.foreach(_.foreach {
      label =>
        label.categoryID.opt.foreach(x => nbers.add(x)(new DiffList))
        label.subcategoryID.opt.foreach(x => nbers.add(x)(new DiffList))
    })


    // canopy
    val canopy = determineCanopy(mention.self.value)

    val vars = new InventorVars(firstNames,middleNames,new BagOfWordsVariable(),locations,new BagOfWordsVariable(),new BagOfWordsVariable(),new BagOfWordsVariable(),new BagOfWordsVariable(),new BagOfWordsVariable(),new BagOfWordsVariable(),new BagOfWordsVariable(),topics,new BagOfWordsVariable(),coinventors,assignees,new BagOfWordsVariable(),lawyers,new BagOfWordsVariable(),cpcs,ipcrs,uspcs,nbers,canopy,new BagOfWordsVariable())
    val m = new Mention[InventorVars](vars,mention.uuid.value)(new DiffList)
    m.variables.provenance = Some(mention)
    m
  }
  
  def addEntityLabels() = {
    val numMentions = mentions.size
    val numMentionsIndex = mentionIndex.size
    val numVarMentions = inventorVarMentions.size
    if (numMentions != numMentionsIndex) {
      println(s"[WARNING] Number of input mentions $numMentions does not " +
        s"match number of unique mention ids $numMentionsIndex. There could be" +
        s"mentions with duplicate ids. Duplicates: " +
        s"${mentions.map(_.uuid.value).counts.filter(_._2 > 1).mkString(" ")}")
    }
    if (numVarMentions != numMentionsIndex) {
      println(s"[WARNING] Number of variable mentions $numVarMentions does not " +
        s"match number of unique mention ids $numMentionsIndex. There could be" +
        s"mentions with duplicate ids.")
    }
    if (numVarMentions != numMentions) {
      println(s"[WARNING] Number of variable mentions $numVarMentions does not " +
        s"match number of input mention ids $numMentions.")
    }
    // Apply the entity ids for the mentions that were actually resolved.
    inventorVarMentions.foreach(m => mentionIndex(m.uniqueId).entityId.set(m.root.uniqueId))

    // Handle duplicates
    val duplicateIds = mentions.map(_.uuid.value).counts.filter(_._2 > 1).keySet
    if (duplicateIds.nonEmpty) {
      mentions.filter(m => duplicateIds.contains(m.uuid.value) && !m.entityId.isDefined).foreach{
        m =>
          m.entityId.set(mentionIndex(m.uuid.value).entityId.value)
      }
    }
  }


}

/**
 * Implementation using the single canopy method 
 * @param opts - the model parameters
 * @param keystore - the word embedding lookup dictionary
 * @param rawMentions - the mentions as they were stored in the mongo table
 */
class SingleCanopyHierarchicalInventorCorefRunner(opts: InventorModelOptions, keystore: Keystore, rawMentions: Iterable[InventorMention]) extends HierarchicalInventorCorefRunner(opts,keystore,rawMentions) {
  override def run(): Unit = {new SingleCanopyInventorCorefSystem(opts,inventorVarMentions).run(); addEntityLabels()}
  override def runPar(numThreads: Int): Unit = {new SingleCanopyInventorCorefSystem(opts,inventorVarMentions).runPar(numThreads); addEntityLabels()}
}

/**
 * Implementation used in submission, uses multiple canopies.
 * @param opts - the model parameters
 * @param keystore - the word embedding lookup dictionary
 * @param rawMentions - the mentions as they were stored in the mongo table
 */
class MultiCanopyHierarchicalInventorCorefRunner(opts: InventorModelOptions, keystore: Keystore, rawMentions: Iterable[InventorMention]) extends HierarchicalInventorCorefRunner(opts,keystore,rawMentions) {
  
  // We don't need this here
  override def determineCanopy(inventor: Inventor): String = ""
  
  private val canopyFunctions = Iterable((i: Inventor) => Canopies.fullName(i,false), (i: Inventor) => Canopies.firstAndLastNameCaseInsensitive(i), (i: Inventor) => Canopies.lastNameAndFirstNCharactersOfFirst(i,5,false), (i: Inventor) => Canopies.lastNameAndFirstNCharactersOfFirst(i,3,false))
  
  override def run(): Unit = {new MultiCanopyInventorCorefSystem(opts,inventorVarMentions,canopyFunctions).run(); addEntityLabels()}
  override def runPar(numThreads: Int): Unit = {new MultiCanopyInventorCorefSystem(opts,inventorVarMentions,canopyFunctions).runPar(numThreads); addEntityLabels()}
}