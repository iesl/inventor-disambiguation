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

package edu.umass.cs.iesl

import java.io._
import java.util.zip.GZIPInputStream
import cc.factorie._
import scala.collection.JavaConverters._

package object inventor_disambiguation {

  implicit class IterableExtras[T](iterable: Iterable[T]) {

    def stableRemoveDuplicates() = {
      var uniq = new java.util.HashSet[T]().asScala
      val res = iterable.flatMap{
        case f =>
          if (uniq.contains(f))
            None
          else {
            uniq.add(f)
            Some(f)
          }
      }
      uniq = null
      res
    }
    
    def mostCommonElements = {
      val count = iterable.counts
      val max = count.maxBySafe(_._2).map(_._2).getOrElse(0)
      count.filter(_._2 == max).map(_._1)
    }
    
    def counts =
      iterable.groupBy(f => f).mapValues(_.size)

    def elementsOccurringXPercent(percent: Double) = {
      val count = iterable.counts
      val total = count.map(_._2).sum.toDouble
      count.filter(p => p._2 / total >= percent).map(_._1)
    }

    def elementsInMajority = elementsOccurringXPercent(0.5)
    
    def maxBySafe[U](f: T => U)(implicit ordering: Ordering[U]): Option[T] = if (iterable.isEmpty) None else Some(iterable.maxBy(f))
    
  }
  
  implicit class StringExtras(string: String) {
    
    def noneIfEmpty = if (string.isEmpty) None else Some(string)

    def noneIfIn(set: Set[String]) = if (set contains string) None else Some(string)
    
    def removeQuotes() = string.replaceAll("^\"|^\'|\"$|\'$","")
    
    def removeNewLinesAndTrailingSpaces() = string.trimBegEnd().replaceAll("\n+|\t+"," ")

    def clean() = string.removeQuotes().removeNewLinesAndTrailingSpaces()
    
    def noneIfNAorBlank = {
      val trimmed = string.trim
      if (trimmed.isEmpty || trimmed == "NA") None else Some(string)
    }
    
    def alphanumericAndSpacesOnly = {
      string.replaceAll("[^a-zA-Z0-9\\s]","")
    }
    
    def trimBegEnd() = {
      string.replaceAll("^\\s+|\\s+$","")
    }
    
    def noneIfNULL = noneIfIn(Set("NULL"))
    
    def noneIfEmptyDate = noneIfIn(Set("0000-00-00"))

    def removePunctuation = {
      string.replaceAll("""[,|\.|`|~|!|@|#|$|%|\^|&|\*|\(|\)|=|\+|\\|\||'|\"|\[|\{|\]|\}|\<|\>|\/|\?]+""","")
    }
  }
  
  implicit class ArrayDoubleExras(arr: Array[Double]) {
    
    def cosineSimilarity(other: Array[Double]) =
      arr.dot(other) / (arr.norm * other.norm)

    
    def dot(other: Array[Double]) = {
      assert(arr.length == other.length)
      var sum = 0.0
      var i = 0
      while (i < arr.length) {
        sum += arr(i) * other(i)
        i += 1
      }
      sum
    }

  def norm = {
    var sum = 0.0
    var i = 0
    while (i < arr.length) {
      sum += arr(i) * arr(i)
      i += 1
    }
    Math.sqrt(sum)
  }
  }

  implicit class ClassExtras(clss: Class[_]) {
    def conventionalName =
      clss.getSimpleName.replaceAll("\\$$","")
  }
 
  implicit class FileExtras(file: File) {
    
    def numLines =
      new BufferedReader(new FileReader(file)).toIterator.size

    def lines(codec: String = "UTF-8", start: Int = 0, end: Int = Int.MaxValue) =
      if (file.getName.endsWith(".gz"))
        new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)),codec)).toIterator.zipWithIndex.filter(p => p._2 >= start && p._2 < end).map(_._1)
      else
        new BufferedReader(new InputStreamReader(new FileInputStream(file),codec)).toIterator.zipWithIndex.filter(p => p._2 >= start && p._2 < end).map(_._1)


    def mkParentDirs():Unit =
      file.getParentFile.toNotNull.map(_.mkdirs())
  }
}
