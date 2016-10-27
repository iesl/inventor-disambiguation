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

class Location extends PatentsViewRecord {
  
  val locationID = new StringSlot("locationID")
  val city = new StringSlot("city")
  val state = new StringSlot("state")
  val country = new StringSlot("country")
  
  def this(locationID: String, city: Option[String], state: Option[String], country: Option[String]) = {
    this()
    this.locationID.set(locationID)
    this.city.set(city)
    this.state.set(state)
    this.country.set(country)
  }
  
  def debugString() = (city.opt ++ state.opt ++ country.opt).mkString(" ")
}

object Location extends Location