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


class Application extends PatentsViewRecord {
  
  val applicationID = new StringSlot("applicationID")
  val applicationType = new StringSlot("applicationType")
  val number = new StringSlot("number")
  val country = new StringSlot("country")
  val date = new StringSlot("date")
  
  def this(applicationID: String, patentID: String, applicationType: String, number: String, country: String, date: String) = {
    this()
    this.applicationID.set(applicationID)
    this.patentID.set(patentID)
    this.applicationType.set(applicationType)
    this.number.set(number)
    this.country.set(country)
    this.date.set(date)
  }

  def this(applicationID: Option[String], patentID: Option[String], applicationType: Option[String], number: Option[String], country: Option[String], date: Option[String]) = {
    this()
    this.applicationID.set(applicationID)
    this.patentID.set(patentID)
    this.applicationType.set(applicationType)
    this.number.set(number)
    this.country.set(country)
    this.date.set(date)
  }
}

object Application extends Application

