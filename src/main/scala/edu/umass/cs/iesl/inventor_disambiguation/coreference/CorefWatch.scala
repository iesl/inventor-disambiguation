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
 * Used to print debug messages while coref is running, every N proposals
 * the canopy that is being processed is displayed.
 * @tparam Vars - type of variables
 */
trait CorefWatch[Vars <: NodeVariables[Vars] with Canopy] {
  this: CorefSampler[Vars] with PairGenerator[Vars] with MoveGenerator[Vars] =>

  var numPropsMade = 0

  val printEveryNLines = 10000

  var canopy: String = null
  
  afterInferHooks += { _ =>
    watchStatement()
  }

  private def watchStatement(): Unit = {
    println(s"Working on canopy $canopy. Total Number of proposals made: $numPropsMade")
  }

  proposalHooks += {p:Proposal[(Node[Vars], Node[Vars])] =>
    numPropsMade +=1
    if (canopy eq null) canopy = p.context._1.variables.canopies.mkString(",")
    if(numPropsMade % printEveryNLines == 0) {
      watchStatement()
    }
  }

}
