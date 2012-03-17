package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import sensordb._

class HelloWorldSpec extends Specification {

  "The 'Hello world' string" should {
    val c = new CassandraDataStore()
    val n = "testnode"

    "Can Query none existing nodes" in {
      c.dropNode(n)
      val writer = new InMemWriter()
      writer.isClosed() must beFalse
      c.queryNode(n,new KeyGen(List("s1"),"20111","20112"),None,new DefaultChunkFormatter(writer))
      writer.isClosed() must beTrue
    }

    "Query inserting one element" in {
      c.dropNode(n)
      c.addNodeData(n,Map("s1"->Map("2011-01-02T00:01:15"->"-100")))
      val writer = new InMemWriter()
      new KeyGen(List("s1"),"20111","20112").length must_== 2
      c.queryNode(n,new KeyGen(List("s1"),"20111","20112"),None,new DefaultChunkFormatter(writer))
      writer.getData.length must_==  1
      val writer2= new InMemWriter()
      c.queryNode(n,new KeyGen(List("s1"),"20111","20112"),Option(0,74),new DefaultChunkFormatter(writer2))
      writer2.getData.length must_==  0

      val writer3= new InMemWriter()
      c.queryNode(n,new KeyGen(List("s1"),"20111","20112"),Option(74,74),new DefaultChunkFormatter(writer3))
      writer3.getData.length must_==  0

      val writer4= new InMemWriter()
      c.queryNode(n,new KeyGen(List("s1"),"20111","20112"),Option(75,75),new DefaultChunkFormatter(writer4))
      writer4.getData.length must_==  1

      val writer5= new InMemWriter()
      c.queryNode(n,new KeyGen(List("s1"),"20111","20112"),Option(75,86400),new DefaultChunkFormatter(writer5))
      writer5.getData.length must_==  1
      writer5.getData.head.last must_==  "-100"

      val writer6= new InMemWriter()
      c.queryNode(n,new KeyGen(List("s1"),"20111","20112"),Option(76,86400),new DefaultChunkFormatter(writer6))
      writer6.getData.length must_==  0
    }
    "Query inserting multiple element" in {
      c.dropNode(n)
      c.addNodeData(n,Map("s1"->Map("2011-01-02T00:01:15"->"1","2011-01-02T00:01:16"->"2")))
      val writer = new InMemWriter()
      new KeyGen(List("s1"),"20111","20112").length must_== 2
      c.queryNode(n,new KeyGen(List("s1"),"20111","20112"),None,new DefaultChunkFormatter(writer))
      writer.getData.length must_==  2
      val writer2= new InMemWriter()
      c.queryNode(n,new KeyGen(List("s1"),"20111","20112"),Option(0,74),new DefaultChunkFormatter(writer2))
      writer2.getData.length must_==  0

      val writer3= new InMemWriter()
      c.queryNode(n,new KeyGen(List("s1"),"20111","20112"),Option(74,74),new DefaultChunkFormatter(writer3))
      writer3.getData.length must_==  0

      val writer4= new InMemWriter()
      c.queryNode(n,new KeyGen(List("s1"),"20111","20112"),Option(75,75),new DefaultChunkFormatter(writer4))
      writer4.getData.length must_==  1

      val writer5= new InMemWriter()
      c.queryNode(n,new KeyGen(List("s1"),"20111","20112"),Option(75,86400),new DefaultChunkFormatter(writer5))
      writer5.getData.length must_==  2
      writer5.getData.last.last must_==  "1"
      writer5.getData.head.last must_==  "2"

      val writer6= new InMemWriter()
      c.queryNode(n,new KeyGen(List("s1"),"20111","20112"),Option(76,86400),new DefaultChunkFormatter(writer6))
      writer6.getData.length must_==  1
      writer5.getData.head.last must_==  "2"

    }

    "Query inserting multiple element in Multiple sensors" in {
      c.dropNode(n)
      c.addNodeData(n,Map("s1"->Map("2011-01-02T00:01:15"->"15","2011-01-02T00:01:16"->"16"),"s2"->Map("2011-01-02T00:01:17"->"17","2011-01-02T00:01:16"->"16")))

      val writer = new InMemWriter()
      new KeyGen(List("s1"),"20111","20112").length must_== 2
      c.queryNode(n,new KeyGen(List("s1","s2"),"20111","20112"),None,new DefaultChunkFormatter(writer))
      writer.getData.length must_==  4

      val writer4= new InMemWriter()
      c.queryNode(n,new KeyGen(List("s1","s2"),"20111","20112"),Option(76,76),new DefaultChunkFormatter(writer4))
      writer4.getData.length must_==  2

      val writer5= new InMemWriter()
      c.queryNode(n,new KeyGen(List("s1","s2"),"20111","20112"),Option(76,86400),new DefaultChunkFormatter(writer5))
      writer5.getData.length must_==  3
    }

  }
}