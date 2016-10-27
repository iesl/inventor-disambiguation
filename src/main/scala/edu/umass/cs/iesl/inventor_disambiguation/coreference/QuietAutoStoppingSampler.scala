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
import cc.factorie.infer.Proposal

/**
 * The sampler that is used in the inventor coreference algorithm.
 * @tparam Vars
 */
trait QuietAutoStoppingSampler[Vars <: NodeVariables[Vars]] extends CorefSampler[Vars] {
  this: PairGenerator[Vars] with MoveGenerator[Vars] =>

  def autoStopThreshold:Int

  private var runOfEmptyProposals = 0


  override def processProposals(props: Seq[Proposal[(Node[Vars], Node[Vars])]]) = {
    if(props.size == 1) { // a proposal of size one is 'empty' because NoMove is always a valid choice
      runOfEmptyProposals += 1
    } else {
      runOfEmptyProposals = 0
    }
    super.processProposals(props)
  }

  override def infer() = {
    beforeInferHook
    var step = 0

    while (step < iterations && runOfEmptyProposals < autoStopThreshold) {
      process(nextContext)
      step += 1
    }
    afterInferHook
  }
}