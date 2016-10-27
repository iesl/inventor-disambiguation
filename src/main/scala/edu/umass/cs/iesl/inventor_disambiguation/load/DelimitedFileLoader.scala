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


package edu.umass.cs.iesl.inventor_disambiguation.load

import java.io.{BufferedReader, File, FileInputStream, InputStreamReader}

import cc.factorie._
import edu.umass.cs.iesl.inventor_disambiguation._

/**
 * Base trait for loading delimited text files. It has various helper methods for loading 
 * multiple portions of the file, etc.
 * @tparam T - the type of the object that is loaded from the file
 */
trait DelimitedFileLoader[T] {

  /**
   * The character or regex that will be used to split the lines of the file. 
   * @return
   */
  def delimiter: String

  /**
   * How to handle a line that has been split by the delimiter. 
   * This is one of the methods that implementations of this trait 
   * will need to define. 
   * @param split
   * @return
   */
  def parse(split: Array[String]): Option[T]


  /**
   * Whether or not the given length is expected. By default, this is 
   * true when the length appears in expectedLineLength*
   * @param length - length of interest
   * @return
   */
  def expectedLineLength(length: Int) = expectedLineLengths contains length

  /**
   * The line lengths (in terms of elements after the split) that are acceptable for the loader. 
   * @return
   */
  def expectedLineLengths: Set[Int]

  /**
   * Whether or not to skip the first line, i.e. if it is a header 
   * @return
   */
  def skipFirstLine: Boolean
  
  var printMessages: Boolean = true

  val printEvery: Int = 10000

  /**
   * Gives an iterator over the entire file 
   * @param file - input file
   * @param codec - encoding
   * @return - iterator over the lines of the file
   */
  def load(file: File, codec: String = "UTF-8"): Iterator[T] = load(file,codec,0,Int.MaxValue)

  /**
   * Gives an iterator starting at the given line 
   * @param file - input file
   * @param codec - encoding
   * @param start - start line
   * @return
   */
  def load(file: File, codec: String, start: Int): Iterator[T] = load(file,codec,start,Int.MaxValue)

  /**
   * Givesn an iterator over the lines of the file between start (inclusive) and end (exclusive).  
   * @param file - the file to read
   * @param codec - the encoding
   * @param start - the starting line number (inclusive)
   * @param end - the ending line number (exclusive)
   * @return
   */
  def load(file: File, codec: String, start: Int, end: Int) : Iterator[T] = {
    val className = this.getClass.conventionalName
    println(s"[$className] Loading from ${file.getName}")
    val reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), codec)).toIterator.zipWithIndex.filter(p => p._2 >= start && p._2 < end)
    
    if (skipFirstLine && start == 0)
      reader.next()
    
    val res = reader.flatMap {
      case (line, count) =>
        if (printMessages && count % printEvery == 0)
          println(s"\r[$className] Loaded $count lines")
        val split = line.split(delimiter,-1)
        if (expectedLineLength(split.length))
          parse(split)
        else {
          println(s"\n[$className] Line $count with unexpected length (${split.length}): $line")
          None
        }
    }
    res
  }

  /**
   * Splits the file into N chunks and returns an iterator for each chunk. This can be 
   * very handy for multithreading
   * @param file - the file
   * @param codec - the encoding
   * @param num - the number of chunks to use
   * @return - an iterable of num iterators over the file
   */
  def loadMultiple(file: File, codec: String, num: Int): Iterable[Iterator[T]] = {
    val numLinesInFile = file.numLines
    println(s"[${this.getClass.conventionalName}] There are $numLinesInFile in ${file.getName}")
    val blockSize = numLinesInFile/num
    println(s"[${this.getClass.conventionalName}] Each of the $num iterators will have about $blockSize items")
    val startingIndices = (0 until num).map(_ * blockSize)
    startingIndices.dropRight(1).zip(startingIndices.drop(1)).map(f => load(file,codec,f._1,f._2)) ++ Iterable(load(file,codec,startingIndices.last))
  }

}


trait TabSeparatedFileLoader[T] extends DelimitedFileLoader[T] {
  
  def delimiter = "\t"
  
} 

trait CommaSeparateFileLoader[T] extends DelimitedFileLoader[T] {

  def delimiter =  """,(?=[^"]*([^"]*"[^"]*")*[^"]*$)"""
}