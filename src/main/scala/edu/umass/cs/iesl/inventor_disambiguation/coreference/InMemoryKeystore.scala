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

import java.io.{BufferedReader, File, FileInputStream, InputStreamReader}
import java.util

import cc.factorie._
import edu.umass.cs.iesl.inventor_disambiguation._

import scala.collection.JavaConverters._

/**
 * Modified from Jack Sullivan's implementation in factorie, a look up table for the embeddings.
 */
trait Keystore {
  def dimensionality:Int
  def retrieve(key:String):Option[Array[Double]]

  val missingKeys = new java.util.concurrent.ConcurrentHashMap[String, Int]().asScala.withDefaultValue(0)

  import cc.factorie.util.VectorUtils._
  def generateVector(keys:Iterable[String]):Array[Double] = keys.flatMap{ key =>
    val res = retrieve(key)
    if(res.isEmpty) {
      missingKeys += key -> (missingKeys(key) + 1)
    }
    res
  }.foldLeft(new Array[Double](dimensionality)){case (tot, arr) => tot += arr; tot}
}

class InMemoryKeystore(map: scala.collection.Map[String,Array[Double]]) extends Keystore {
  override def dimensionality: Int = map.head._2.length

  override def retrieve(key: String): Option[Array[Double]] = map.get(key)
  
  def nearestNeighbors(key: String, N: Int) = {
    val top = new cc.factorie.util.TopN[String](N)
    val maybeKeyEmb = retrieve(key)
    if (maybeKeyEmb.isDefined) {
      val keyEmb = maybeKeyEmb.get
      for (entry <- map) {
        top.+=(0,entry._2.cosineSimilarity(keyEmb),entry._1)
      }
    }
    top.map(entry=> (entry.category,entry.score))
  }
}


object InMemoryKeystore {
  
  def fromFile(embeddingFile:File, dimensionality:Int, fileDelimiter:String, codec: String) = {
    val _store = new util.HashMap[String,Array[Double]](10000).asScala

    new BufferedReader(new InputStreamReader(new FileInputStream(embeddingFile),codec)).toIterator.foreach {
      line =>
        val split = line.split(fileDelimiter)
        if (split.length == dimensionality + 1) { //plus 1 b/c format is wordtoken emb
        val key :: vec = line.split(fileDelimiter).toList
          _store.put(key, vec.map(_.toDouble).toArray)
        } else {
          println(s"[${this.getClass.getSimpleName}] WARNING: error reading line: $line")
        }
    }
    new InMemoryKeystore(_store)
  }

  def fromFile(embeddingFile: File, fileDelimiter: String, codec: String) = {

    val lines = new BufferedReader(new InputStreamReader(new FileInputStream(embeddingFile),codec)).toIterator
    val Array(numItems,dimensionality) = lines.next().split(fileDelimiter).map(_.toInt)
    val _store = new util.HashMap[String,Array[Double]](numItems).asScala

    lines.foreach {
      line =>
        val split = line.split(fileDelimiter)
        if (split.length == dimensionality + 1) { //plus 1 b/c format is wordtoken emb
        val key :: vec = line.split(fileDelimiter).toList
          _store.put(key, vec.map(_.toDouble).toArray)
        } else {
          println(s"[${this.getClass.getSimpleName}] WARNING: error reading line: $line")
        }
    }
    new InMemoryKeystore(_store)
    
  }
}