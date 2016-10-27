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

import cc.factorie.app.nlp.hcoref._

/**
 * The canopy of a mention can be edited 
 */
trait MutableSingularCanopy extends SingularCanopy {
  var canopy:String
}

/**
 * A disambiguation system which uses multiple canopies in succession. The canopies
 * must be ordered from specific to general. After running inference with a particular
 * canopy assignment, inference is run on the resulting entity nodes (root nodes) with the next
 * canopy function applied.  
 * @tparam V - The variable type of the node variables
 * @tparam CanopyInput - the input to the canopy generation functions
 */
trait MultiCanopy[V <: NodeVariables[V] with MutableSingularCanopy, CanopyInput] extends HierarchicalCorefSystem[V] {

  /**
   * A canopy function is a mapping from some input to a string
   */
  type CanopyFunction = CanopyInput => String
  
  /**
   * The canopy functions to be used 
   * @return
   */
  def canopyFunctions: Iterable[CanopyFunction]
  
  /**
   * The most general of the canopy functions is the last one to be applied
   * This is useful as it can be used to block the larger group of mentions into smaller groups 
   * @return
   */
  def mostGeneralFunction: CanopyFunction = canopyFunctions.last
  
  /**
   * Select the input to the canopy function from a node's variables 
   * @param vars
   * @return
   */
  def getCanopyInput(vars: V): CanopyInput

  /**
   * Assign a canopy to each node in the tree. 
   * @param function - canopy function
   * @param mentions - the mention nodes 
   */
  def assignCanopy(function: CanopyFunction, mentions: Iterable[Mention[V]]): Unit = mentions.foreach{
    case mention =>
      val canopy = function(getCanopyInput(mention.variables))
      mention.variables.canopy = canopy
      propagateAssignment(canopy,mention.getParent)
  }
  
  /**
   * Move the canopy assignment of each mention up the tree towards its ancestors.
   * @param canopy - assignment
   * @param node - the node
   */
  def propagateAssignment(canopy: String, node: Option[Node[V]]): Unit = if (node.isDefined) {
      node.get.variables.canopy = canopy
      propagateAssignment(canopy, node.get.getParent)
  }
   
  /**
   * For a given group of nodes defined by some canopy fucntion, apply the next canopy functions in succession/  
   * @param canopyFunctions - the canopy functions to use
   * @param groupNodes - the nodes to run coreference on
   * @param mentions - the original mention nodes used for canopy assignment
   */
  def runInGroup(canopyFunctions: Iterable[CanopyFunction], groupNodes: Iterable[Node[V]], mentions: Iterable[Mention[V]]): Unit = {
    if (canopyFunctions.nonEmpty) {
      val cf = canopyFunctions.head
      assignCanopy(cf,mentions)
      val subgroups = groupNodes.groupBy(m => m.variables.canopy)
      subgroups.foreach {
        case (subgroup, mentionsInSubGroup) =>
          try {
            corefPrintln(s"[$name] Applying canopy function: $cf")
            val start = System.currentTimeMillis()
            sampler(mentionsInSubGroup).infer()
            corefPrintln(s"[$name] Finished Coref. Total time: ${System.currentTimeMillis() - start} ms")
          } catch {
            case e: Exception =>
              corefPrintln(s"[$name] Failure:")
              e.printStackTrace()
          }
      }
      val numCanopiesThisRound = groupNodes.map(_.variables.canopy).toSet.size
      // If there was only one canopy in this round, then we are done. The next clustering by canopy
      // will be identical to this one.
      if (numCanopiesThisRound != 1)
        runInGroup(canopyFunctions.drop(1),determineNextRound(groupNodes),mentions)
    }
  }
  
  /**
   * Choose which nodes to be used in the next round
   * @param mentions - the nodes from the previous round
   * @return
   */
  def determineNextRound(mentions: Iterable[Node[V]]): Iterable[Node[V]]
  

  override def run() = {
    corefPrintln(s"[$name] Running Coreference Experiment.")
    val groupped = mentions.groupBy(m => mostGeneralFunction(getCanopyInput(m.variables)))
    val numTotalCanopies = groupped.size
    groupped.toIterable.zipWithIndex.foreach {
      case ((group, mentionsInGroup),idx) =>
        corefPrintln(s"[$name] Processing Group #$idx of $numTotalCanopies, $group, with ${mentionsInGroup.size} mentions")
        runInGroup(canopyFunctions,mentionsInGroup,mentionsInGroup)
    }
  }
  
  override def runPar(numThreads: Int) = {
    corefPrintln(s"[$name] Running Coreference Experiment.")
    val groupped = mentions.groupBy(m => mostGeneralFunction(getCanopyInput(m.variables)))
    val numTotalCanopies = groupped.size
    groupped.toIterable.zipWithIndex.grouped(numThreads).toIterable.par.foreach(_.foreach {
      case ((group, mentionsInGroup),idx) =>
        corefPrintln(s"[$name] Processing Group #$idx of $numTotalCanopies, $group, with ${mentionsInGroup.size} mentions")
        runInGroup(canopyFunctions,mentionsInGroup,mentionsInGroup)
    })
  }
}
