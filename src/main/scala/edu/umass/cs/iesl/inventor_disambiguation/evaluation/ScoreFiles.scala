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


package edu.umass.cs.iesl.inventor_disambiguation.evaluation

import java.io.File

import cc.factorie.util.{BasicEvaluatableClustering, EvaluatableClustering}
import edu.umass.cs.iesl.inventor_disambiguation.load.LoadTabSeparatedTuples

/**
 * Given two tab separated files, one of the predicted clustering, another
 * of the gold clustering, evaluate the predictions. The files are tab separated
 * two column files where the first column corresponds to the point ids and the second corresponds to 
 * the cluster ids. Optionally the name of the algorithm can be given as the third parameter.
 * 
 * Usage:
 * java -Xmx20G -cp target/inventor_disambiguation-1.0-SNAPSHOT-jar-with-dependencies.jar edu.umass.cs.iesl.inventor_disambiguation.evaluation.ScoreFiles
 */
object ScoreFiles {

  def main(args: Array[String]): Unit = {
    val pred = load(new File(args(0)),"UTF-8")
    val gold = load(new File(args(1)), "UTF-8")
    val name = if (args.length >= 3) args(2) else "Coref. Alg"
    val resPred = restrictPredicted(pred,gold)
    val eval = new EvaluateCoreference[String,String](name,resPred,gold)
    val precisionRecallF1 = eval.formattedString(eval.evaluate)
    val pairwiseB3MUC = EvaluatableClustering.evaluationString(new BasicEvaluatableClustering(resPred), new BasicEvaluatableClustering(gold))
    val mdStringOrig = (Iterable(name) ++ pairwiseB3MUC.split("\n").drop(2).map(_.split(" ")).map(_.drop(1).mkString(" | "))).mkString("| ", " | ", " |")
    println(pairwiseB3MUC + "\n" + s"summary_pw_muc_b3: $mdStringOrig" + "\n\n" + precisionRecallF1 + "\n\n")
  }
  
  def load(file: File, codec: String) = {
    LoadTabSeparatedTuples.load(file,codec).toIndexedSeq
  }
  
  def restrictPredicted(pred: Iterable[(String,String)], gold: Iterable[(String,String)]) = {
    val goldPointSet = gold.map(_._1).toSet[String]
    pred.filter(p => goldPointSet.contains(p._1))
  }
  
}
