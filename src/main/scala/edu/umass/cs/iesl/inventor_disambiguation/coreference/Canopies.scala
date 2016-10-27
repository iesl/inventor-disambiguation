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

import edu.umass.cs.iesl.inventor_disambiguation.data_structures.Inventor
import edu.umass.cs.iesl.inventor_disambiguation._

/**
 * Functions used for determining which canopy of a particular inventor resides in
 */
object Canopies {

  /**
   * The canopy is the inventor last name and the first N characters of the inventor's first name, either as listed or lowercased  
   * @param inventor - the inventor
   * @param N - the number of characters of the first name to include
   * @param caseSensitive - whether or not to keep the case information
   * @return the canopy
   */
  def lastNameAndFirstNCharactersOfFirst(inventor: Inventor, N: Int, caseSensitive: Boolean) = {
    val nameParts =  Some("LAST_") ++ inventor.nameLast.opt.map(f => if (caseSensitive) f else f.toLowerCase) ++ Some("_FIRST_") ++ inventor.nameFirst.opt.map(f => if (caseSensitive) f.slice(0,N) else f.toLowerCase.slice(0,N))
    nameParts.mkString("")
  }

  /**
   * The canopy is the inventor last name and the first character of the inventor first name, each lowercased 
   * @param inventor - the inventor
   * @return the canopy
   */
  def lastNameAndFirstCharCaseInsensitive(inventor: Inventor) = lastNameAndFirstNCharactersOfFirst(inventor,1,caseSensitive = false)

  /**
   * The canopy is the inventor last name and the first three characters of the inventor first name, lowercased
   * @param inventor the inventor
   * @return the canopy
   */
  def lastNameAndFirstThreeCharsCaseInsensitive(inventor: Inventor) = lastNameAndFirstNCharactersOfFirst(inventor,3,caseSensitive = false)

  /**
   * The first and last names of the inventor as the canopy 
   * @param inventor - the inventor
   * @param caseSensitive - whether or not to keep the case information
   * @return the canopy
   */
  def firstAndLastName(inventor: Inventor, caseSensitive: Boolean) = {
    val nameParts =  Some("LAST_") ++ inventor.nameLast.opt.map(f => if (caseSensitive) f else f.toLowerCase) ++ Some("_FIRST_") ++ inventor.nameFirst.opt.map(f => if (caseSensitive) f else f.toLowerCase)
    nameParts.mkString("")
  }

  /**
   * The case insensitive first and last names of the inventor 
   * @param inventor - the inventor
   * @return the canopy
   */
  def firstAndLastNameCaseInsensitive(inventor: Inventor) = firstAndLastName(inventor,caseSensitive = false)

  /**
   * The last name of the inventor
   * @param inventor - the inventor
   * @param caseSensitive - whether or not to keep the case information
   * @return the canopy
   */
  def fullName(inventor: Inventor,caseSensitive: Boolean) = {
    val nameParts =  Some("LAST_") ++ inventor.nameLast.opt.map(f => if (caseSensitive) f else f.toLowerCase) ++ Some("_FIRST_") ++ inventor.nameFirst.opt.map(f => if (caseSensitive) f else f.toLowerCase) ++  Some("_MIDDLE_") ++ inventor.nameMiddles.opt.map(f => if (caseSensitive) f.mkString(" ") else f.mkString(" ").toLowerCase.trimBegEnd()) ++  Some("_SUFFIXES_") ++ inventor.nameSuffixes.opt.map(f => if (caseSensitive) f.mkString(" ") else f.mkString(" ").toLowerCase.trimBegEnd())
    nameParts.mkString("")
  }
  
}
