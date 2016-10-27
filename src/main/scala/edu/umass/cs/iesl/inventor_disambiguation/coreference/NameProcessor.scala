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

import java.text.Normalizer

import edu.umass.cs.iesl.inventor_disambiguation._
import edu.umass.cs.iesl.inventor_disambiguation.data_structures.PersonName

/**
 * This file contains a variety of name processing steps
 */

/**
 * An operator which processes inventor, assignee, lawyer, etc names
 */
trait NameProcessor {
  def process(name: PersonName): PersonName
}

class CompoundNameProcessor(processors: Iterable[NameProcessor]) extends NameProcessor {
  override def process(name: PersonName): PersonName = {
    var res: PersonName = null
    processors.foreach(p => res = p.process(name))
    res
  }   
}

object FirstNameTrimmer extends NameProcessor {
  override def process(name: PersonName): PersonName = {
    name.nameFirst.opt.foreach(n => name.nameFirst.set(n.trimBegEnd()))
    name
  }
}

object LastNameTrimmer extends NameProcessor {
  override def process(name: PersonName): PersonName = {
    name.nameLast.opt.foreach(n => name.nameLast.set(n.trimBegEnd()))
    name
  }
}

object FirstMiddleSplitter extends NameProcessor {
  override def process(name: PersonName): PersonName = {
    val split = name.nameFirst.opt.map(_.split("\\s")).getOrElse(Array())
    name.nameFirst.set(split.headOption)
    name.nameMiddles.set(name.nameMiddles.opt.getOrElse(Seq()) ++ split.drop(1))
    name
  }
}

object LastNameSuffixProcessor extends NameProcessor {
  override def process(name: PersonName): PersonName = {
    val (newLastname, newSuffixes) = processLastName(name.nameLast.opt.getOrElse(""))
    name.nameLast.set(newLastname)
    name.nameSuffixes.set(name.nameSuffixes.opt.getOrElse(Seq()) ++ newSuffixes)
    name
  }

  def processLastName(string: String) = {
    val splitOnComma = string.split(",").map(_.trimBegEnd())
    if (splitOnComma.isEmpty)
      println("WARNING: name is a single comma.")
    val name = splitOnComma.headOption.getOrElse("")
    val suffixes = splitOnComma.drop(1)
    (name,suffixes)
  }
}

trait AccentNameProcessor extends NameProcessor {

  val accentRegex = "\\{[a-zA-Z0-9]+ over \\(([a-zA-Z0-9\\s]+)\\)\\}".r

  def processString(stringName: String) = {
    accentRegex.replaceAllIn(stringName,regexMatch => regexMatch.group(1))
  }

}

object LastNameAccentNameProcessor extends AccentNameProcessor {
  override def process(name: PersonName): PersonName = 
    name.nameLast.set(name.nameLast.opt.map(processString))
}


object FirstNameAccentNameProcessor extends AccentNameProcessor {
  override def process(name: PersonName): PersonName =
    name.nameFirst.set(name.nameFirst.opt.map(processString))
}

trait UnicodeCharacterAccentNameProcessor extends NameProcessor {
  def processString(stringName: String)= {
    Normalizer.normalize(stringName, Normalizer.Form.NFKD).replaceAll("[^\\p{ASCII}]", "")
  }
}

object LastNameUnicodeCharacterAccentNameProcessor extends AccentNameProcessor {
  override def process(name: PersonName): PersonName =
    name.nameLast.set(name.nameLast.opt.map(processString))
}

object FirstNameUnicodeCharacterAccentNameProcessor extends AccentNameProcessor {
  override def process(name: PersonName): PersonName =
    name.nameFirst.set(name.nameFirst.opt.map(processString))
}


trait AlphabeticOnlyNameProcessor extends NameProcessor {
  val regex = "[^a-zA-Z]"
  def processString(stringName: String) = {
    stringName.replaceAll(regex,"")
  }
}

object AlphabeticOnlyLastNameProcessor extends AlphabeticOnlyNameProcessor {
  override def process(name: PersonName): PersonName =
    name.nameLast.set(name.nameLast.opt.map(processString))  
}

object AlphabeticOnlyMiddleNameProcessor extends AlphabeticOnlyNameProcessor {
  override def process(name: PersonName): PersonName =
    name.nameMiddles.set(name.nameMiddles.opt.map(c => c.map(processString)))
}

object AlphabeticOnlySuffixesProcessor extends AlphabeticOnlyNameProcessor {
  override def process(name: PersonName): PersonName =
    name.nameSuffixes.set(name.nameSuffixes.opt.map(c => c.map(processString)))
}

object AlphabeticOnlyFirstNameProcessor extends AlphabeticOnlyNameProcessor {
  override def process(name: PersonName): PersonName =
    name.nameFirst.set(name.nameFirst.opt.map(processString))
}

object AlphabeticOnlyAllNamesProcessor extends CompoundNameProcessor(Iterable(AlphabeticOnlyFirstNameProcessor,AlphabeticOnlyMiddleNameProcessor,AlphabeticOnlyLastNameProcessor,AlphabeticOnlySuffixesProcessor))

object LowerCaseFirstName extends NameProcessor {
  override def process(name: PersonName): PersonName = name.nameFirst.set(name.nameFirst.opt.map(_.toLowerCase))
}

object LowerCaseLastName extends NameProcessor {
  override def process(name: PersonName): PersonName = name.nameLast.set(name.nameLast.opt.map(_.toLowerCase))
}

object LowerCaseMiddleNames extends NameProcessor {
  override def process(name: PersonName): PersonName = name.nameMiddles.set(name.nameMiddles.opt.map(_.map(_.toLowerCase)))
}

object LowerCaseSuffixes extends NameProcessor {
  override def process(name: PersonName): PersonName = name.nameSuffixes.set(name.nameSuffixes.opt.map(_.map(_.toLowerCase)))
}

object LowerCaseAllNames extends CompoundNameProcessor(Iterable(LowerCaseFirstName,LowerCaseMiddleNames,LowerCaseLastName,LowerCaseSuffixes))

object DefaultNameProcessor extends CompoundNameProcessor(Iterable(FirstNameTrimmer,LastNameTrimmer,LastNameSuffixProcessor,LastNameAccentNameProcessor,FirstNameAccentNameProcessor))

object ReEvaluatingNameProcessor extends CompoundNameProcessor(Iterable(FirstNameTrimmer,LastNameTrimmer,LastNameSuffixProcessor,LastNameAccentNameProcessor,FirstNameAccentNameProcessor,FirstMiddleSplitter))

object CaseInsensitiveReEvaluatingNameProcessor extends CompoundNameProcessor(Iterable(FirstNameTrimmer,LastNameTrimmer,LastNameSuffixProcessor,LastNameAccentNameProcessor,FirstNameAccentNameProcessor,FirstNameUnicodeCharacterAccentNameProcessor,LastNameUnicodeCharacterAccentNameProcessor,FirstMiddleSplitter,AlphabeticOnlyAllNamesProcessor,LowerCaseAllNames))