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


package edu.umass.cs.iesl.inventor_disambiguation.db

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._

class InMemoryPrimaryKeyDatastore[Key,Value](map: scala.collection.Map[Key,Value]) extends Datastore[Key,Value]{
  override def get(key: Key): Iterable[Value] = map.get(key)
}

object InMemoryPrimaryKeyDatastore {
  def fromIterator[Key,Value](data: Iterator[(Key,Value)]) ={
    val _map = new java.util.HashMap[Key,Value](10000).asScala
    data.foreach( f => _map.put(f._1,f._2))
    new InMemoryPrimaryKeyDatastore[Key,Value](_map)
  }
}


class InMemoryDatastore[Key,Value](map: scala.collection.Map[Key,Iterable[Value]]) extends Datastore[Key,Value]{
  override def get(key: Key): Iterable[Value] = map.getOrElse(key,Iterable())
}


object InMemoryDatastore {

  def fromIterator[Key,Value](data: Iterator[(Key,Value)]) ={
    val _map = new java.util.HashMap[Key,ArrayBuffer[Value]](10000).asScala
    data.foreach( f => {
      if (!_map.contains(f._1)) {
        _map.put(f._1, new ArrayBuffer[Value]())
      }
      _map(f._1) += f._2
    })
    new InMemoryDatastore[Key,Value](_map)
  }

}