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


package edu.umass.cs.iesl.inventor_disambiguation.utilities

import java.io.{BufferedReader, InputStream, InputStreamReader}

import cc.factorie._
import cc.factorie.app.nlp.lexicon.CustomStopWords


object InventorNameStopWords extends CustomStopWords(Stopwords.loadResource("/inventor-name-stopwords.txt", "UTF-8")) {

  def removeStopwords(text: String):String = {
    removeStopwords(text.split("\\s")).mkString(" ")
  }

  def removeStopwords(tokens: Seq[String]):Seq[String] = {
    val found = this.trie.findMentions(tokens).map(_.mention)
    tokens.filterNot(found.contains)
  }

}


object Stopwords {


  def loadResource(filename: String, codec: String) = {
    val stream  = getClass.getResourceAsStream(filename)
    loadStream(stream,codec)
  }

  def loadStream(stream: InputStream, codec: String) = {
    val reader = new BufferedReader(new InputStreamReader(stream,codec))
    reader.toIterator.toSeq
  }

}