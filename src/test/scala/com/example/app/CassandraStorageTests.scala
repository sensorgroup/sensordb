package com.example.app

import org.specs2.mutable._
import au.csiro.ict._
import com.mongodb.casbah.commons.MongoDBObject


class SensorDB extends Specification() {
  "MongoDB should be able to store and retrive user information" should {
    "CRUD on Users" in {
        User.find(MongoDBObject()).length must_== 0
        User.save(new User("ali", "secret", -120))
        User.save(new User("ali3", "secret", -120))
        User.find(MongoDBObject()).length must_== 2
        User.findByName("ali").isDefined must beTrue
        User.findByName("ali2").isDefined must beFalse
        User.dropByName("ali2")
        User.find(MongoDBObject()).length must_== 2
        User.dropByName("ali3")
        User.find(MongoDBObject()).length must_== 1
        User.dropByName("ali")
        User.find(MongoDBObject()).length must_== 0
       1 must_== 1
    }
  }
  "Cassandra should be able to store and retrive sensor data" should {
    val c = new CassandraDataStore()
    val n = "testnode"
//    "Can iterate over rows matching a regex" in {
//      c.addNodeData(n, Map("s1" -> Map("2011-01-02T00:01:15" -> "-100")))
//      c.addNodeData(n, Map("s2" -> Map("2011-01-02T00:01:15" -> "-101")))
//      c.addNodeData(n, Map("s1" -> Map("2011-02-02T00:01:15" -> "-102")))
//    }
    "Can Query none existing nodes" in {
      c.dropNode(n)
      val writer = new InMemWriter()
      writer.isClosed() must beFalse

      c.queryNode(n, new KeyListIterator(List("s1"), "20111", "20112"), None, new DefaultChunkFormatter(writer))
      writer.isClosed() must beTrue
    }
//
    "Query inserting one element" in {
      c.dropNode(n)
      c.addNodeData(n, Map("s1" -> Map("2011-01-02T00:01:15" -> "-100")))
      val writer = new InMemWriter()
      new KeyListIterator(List("s1"), "20111", "20112").length must_== 2
      c.queryNode(n, new KeyListIterator(List("s1"), "20111", "20112"), None, new DefaultChunkFormatter(writer))
      writer.getData.length must_== 1
      val writer2 = new InMemWriter()
      c.queryNode(n, new KeyListIterator(List("s1"), "20111", "20112"), Option(0, 74), new DefaultChunkFormatter(writer2))
      writer2.getData.length must_== 0

      val writer3 = new InMemWriter()
      c.queryNode(n, new KeyListIterator(List("s1"), "20111", "20112"), Option(74, 74), new DefaultChunkFormatter(writer3))
      writer3.getData.length must_== 0

      val writer4 = new InMemWriter()
      c.queryNode(n, new KeyListIterator(List("s1"), "20111", "20112"), Option(75, 75), new DefaultChunkFormatter(writer4))
      writer4.getData.length must_== 1

      val writer5 = new InMemWriter()
      c.queryNode(n, new KeyListIterator(List("s1"), "20111", "20112"), Option(75, 86400), new DefaultChunkFormatter(writer5))
      writer5.getData.length must_== 1
      writer5.getData.head.last must_== "-100"

      val writer6 = new InMemWriter()
      c.queryNode(n, new KeyListIterator(List("s1"), "20111", "20112"), Option(76, 86400), new DefaultChunkFormatter(writer6))
      writer6.getData.length must_== 0
    }
    "Query inserting multiple element" in {
      c.dropNode(n)
      c.addNodeData(n, Map("s1" -> Map("2011-01-02T00:01:15" -> "1", "2011-01-02T00:01:16" -> "2")))
      val writer = new InMemWriter()
      new KeyListIterator(List("s1"), "20111", "20112").length must_== 2
      c.queryNode(n, new KeyListIterator(List("s1"), "20111", "20112"), None, new DefaultChunkFormatter(writer))
      writer.getData.length must_== 2
      val writer2 = new InMemWriter()
      c.queryNode(n, new KeyListIterator(List("s1"), "20111", "20112"), Option(0, 74), new DefaultChunkFormatter(writer2))
      writer2.getData.length must_== 0

      val writer3 = new InMemWriter()
      c.queryNode(n, new KeyListIterator(List("s1"), "20111", "20112"), Option(74, 74), new DefaultChunkFormatter(writer3))
      writer3.getData.length must_== 0

      val writer4 = new InMemWriter()
      c.queryNode(n, new KeyListIterator(List("s1"), "20111", "20112"), Option(75, 75), new DefaultChunkFormatter(writer4))
      writer4.getData.length must_== 1

      val writer5 = new InMemWriter()
      c.queryNode(n, new KeyListIterator(List("s1"), "20111", "20112"), Option(75, 86400), new DefaultChunkFormatter(writer5))
      writer5.getData.length must_== 2
      writer5.getData.last.last must_== "1"
      writer5.getData.head.last must_== "2"

      val writer6 = new InMemWriter()
      c.queryNode(n, new KeyListIterator(List("s1"), "20111", "20112"), Option(76, 86400), new DefaultChunkFormatter(writer6))
      writer6.getData.length must_== 1
      writer5.getData.head.last must_== "2"

    }

    "Query inserting multiple element in Multiple sensors" in {
      c.dropNode(n)
      c.addNodeData(n, Map("s1" -> Map("2011-01-02T00:01:15" -> "15", "2011-01-02T00:01:16" -> "16"), "s2" -> Map("2011-01-02T00:01:17" -> "17", "2011-01-02T00:01:16" -> "16")))

      val writer = new InMemWriter()
      new KeyListIterator(List("s1"), "20111", "20112").length must_== 2
      c.queryNode(n, new KeyListIterator(List("s1", "s2"), "20111", "20112"), None, new DefaultChunkFormatter(writer))
      writer.getData.length must_== 4

      val writer4 = new InMemWriter()
      c.queryNode(n, new KeyListIterator(List("s1", "s2"), "20111", "20112"), Option(76, 76), new DefaultChunkFormatter(writer4))
      writer4.getData.length must_== 2

      val writer5 = new InMemWriter()
      c.queryNode(n, new KeyListIterator(List("s1", "s2"), "20111", "20112"), Option(76, 86400), new DefaultChunkFormatter(writer5))
      writer5.getData.length must_== 3
    }
  }
}
