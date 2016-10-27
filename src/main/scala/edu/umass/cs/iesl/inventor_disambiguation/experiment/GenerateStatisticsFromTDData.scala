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


package edu.umass.cs.iesl.inventor_disambiguation.experiment

import java.io.{PrintWriter, File}

import edu.umass.cs.iesl.inventor_disambiguation.process.GenerateTDGroundTruth

/**
 * Used in initial data analysis for understanding the distribution of mentions in the 
 * training data.
 */
object GenerateStatisticsFromTDData {

  
  def main(args: Array[String]): Unit = {
    
    val tdInventor: File = new File(args(0))
    
    val records = GenerateTDGroundTruth.generateRecords(tdInventor)
    
    val gt = records.filter(_.groundTruthID != null).map(r => (GenerateTDGroundTruth.getPointID(r),r.groundTruthID))
    
    val clusterId2clusterSize = gt.groupBy(_._2).mapValues(_.size).toIndexedSeq.sortBy(_._2)
    
    val clusterSize2numberOfClustersWithThatSize = clusterId2clusterSize.groupBy(_._2).mapValues(_.size).toIndexedSeq.sortBy(_._1)

    val plot1 = pgfPlot(clusterId2clusterSize.zipWithIndex.map(f => (f._2,f._1._2)), "Cluster ID", "Cluster Size (Num. Mentions)")
    
    val plot2 = pgfPlot(clusterSize2numberOfClustersWithThatSize, "Cluster Size", "Number of Clusters with Given Size")
    
    val pw1 = new PrintWriter("cluster_id_vs_cluster_size.tex")
    pw1.print(plot1)
    pw1.close()
    
    val pw2 = new PrintWriter("cluster_size_vs_number_of_clusters.tex")
    pw2.print(plot2)
    pw2.close()
    
  }
  
  def pgfPlot(points: Iterable[(AnyVal,AnyVal)], xlabel: String, ylabel: String) = 
     s"""\\documentclass[border=2cm]{standalone}
    \\usepackage{pgfplots}
    \\pgfplotsset{compat=1.6}
    \\usepackage{tikz}
    \\begin{document}
    \\begin{tikzpicture}
    \\begin{axis}[xlabel=$xlabel,ylabel=$ylabel]
    \\addplot coordinates {
     ${points.map(f => s"(${f._1},${f._2})").mkString("\n")}
    };
    \\end{axis}
    \\end{tikzpicture}
    \\end{document}"""

}
