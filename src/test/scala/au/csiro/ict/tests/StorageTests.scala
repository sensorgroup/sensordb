package au.csiro.ict.tests

import org.specs2.mutable._
import au.csiro.ict._
import redis.clients.jedis.Jedis
import org.scalatest.FunSuite
import org.scalatra.test.scalatest.ScalatraSuite
import org.joda.time.DateTime
import org.apache.hadoop.hbase.util.Bytes


class DataStoreTests extends ScalatraSuite with FunSuite {
  import SDBTestHelpers._
  val _20111 = Some(time2DateTime("20111"))
  val _20112 = Some(time2DateTime("20112"))
  val _20113 = Some(time2DateTime("20113"))
  val _20131 = Some(time2DateTime("20131"))

  val store = new RedisStore()
  test("Row Id Generator For Column Based Storage"){
    val periodIterator1 = RawLevel.createPeriod(new DateTime(),new DateTime().plusDays(1))
    periodIterator1.length should equal(2)
    val periodIterator2 = OneMinuteLevel.createPeriod(new DateTime(),new DateTime().plusDays(1))
    periodIterator2.length should equal(2)
    val periodIterator3 = FiveMinuteLevel.createPeriod(new DateTime(),new DateTime().plusDays(1))
    periodIterator3.length should equal(2)
    val periodIterator4 = FifteenMinuteLevel.createPeriod(new DateTime(),new DateTime().plusDays(1))
    periodIterator4.length should equal(2)
    val periodIterator5 = OneHourLevel.createPeriod(new DateTime(),new DateTime().plusDays(1))
    periodIterator5.length should equal(2)// brings one per year
    val periodIterator6 = ThreeHourLevel.createPeriod(new DateTime(),new DateTime().plusDays(1))
    periodIterator6.length should equal(1)// brings one per year
    val periodIterator7 = SixHourLevel.createPeriod(new DateTime(),new DateTime().plusDays(1))
    periodIterator7.length should equal(1)// brings one per year
    val periodIterator8 = OneDayLevel.createPeriod(new DateTime(),new DateTime().plusDays(1))
    periodIterator8.length should equal(1)// brings one per year
    val periodIterator9 = OneMonthLevel.createPeriod(new DateTime(),new DateTime().plusDays(1))
    periodIterator9.length should equal(1)// brings one per year
    val periodIterator10 = OneYearLevel.createPeriod(new DateTime(),new DateTime().plusDays(1))
    periodIterator10.length should equal(1)// brings one per year

  }
  test("Can Query none existing nodes" ) {

    store.drop("s1")
    val writer = new InMemWriter()
    writer.isClosed() should  be(false)
    val chunker = new DefaultChunkFormatter(writer)
    store.get(Set("s1"), _20111, Some(time2DateTime("20112")),None,RawLevel,chunker)
    chunker.item_count should equal(0)
    writer.isClosed() must equal(false)
    chunker.done()
    writer.isClosed() must equal(true)
  }
  test("Query inserting one element" ) {
    store.drop("s1")
    store.put("s1", Map(ukDateTimeToInt("02-01-2011T00:01:15")-> Some(-100.0)))
    val ts1 = ukDateTimeToInt("02-01-2011T00:01:15")
    val writer = new InMemWriter()
    store.get(Set("s1"),Some(new DateTime(ts1*1000L)),Some(new DateTime((ts1+10)*1000L)),None,RawLevel,new DefaultChunkFormatter(writer))
    writer.getData.toList should equal(List(("s1",ts1,-100.0)))
    val writer2 = new InMemWriter()
    store.get(Set("s1"), _20111,_20113,None,RawLevel,new DefaultChunkFormatter(writer2))
    writer2.getData.length should equal(1)
    val writer3 = new InMemWriter()
    store.get(Set("s1"), _20111, _20113,Some((0,74)),RawLevel,new DefaultChunkFormatter(writer3))
    writer3.getData.length should equal(0)

    val writer4 = new InMemWriter()
    store.get(Set("s1"), _20111, _20113,Some((74,75)),RawLevel,new DefaultChunkFormatter(writer4))
    writer4.getData.length should equal(1)

    val writer5 = new InMemWriter()
    store.get(Set("s1"),_20111,_20113,Some((75,86400)),RawLevel,new DefaultChunkFormatter(writer5))
    writer5.getData.length should equal(1)

    val writer6 = new InMemWriter()
    store.get(Set("s1"), _20111, _20113,Some((76,86400)),RawLevel,new DefaultChunkFormatter(writer6))
    writer6.getData.length should equal(0)

    store.drop("s1")

    val writer7 = new InMemWriter()
    store.get(Set("s1"), _20111, _20113,Some((74,75)),RawLevel,new DefaultChunkFormatter(writer7))
    writer7.getData.length should equal(0)

  }
  test("Query inserting multiple element" ) {
    store.drop("s1")
    store.put("s1", Map(ukDateTimeToInt("02-01-2011T00:01:15") -> Some(1.0),
      ukDateTimeToInt("02-01-2011T00:01:16") -> Some(2.0)))
    val writer = new InMemWriter()

    store.get(Set("s1"),_20112, _20113, None, RawLevel,new DefaultChunkFormatter(writer))
    writer.getData.length  should equal(2)
    val writer2 = new InMemWriter()
    store.get(Set("s1"), _20111, _20113, Some((0,74)),RawLevel, new DefaultChunkFormatter(writer2))
    writer2.getData.length  should equal(0)

    store.get(Set("s1"), None, None, Some((0,74)),RawLevel, new DefaultChunkFormatter(writer2))
    writer2.getData.length  should equal(0)

    val writer3 = new InMemWriter()
    store.get(Set("s1"), _20111, _20113, Some((74,75)),RawLevel, new DefaultChunkFormatter(writer3))
    writer3.getData.length should equal(1)

    val writer3_1 = new InMemWriter()
    store.get(Set("s1"), None, None, None,RawLevel, new DefaultChunkFormatter(writer3_1))
    writer3_1.getData.length should equal(2)

    val writer3_2 = new InMemWriter()
    store.get(Set("s1"), _20131, None, None,RawLevel, new DefaultChunkFormatter(writer3_2))
    writer3_2.getData.length should equal(0)
    store.get(Set("s1"), _20111, _20111, None,RawLevel, new DefaultChunkFormatter(writer3_2))
    writer3_2.getData.length should equal(0)
    store.get(Set("s1"), _20111, _20112, None,RawLevel, new DefaultChunkFormatter(writer3_2))
    writer3_2.getData.length should equal(2)

    val writer4 = new InMemWriter()
    store.get(Set("s1"), _20111, _20113, Some((75,76)), RawLevel, new DefaultChunkFormatter(writer4))
    writer4.getData.length should equal(2)

    val writer5 = new InMemWriter()
    store.get(Set("s1") ,_20111, _20113, Some((75,76)), RawLevel, new DefaultChunkFormatter(writer5))
    writer5.getData.length should equal(2)
    writer5.getData should contain(("s1",ukDateTimeToInt("02-01-2011T00:01:15"),1.0))
    writer5.getData should contain(("s1",ukDateTimeToInt("02-01-2011T00:01:16") ,2.0))

    val writer6 = new InMemWriter()
    store.get(Set("s1"), _20111, _20113,Some((76,100)), RawLevel,new DefaultChunkFormatter(writer6))
    writer6.getData.length should equal(1)
    writer6.getData should contain(("s1",ukDateTimeToInt("02-01-2011T00:01:16") ,2.0))
  }
}
