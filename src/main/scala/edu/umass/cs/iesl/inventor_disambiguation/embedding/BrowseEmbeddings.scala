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


package edu.umass.cs.iesl.inventor_disambiguation.embedding

import java.io.File

import cc.factorie.app.bib.hcoref.InMemoryHashMapKeystoreOpts
import edu.umass.cs.iesl.inventor_disambiguation.coreference.InMemoryKeystore

/**
 * Given a plain text file of embeddings. This executable lets you browse the nearest neighbors
 * of words in the embedding space using an interactive prompt. *
 */
class BrowseEmbeddingsOpts extends InMemoryHashMapKeystoreOpts 

object BrowseEmbeddings {
  
  def main(args: Array[String]): Unit = {
    
    val opts = new BrowseEmbeddingsOpts
    opts.parse(args)
    
    val keystore = InMemoryKeystore.fromFile(new File(opts.keystorePath.value)," ", "UTF-8")

    println("Use CTRL-C to quit")
    val done = false
    while (!done) {
      try {
        print("Enter word: ")
        val word = io.Source.stdin.getLines().next().trim
        print("Num Neighbors: ")
        val num = io.Source.stdin.getLines().next().toInt
        val neighbors = keystore.nearestNeighbors(word,num)
        neighbors.foreach{
          pair =>
            println(pair._1 + "\t" + pair._2)
        }
        println()
      } catch {
        case e: Exception =>
          println(s"\nSomething went wrong: ")
          e.printStackTrace()
      }
    }


  }

}
