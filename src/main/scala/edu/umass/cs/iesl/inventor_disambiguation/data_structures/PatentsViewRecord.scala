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


package edu.umass.cs.iesl.inventor_disambiguation.data_structures

import cc.factorie.util.Cubbie
import edu.umass.cs.iesl.inventor_disambiguation._


/**
 * Cubbies, from the toolkit factorie, provide, among other things, serialization
 * to and from Mongo (and other formats) very easily. Nearly all of the data structures
 * in this project extent PatentsViewRecord and in so doing, Cubbie. This allows them to 
 * be easily loaded into and out of MongoDBs 
 */
class PatentsViewRecord extends Cubbie{

  /**
   * Every PatentsViewRecord has a patentID 
   */
  val patentID = new StringSlot("patentID")

  /**
   * Every class that is a descendant of PatentsViewRecord has a name, which is a cleaned version of its class name. 
   */
  val name = this.getClass.getSimpleName.replaceAll("\\$$", "")

  /**
   * Creates an HTML formatted string * 
   * @return
   */
  def toHTMLFormattedString: String = {
    val sb = new StringBuilder(1000)
    sb.append("\n<ul style=\"list-style-type:none\"><b>")
    sb.append(s"${this.getClass.conventionalName}</b><br>")
    sb.append(HTMLFormatMap(this._map))
    sb.append("\n</ul>")
    sb.toString()
  }

  /**
   * Helper method to generate the HTML strings
   * @param map
   * @return
   */
  protected def HTMLFormatMap(map: scala.collection.Map[String,Any]): String = {
    val sb = new StringBuilder(1000)
    sb.append(s"\n<ul>")
    map.foreach{
      case (key, value) =>
        value match {
          case innerMap: scala.collection.Map[String,Any] =>
            sb.append(s"\n<li><b>$key</b>:</li>")
            sb.append(HTMLFormatMap(innerMap))
          case iterable: Iterable[Any] =>
            iterable.foreach{_ match {
              case anotherMap: scala.collection.Map[String,Any] =>
                sb.append(s"\n<li><b>$key</b>:</li>")
                sb.append(HTMLFormatMap(anotherMap))
              case _ =>
                sb.append(s"\n<li><b>$key</b>: ${value.toString}</li>")
            }}
          case _ =>
            sb.append(s"\n<li><b>$key</b>: ${value.toString}</li>")
        }
    }
    sb.append("\n</ul>")
    sb.toString()
  }
  
}

