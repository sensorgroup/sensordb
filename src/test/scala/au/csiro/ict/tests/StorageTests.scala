package au.csiro.ict.tests

import org.specs2.mutable._
import au.csiro.ict._
import org.joda.time.DateTime


class DataStoreTests extends Specification {
  args(sequential = true)

  def time2Int(x:String) = (Utils.yyyyDDDFormat.parseDateTime(x).getMillis/1000L).asInstanceOf[Int]
  def time2DateTime(x:String) = new DateTime(Utils.yyyyDDDFormat.parseDateTime(x).getMillis)

  "Habase should be able to store and retrive sensor data" should {
    val c = new HbaseStorage()

    "Can Query none existing nodes" in {
      c.drop("s1")
      val writer = new InMemWriter()
      writer.isClosed() must beFalse
      val chunker = new DefaultChunkFormatter(writer)
      c.get("s1", time2Int("20111"), time2Int("20112")).foreach(x=>chunker.insert("s1",x._1,x._2))
      chunker.done()
      chunker.count must equalTo(0)
      writer.isClosed() must beTrue
    }
    "Query inserting one element" in {
      c.drop("s1")
      c.put("s1", Map(Utils.ukDateTimeToInt("02-01-2011T00:01:15")-> Some(-100.0)))
      val ts1 = Utils.ukDateTimeToInt("02-01-2011T00:01:15")
      c.get("s1",ts1,ts1+10).hasNext must_== true
      c.get("s1",ts1,ts1+10).next() should equalTo((ts1,-100.0))

      val writer = new InMemWriter()
      new StorageStreamDayIdGenerator(List("s1"), time2DateTime("20111"), time2DateTime("20112")).length must_== 2
      c.get("s1", time2Int("20111"), time2Int("20113")).foldLeft(new DefaultChunkFormatter(writer))((sum,item)=>sum.insert("s1",item._1,item._2)).done()
      writer.getData.length must_== 1
      val writer2 = new InMemWriter()
      c.get("s1", time2Int("20111"), time2Int("20113"), 0, 75).foldLeft(new DefaultChunkFormatter(writer2))((sum,item)=>sum.insert("s1",item._1,item._2)).done()
      writer2.getData.length must_== 0

      val writer3 = new InMemWriter()
      c.get("s1", time2Int("20111"), time2Int("20113"), 74, 75).foldLeft(new DefaultChunkFormatter(writer3))((sum,item)=>sum.insert("s1",item._1,item._2)).done()
      writer3.getData.length must_== 0

      val writer4 = new InMemWriter()
      c.get("s1", time2Int("20111"), time2Int("20113"), 75, 76,new DefaultChunkFormatter(writer4))
      writer4.getData.length must_== 1

      val writer5 = new InMemWriter()
      c.get("s1", time2Int("20112"), time2Int("20113"), 75, 86400, new DefaultChunkFormatter(writer5))
      writer5.getData.head must_== ("s1",ts1,-100.0)

      val writer6 = new InMemWriter()
      c.get("s1", time2Int("20112"), time2Int("20113"), 76, 86400, new DefaultChunkFormatter(writer6))
      writer6.getData.length must_== 0

      c.drop("s1")
      val writer7 = new InMemWriter()
      c.get("s1", time2Int("20111"), time2Int("20113"), 75, 76,new DefaultChunkFormatter(writer7))
      writer7.getData.length must_== 0 // because drop is called

    }
    "Query inserting multiple element" in {
      c.drop("s1")
      val _20111=time2Int("20112")
      val _20112=time2Int("20112")
      val _20113=time2Int("20113")
      c.put("s1", Map(Utils.ukDateTimeToInt("02-01-2011T00:01:15") -> Some(1.0),
        Utils.ukDateTimeToInt("02-01-2011T00:01:16") -> Some(2.0)))
      val writer = new InMemWriter()

      c.get("s1",_20112, _20113, new DefaultChunkFormatter(writer))
      writer.getData.length must_== 2
      val writer2 = new InMemWriter()
      c.get("s1", _20111, _20113,0,75,new DefaultChunkFormatter(writer2))
      writer2.getData.length must_== 0

      val writer3 = new InMemWriter()
      c.get("s1", _20111, _20113,74,75,new DefaultChunkFormatter(writer3))
      writer3.getData.length must_== 0

      val writer4 = new InMemWriter()
      c.get("s1", _20111, _20113,75,76,new DefaultChunkFormatter(writer4))
      writer4.getData.length must_== 1

      val writer5 = new InMemWriter()
      c.get("s1" ,_20111, _20113, 75, 86400, new DefaultChunkFormatter(writer5))
      writer5.getData.length must_== 2
      writer5.getData should contain(("s1",1293926475,1.0))
      writer5.getData should contain(("s1",1293926476,2.0))


      val writer6 = new InMemWriter()
      c.get("s1", _20111, _20113, 76, 86400, new DefaultChunkFormatter(writer6))
      writer6.getData.length must_== 1
      writer6.getData should contain(("s1",1293926476,2.0))

    }
  }
}
