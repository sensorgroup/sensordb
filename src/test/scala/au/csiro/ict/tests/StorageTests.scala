package au.csiro.ict.tests

import org.specs2.mutable._
import au.csiro.ict._
import org.joda.time.DateTime
import redis.clients.jedis.Jedis
import org.scalatest.FunSuite
import org.scalatra.test.scalatest.ScalatraSuite


class DataStoreTests extends ScalatraSuite with FunSuite {
  import SDBTestHelpers._

  val store = new HbaseStorage()

  test("Can Query none existing nodes" ) {
    store.drop("s1")
    val writer = new InMemWriter()
    writer.isClosed() should  be(false)
    val chunker = new DefaultChunkFormatter(writer)
    store.get("s1", time2Int("20111"), time2Int("20112"),Utils.TZ_Sydney).foreach(x=>chunker.insert("s1",x._1,x._2))
    chunker.done()
    chunker.count should equal(0)
    writer.isClosed() must equal(true)
  }
  test("Query inserting one element" ) {
    store.drop("s1")
    store.put("s1", Map(ukDateTimeToInt("02-01-2011T00:01:15",Utils.TZ_Sydney)-> Some(-100.0)),Utils.TZ_Sydney)
    val ts1 = ukDateTimeToInt("02-01-2011T00:01:15",Utils.TZ_Sydney)
    store.get("s1",ts1,ts1+10,Utils.TZ_Sydney).hasNext must equal(true)
    store.get("s1",ts1,ts1+10,Utils.TZ_Sydney).next() should equal((ts1,-100.0))
    val writer = new InMemWriter()
    new StorageStreamDayIdGenerator(List("s1"), time2DateTime("20111"), time2DateTime("20112")).length should equal(2)
    store.get("s1", time2Int("20111"), time2Int("20113"),Utils.TZ_Sydney).foldLeft(new DefaultChunkFormatter(writer))((sum,item)=>sum.insert("s1",item._1,item._2)).done()
    writer.getData.length should equal(1)
    val writer2 = new InMemWriter()
    store.get("s1", time2Int("20111"), time2Int("20113"), 0, 75,Utils.TZ_Sydney).foldLeft(new DefaultChunkFormatter(writer2))((sum,item)=>sum.insert("s1",item._1,item._2)).done()
    writer2.getData.length should equal(0)
    //
    val writer3 = new InMemWriter()
    store.get("s1", time2Int("20111"), time2Int("20113"), 74, 75,Utils.TZ_Sydney).foldLeft(new DefaultChunkFormatter(writer3))((sum,item)=>sum.insert("s1",item._1,item._2)).done()
    writer3.getData.length  should equal(0)
    //
    val writer4 = new InMemWriter()
    store.get("s1", time2Int("20111"), time2Int("20113"), 75, 76,Utils.TZ_Sydney,new DefaultChunkFormatter(writer4))
    writer4.getData.length should equal(1)

    val writer5 = new InMemWriter()
    store.get("s1", time2Int("20112"), time2Int("20113"), 75, 86400,Utils.TZ_Sydney, new DefaultChunkFormatter(writer5))
    writer5.getData.head should equal(("s1",ts1,-100.0))

    val writer6 = new InMemWriter()
    store.get("s1", time2Int("20112"), time2Int("20113"), 76, 86400,Utils.TZ_Sydney, new DefaultChunkFormatter(writer6))
    writer6.getData.length  should equal(0)

    store.drop("s1")
    val writer7 = new InMemWriter()
    store.get("s1", time2Int("20111"), time2Int("20113"), 75, 76,Utils.TZ_Sydney,new DefaultChunkFormatter(writer7))
    writer7.getData.length  should equal(0) // because drop is called

  }
  test("Query inserting multiple element" ) {
    store.drop("s1")
    val _20111=time2Int("20112")
    val _20112=time2Int("20112")
    val _20113=time2Int("20113")
    store.put("s1", Map(ukDateTimeToInt("02-01-2011T00:01:15",Utils.TZ_Sydney) -> Some(1.0),
      ukDateTimeToInt("02-01-2011T00:01:16",Utils.TZ_Sydney) -> Some(2.0)),Utils.TZ_Sydney)
    val writer = new InMemWriter()

    store.get("s1",_20112, _20113, Utils.TZ_Sydney,new DefaultChunkFormatter(writer))
    writer.getData.length  should equal(2)
    val writer2 = new InMemWriter()
    store.get("s1", _20111, _20113,0,75,Utils.TZ_Sydney,new DefaultChunkFormatter(writer2))
    writer2.getData.length  should equal(0)

    val writer3 = new InMemWriter()
    store.get("s1", _20111, _20113,74,75,Utils.TZ_Sydney,new DefaultChunkFormatter(writer3))
    writer3.getData.length should equal(0)

    val writer4 = new InMemWriter()
    store.get("s1", _20111, _20113,75,76,Utils.TZ_Sydney,new DefaultChunkFormatter(writer4))
    writer4.getData.length should equal(1)
    //
    //      val writer5 = new InMemWriter()
    //      store.get("s1" ,_20111, _20113, 75, 86400,Utils.TZ_Sydney, new DefaultChunkFormatter(writer5))
    //      writer5.getData.length should equal(2)
    //      writer5.getData should contain(("s1",1293926475,1.0))
    //      writer5.getData should contain(("s1",1293926476,2.0))
    //
    //
    //      val writer6 = new InMemWriter()
    //      store.get("s1", _20111, _20113, 76, 86400, Utils.TZ_Sydney,new DefaultChunkFormatter(writer6))
    //      writer6.getData.length should equal(1)
    //      writer6.getData should contain(("s1",1293926476,2.0))

  }
}
