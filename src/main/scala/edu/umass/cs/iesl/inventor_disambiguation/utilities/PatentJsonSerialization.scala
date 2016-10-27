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


package edu.umass.cs.iesl.inventor_disambiguation.utilities

import cc.factorie.util._
import org.json4s.JsonAST.JObject
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization

import scala.collection.mutable


/**
  * Modified version of implementation in factorie to support the BSON object ids
  * that will in records
  */
object PatentJsonSerialization {
  implicit val formats = Serialization.formats(NoTypeHints)

  def fromJsonString[C <: Cubbie](s: String,con: () => C): Option[C] = {
    parseOpt(s).map(jvalue => JsonCubbieConverter.toCubbie(jvalue.asInstanceOf[JObject], con))
  }

  def toJsonString(c:Cubbie): String = compact(render(toJson(c)))

  def toJson(c:Cubbie):JObject = {
    def toJsonImpl(a:Any):JValue = {
      a match {
        case null => JNull
        case is:IntSeq => is._rawArray.slice(0,is.length).map(toJsonImpl).toIterable
        case ds:DoubleSeq => ds._rawArray.slice(0,ds.length).map(toJsonImpl).toIterable
        case m:mutable.Map[_,_] =>
          m.toMap.map{case (k,v) => k.toString -> toJsonImpl(v)}
        case it:Iterable[_] =>
          it.map(toJsonImpl)
        case i:Int => i
        case l:Long => l
        case d:Double => d
        case f:Float => f
        case b:Boolean => b
        case s:String => s
        case d:java.util.Date => d.toString

        case id: org.bson.types.ObjectId => id.toString
        case Some(an) => toJsonImpl(an)
        case None => JNothing
      }}
    toJsonImpl(c._map).asInstanceOf[JObject]
  }

  def toCubbie[C <: Cubbie](jObj:JObject, con:() => C):C = JsonCubbieConverter.toCubbie(jObj,con)

}
