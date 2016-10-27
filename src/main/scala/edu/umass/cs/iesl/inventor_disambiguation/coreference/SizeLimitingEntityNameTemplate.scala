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

import cc.factorie._
import cc.factorie.app.nlp.hcoref.{DebuggableTemplate, Node, NodeVariables}
import cc.factorie.model.TupleTemplateWithStatistics3
import cc.factorie.variable.BagOfWordsVariable

import scala.reflect.ClassTag



class SizeLimitingEntityNameTemplate[Vars <: NodeVariables[Vars]](val firstLetterWeight:Double=4.0, val fullNameWeight:Double=4.0,val weight:Double=64,val saturation:Double=128.0, val penaltyOnNoName:Double=2.0, val sizeLimit: Int = 30, val exceedLimitPenalty: Double = 100, getBag:(Vars => BagOfWordsVariable), bagName:String = "")(implicit ct:ClassTag[Vars], params:Parameters)
  extends TupleTemplateWithStatistics3[Node[Vars]#Exists,Node[Vars]#IsRoot,Vars]
  with DebuggableTemplate {

  val name = "SizeLimitingEntityNameTemplate: %s".format(bagName)

  def unroll1(exists: Node[Vars]#Exists) = Factor(exists, exists.node.isRootVar, exists.node.variables)
  def unroll2(isRoot: Node[Vars]#IsRoot) = Factor(isRoot.node.existsVar, isRoot, isRoot.node.variables)
  def unroll3(vars: Vars) = Factor(vars.node.existsVar, vars.node.isRootVar, vars)


  override def score(exists: Node[Vars]#Exists#Value, isRoot: Node[Vars]#IsRoot#Value, vars: Vars) = {
    var score = 0.0
    var firstLetterMismatches = 0
    var nameMismatches = 0
    val bag = getBag(vars)
    val uniqueEntries = bag.value.asHashMap.keySet
    if (uniqueEntries.size > sizeLimit) {
      score -= math.min(saturation,exceedLimitPenalty)
    } else {
      bag.value.asHashMap.keySet.pairs.foreach { case (tokI, tokJ) =>
        if (tokI.charAt(0) != tokJ.charAt(0)) {
          firstLetterMismatches += 1
        }
        if (tokI.length > 1 && tokJ.length > 1) {
          nameMismatches += tokI editDistance tokJ
        }
      }
      score -= math.min(saturation, firstLetterMismatches * firstLetterWeight)
      score -= math.min(saturation, nameMismatches * fullNameWeight)
      if (bag.size == 0 && isRoot.booleanValue) {
        score -= penaltyOnNoName
      }
    }
    report(score, weight)
    score * weight
  }
}