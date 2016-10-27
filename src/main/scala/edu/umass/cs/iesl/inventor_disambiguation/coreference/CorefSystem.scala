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

import java.io.{File, PrintWriter}

import cc.factorie.app.nlp.hcoref._
import cc.factorie.util.{BasicEvaluatableClustering, EvaluatableClustering}
import edu.umass.cs.iesl.inventor_disambiguation._
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.PatentsViewRecord
import edu.umass.cs.iesl.inventor_disambiguation.evaluation.EvaluateCoreference

/**
 * Base trait for disambiguation/coreference algorithms operating on Mongo/Raw data structures.
 * CoreferenceAlgorithms differ from HierarchicalCorefSystem in that CoreferenceAlgorithms operate on
 * the data structures drawn from the Patent data while HierarchicalCorefSystem systems operate on Node
 * variables. CoreferenceAlgorithms can be implemented in such a way that mentions are converted into Node variables
 * and disambiguated with a HierarchicalCorefSystem. 
 * @tparam MentionType The datatype of the mentions, must be subclass of PatentsViewRecord
 */
trait CoreferenceAlgorithm[MentionType <: PatentsViewRecord] {

  /**
   * The name of the algorithm
   */
  val name = this.getClass.conventionalName

  /**
   * The mentions to be disambiguated
   * @return
   */
  def mentions: Iterable[MentionType]

  /**
   * Execute the algorithm, disambiguating the mentions 
   */
  def run(): Unit

  /**
   * Execute the algorithm, using the given number of threads
   * @param numThreads
   */
  def runPar(numThreads: Int): Unit

  /**
   * Return the clustering result as pairs of mention ids and entity ids
   * @return
   */
  def predictedClustering: Iterable[(String,String)]

}

/**
 * Allows mentions to be looked up with some IDs 
 * @tparam MentionType
 */
trait IndexableMentions[MentionType <: PatentsViewRecord] {
  def getMention(id: String): MentionType
}

/**
 * Code to run and evaluate with a gold labeling 
 * @tparam MentionType
 */
trait ExperimentWithGroundTruth[MentionType <: PatentsViewRecord]  {

  val algorithm: CoreferenceAlgorithm[MentionType] with IndexableMentions[MentionType]

  def goldClustering: Iterable[(String,String)]

  lazy val predictedClusteringOnGoldPoints = {val goldPointIDs = goldClustering.map(_._1).toSet; algorithm.predictedClustering.filter(p => goldPointIDs.contains(p._1))}

  lazy val evaluation = new EvaluateCoreference(algorithm.name,predictedClusteringOnGoldPoints,goldClustering)

  lazy val results: String = {
    val precisionRecallF1 = evaluation.formattedString(evaluation.evaluate)
    val errorAnalysis = evaluation.errorAnalysis(algorithm.getMention)
    val pairwiseB3MUC = EvaluatableClustering.evaluationString(new BasicEvaluatableClustering(predictedClusteringOnGoldPoints), new BasicEvaluatableClustering(goldClustering))
    val mdStringOrig = (Iterable(algorithm.name) ++ pairwiseB3MUC.split("\n").drop(2).map(_.split(" ")).map(_.drop(1).mkString(" | "))).mkString("| ", " | ", " |")
    pairwiseB3MUC + "\n" + s"mdString_pw_muc_b3: $mdStringOrig" + "\n\n" + precisionRecallF1 + "\n\n" + evaluation.formattedString(errorAnalysis)
  }

  lazy val truePositiveHTML = evaluation.truePositivesReport(algorithm.getMention)
  lazy val falsePositiveHTML = evaluation.falsePositivesReport(algorithm.getMention)
  lazy val falseNegativeHTML = evaluation.falseNegativesReport(algorithm.getMention)

  def performExperiment(outputDir: File, writeHTML: Boolean = false,numThreads: Int) = {
    if (numThreads == 1) algorithm.run() else algorithm.runPar(numThreads)
    writeClustering(algorithm.predictedClustering,new File(outputDir,"predicted_clustering.txt"))
    writeClustering(goldClustering,new File(outputDir,"gold_clustering.txt"))
    val pw1 = new PrintWriter(new File(outputDir,"results.txt"))
    pw1.println(results)
    pw1.close()

    if (writeHTML) {
      val pw3 = new PrintWriter(new File(outputDir, "falsePositives.html"))
      pw3.println(falsePositiveHTML)
      pw3.close()

      val pw4 = new PrintWriter(new File(outputDir, "falseNegatives.html"))
      pw4.println(falseNegativeHTML)
      pw4.close()
    }
  }

  def writeClustering(clustering: Iterable[(String,String)], file: File) = {
    val pw = new PrintWriter(file,"UTF-8")
    clustering.foreach{
      pr =>
        pw.println(pr._1 + "\t" + pr._2)
        pw.flush()
    }
    pw.close()
  }

}


/**
 * Base trait for hierarchical coreference. 
 * Note that this differs from CoreferenceAlgorithm in that this works on
 * node variables while the CoreferenceAlgorithm trait works on the raw data from the database 
 * @tparam V
 */
trait HierarchicalCorefSystem[V <: NodeVariables[V] with SingularCanopy] {

  val name = this.getClass.getSimpleName.replaceAll("\\$$","")

  def mentions: Iterable[Mention[V]]

  def model: CorefModel[V]

  def estimateIterations(ments: Iterable[Node[V]]) = math.min(ments.size * 30.0, 1000000.0).toInt

  def sampler(mentions:Iterable[Node[V]]): CorefSampler[V]

  val quiet: Boolean = true
  
  def corefPrintln(string: String): Unit = if (!quiet) println(string)

  def run() = {
    corefPrintln(s"[$name] Running Coreference Experiment.")
    val groupped = mentions.groupBy(_.variables.canopy)
    val numTotalCanopies = groupped.size
    groupped.toIterable.zipWithIndex.foreach {
      case ((canopy, mentionsInCanopy),idx) =>
        corefPrintln(s"[$name] Processing canopy #$idx of $numTotalCanopies, $canopy, with ${mentionsInCanopy.size} mentions")
        try {
          val start = System.currentTimeMillis()
          sampler(mentionsInCanopy).infer()
          corefPrintln(s"[$name] Finished Coref. Total time: ${System.currentTimeMillis() - start} ms")
        }
        catch {
          case e: Exception =>
            corefPrintln(s"[$name] Failure:")
            e.printStackTrace()
        }
    }
  }
  
  def runPar(numThreads: Int) = {
    corefPrintln(s"[$name] Running Coreference Experiment.")
    val groupped = mentions.groupBy(_.variables.canopy)
    val numTotalCanopies = groupped.size
    groupped.toIterable.zipWithIndex.grouped(numThreads).toIterable.par.foreach(_.foreach {
      case ((canopy, mentionsInCanopy), idx) =>
        corefPrintln(s"[$name] Processing canopy #$idx of $numTotalCanopies, $canopy, with ${mentionsInCanopy.size} mentions")
        try {
          val start = System.currentTimeMillis()
          sampler(mentionsInCanopy).infer()
          corefPrintln(s"[$name] Finished Coref. Total time: ${System.currentTimeMillis() - start} ms")
        }
        catch {
          case e: Exception =>
            corefPrintln(s"[$name] Failure:")
            e.printStackTrace()
        }
    })
  }

  def clusterLabels = mentions.map((f) => (f.uniqueId,f.entity.uniqueId))

}
