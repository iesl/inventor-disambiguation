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

import java.io.{PrintWriter, File}

import cc.factorie.app.nlp.hcoref.Mention
import edu.umass.cs.iesl.inventor_disambiguation.coreference.InventorVars
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.PatentsViewRecord
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.coreference.InventorMention

import scala.collection.mutable.ArrayBuffer

/**
 * Helper methods for scoring a coreference system on a dataset with gold labels
 * @param algName - the name of the algorithm
 * @param predicted - the predicted clustering represented as pairs of point ids and cluster ids
 * @param gold - the gold clustering represented as pairs of point ids and cluster ids
 * @tparam P - the point id datatype
 * @tparam C - the cluster id datatype
 */
class EvaluateCoreference[P <: Comparable[P],C](algName: String, predicted: Iterable[(P,C)], gold: Iterable[(P,C)]) {
  
  var truePositives: Set[(P,P)] = null
  var falseNegatives: Set[(P,P)] = null
  var falsePositives: Set[(P,P)] = null
  
  def evaluate = {
    
    // 1. Check that they are evaluated on same set of points
    
    val predPoints = predicted.map(_._1)
    val goldPoints = gold.map(_._1)
    
    val predPointsUniq = predPoints.toSet[P]
    val goldPointsUniq = goldPoints.toSet[P]
    
    assert(predPointsUniq == goldPointsUniq, s"The two clustering results must be defined on the same set of points. There are ${predPointsUniq.size} predicted points and ${goldPointsUniq.size} gold points. There are ${predPointsUniq.diff(goldPointsUniq).size} points in the predicted set that do not appear in the gold set. There are ${goldPointsUniq.diff(predPointsUniq).size} points in the gold set that do not appear in the predicted set.")
    assert(predPoints.size == predPointsUniq.size, "The predicted clustering contains at least one point multiple times.")
    assert(goldPoints.size == goldPointsUniq.size, "The gold clustering contains at least one point multiple times.")

    // 2. Build pairwise coreference maps for each
    
    val predPairs = collectCoreferencePairs(predicted)
    val goldPairs = collectCoreferencePairs(gold)
    
    // 3. Use maps to evaluate coreference as described by manual
    
    truePositives = predPairs.intersect(goldPairs)
    falseNegatives = goldPairs.diff(predPairs)
    falsePositives = predPairs.diff(goldPairs)
    
    // 4. Package up the results:
    val recall = truePositives.size.toDouble / (truePositives.size + falseNegatives.size).toDouble
    val splitting = falseNegatives.size.toDouble / (truePositives.size + falseNegatives.size).toDouble
    val lumping = falsePositives.size.toDouble / (truePositives.size + falseNegatives.size).toDouble
    val precision = truePositives.size.toDouble / (truePositives.size + falsePositives.size).toDouble
    val f1 = 2.0 * (recall * precision) / (recall + precision)
    
    val numEntities = predicted.map(_._2).toSet.size
    val numGoldEntities = gold.map(_._2).toSet.size

    
    val mdString = Iterable(algName,numEntities,precision,recall,f1,splitting,lumping,truePositives.size,falsePositives.size,falseNegatives.size).mkString("| ", " | ", " |")
    Map("Num. Pred Entities" -> numEntities, "Num. Gold Entities" -> numGoldEntities, "recall" -> recall, "splitting" -> splitting, "lumping" -> lumping, "precision" -> precision, "f1" -> f1, "Num. True Positives" -> truePositives.size, "Num. False Negatives" -> falseNegatives.size, "Num. False Positives" -> falsePositives.size, "summary" -> mdString)
  }
  
  def formattedString(map: Map[String, Any]) = (Iterable("Clustering Results:\n") ++ map.map(f => f._1 + ": " + f._2)).mkString("\n")

  def collectCoreferencePairs(clustering: Iterable[(P,C)]) = {
    val byClusterID = clustering.groupBy(_._2).mapValues(_.map(_._1).toIndexedSeq.sorted)
    val coreferencePairs = new ArrayBuffer[(P,P)](byClusterID.map(ps => ps._2.size * ps._2.size).sum)
    byClusterID.foreach{
      case (clusterID, pointsInCluster) =>
        var i = 0
        while (i < pointsInCluster.length-1) {
          var j = i + 1
          while (j < pointsInCluster.length) {
            coreferencePairs.+=((pointsInCluster(i),pointsInCluster(j)))
            j += 1
          }
          i += 1
        }
    }
    coreferencePairs.toSet
  }
  
  
  def pairwiseHTMLReport(pairs: Iterable[(P,P)], lookup: P => PatentsViewRecord, title: String) = {
    
    val sb = new StringBuilder
    sb.append(s"<html>\n<body>\n")
    sb.append(s"\n<h1>$title</h1>")
    pairs.zipWithIndex.foreach{
      case (pair,idx) =>
        sb.append(s"\n<h3>Pair #$idx</h3>")
        sb.append(s"\n<br>${lookup(pair._1).toHTMLFormattedString}<br><br>\n")
        sb.append(s"\n<br>${lookup(pair._2).toHTMLFormattedString}<br><br>\n")
    }
    sb.append("</body></html>")
  }
  
  def truePositivesReport(lookup: P => PatentsViewRecord) = 
    pairwiseHTMLReport(truePositives,lookup,"True Positives")
  
  def falsePositivesReport(lookup: P => PatentsViewRecord) =
    pairwiseHTMLReport(falsePositives,lookup,"False Positives")

  def falseNegativesReport(lookup: P => PatentsViewRecord) =
    pairwiseHTMLReport(falseNegatives,lookup,"False Negatives")

  def errorAnalysis(lookup: P => PatentsViewRecord) = {
    
    // Number of FPs where inventors have the same first and last name spellings (ignoring the middle)
    val FP_sameFirstAndLastNameSpelling = falsePositives.count{
      pair =>
        val m1 = lookup(pair._1)
        val m2 = lookup(pair._2)
        (m1,m2) match {
          case ((inv1: InventorMention, inv2: InventorMention)) =>
            inv1.self.value.sameFirstAndLastName(inv2.self.value)
          case _ =>
            false
        }
    }

    // Number of FNs where inventors have the same first and last name spellings (ignoring the middle)
    val FN_sameFirstAndLastNameSpelling = falseNegatives.count{
      pair =>
        val m1 = lookup(pair._1)
        val m2 = lookup(pair._2)
        (m1,m2) match {
          case ((inv1: InventorMention, inv2: InventorMention)) =>
            inv1.self.value.sameFirstAndLastName(inv2.self.value)
          case _ =>
            false
        }
    }

    // Number of FNs where inventors have the same first, middle and last name spellings
    val FN_sameFullNameSpelling = falseNegatives.count{
      pair =>
        val m1 = lookup(pair._1)
        val m2 = lookup(pair._2)
        (m1,m2) match {
          case ((inv1: InventorMention, inv2: InventorMention)) =>
            inv1.self.value.sameFullName(inv2.self.value)
          case _ =>
            false
        }
    }
    
    // Number of FNs where inventors have the same last name, but a differently spelled first name
    val FN_sameLastDiffFirst = falseNegatives.count{
      pair =>
        val m1 = lookup(pair._1)
        val m2 = lookup(pair._2)
        (m1,m2) match {
          case ((inv1: InventorMention, inv2: InventorMention)) =>
            inv1.self.value.sameLastName(inv2.self.value) && !inv1.self.value.sameFirstName(inv2.self.value)
          case _ =>
            false
        }
    }
    
    // Number of FNs where inventors have differently spelled last names
    val FN_differentLastName = falseNegatives.count{
      pair =>
        val m1 = lookup(pair._1)
        val m2 = lookup(pair._2)
        (m1,m2) match {
          case ((inv1: InventorMention, inv2: InventorMention)) =>
            !inv1.self.value.sameLastName(inv2.self.value)
          case _ =>
            false
        }
    }
    Map("False Positives: sameFirstAndLastNameSpelling" -> FP_sameFirstAndLastNameSpelling,
      "False Negatives: sameFirstAndLastNameSpelling" -> FN_sameFirstAndLastNameSpelling,
    "False Negatives: sameFullNameSpelling" -> FN_sameFullNameSpelling,
    "False Negatives: sameLastDiffFirst" -> FN_sameLastDiffFirst,
    "False Negatives: differentLastName" -> FN_differentLastName
    )
  }
    

}


object DebugHelpers {


  def falsePositivesByCanopy(falsePositivePairs: Iterable[(String,String)], mentions: Iterable[Mention[InventorVars]]) = {
    val byID = mentions.groupBy(_.uniqueId).mapValues(_.head)
    val falsePositiveMentionPairs = falsePositivePairs.map(f => (byID(f._1),byID(f._2)) )
    val byCanopy = falsePositiveMentionPairs.groupBy(_._1.variables.canopy).mapValues(v => v.map(f => (f._1.uniqueId,f._2.uniqueId)))
    byCanopy
  }

  def writeFalsePositivesByCanopy(file: File, map: Map[String,Iterable[(String,String)]]) = {
    val pw = new PrintWriter(file)
    map.foreach{
      case (canopy,mentions) =>
        pw.print(canopy)
        pw.print("\t")
        pw.print(mentions.flatMap(f => Iterable(f._1,f._2)).toSet.mkString(","))
        pw.println()
        pw.flush()
    }
    pw.close()
  }

  
  def pairwisePlainTextReportByCanopy(pairs: Iterable[(String,String)], mentions: Iterable[Mention[InventorVars]]) = {
    val byID = mentions.groupBy(_.uniqueId).mapValues(_.head)
    val MentionPairs = pairs.map(f => (byID(f._1),byID(f._2)) )
    val byCanopy = MentionPairs.groupBy(_._1.variables.canopy).mapValues(v => v.map(f => (f._1.variables.provenance.get.self.value.debugString(),f._2.variables.provenance.get.self.value.debugString())))
    byCanopy
  }

  def writePairwisePlainTextReportByCanopy(file: File, map: Map[String,Iterable[(String,String)]]) = {
    val pw = new PrintWriter(file)
    map.foreach{
      case (canopy,mentions) =>
        mentions.foreach {
          mention =>
            pw.print(canopy)
            pw.print("\t")
            pw.print(mention._1 + "\t" + mention._2)
            pw.println()
            pw.flush()
        }
    }
    pw.close()
  }
  
  
}
