package au.csiro.ict.tests

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.scalatra.test.scalatest.ScalatraSuite
import org.joda.time.DateTime
import org.apache.hadoop.hbase.util.Bytes
import akka.actor.{Props, ActorSystem}
import akka.dispatch.Await
import akka.testkit.{TestProbe, TestActorRef, TestKit}
import au.csiro.ict._

class AggregationTest extends ScalatraSuite with FunSuite with BeforeAndAfterAll {

  test("Check if child cells are generated correctly") {
    import au.csiro.ict.ColumnOrientedDBKeyHelper._
    val ts1 = new DateTime(2010,2,6,12,43,54,123,Utils.TZ_Sydney)
    childCellsOf("s1",ts1,AggLevel.OneMin).length must equal(60)
    childCellsOf("s1",ts1,AggLevel.OneMin).map(Bytes.toInt) must contain(Bytes.toInt(cellKeyOf(ts1,AggLevel.RAW)))
    for (i<-12*60*60+43*60 until 12*60*60+44*60 )
      childCellsOf("s1",ts1,AggLevel.OneMin).map(Bytes.toInt) must contain(i)

    childCellsOf("s1",ts1,AggLevel.FiveMin).length must equal(5)
    childCellsOf("s1",ts1,AggLevel.FiveMin).map(Bytes.toInt) must contain(Bytes.toInt(cellKeyOf(ts1,AggLevel.OneMin)))
    for (i<-12*60+40 until 12*60+45 )
      childCellsOf("s1",ts1,AggLevel.FiveMin).map(Bytes.toInt) must contain(i)

    childCellsOf("s1",ts1,AggLevel.FifteenMin).length must equal(3)
    childCellsOf("s1",ts1,AggLevel.FifteenMin).map(Bytes.toInt) must contain(Bytes.toInt(cellKeyOf(ts1,AggLevel.FiveMin)))
    for (i<-12*60+30 until 13*45 by 5 )
      childCellsOf("s1",ts1,AggLevel.FifteenMin).map(Bytes.toInt) must contain(i)

    childCellsOf("s1",ts1,AggLevel.OneHour).length must equal(4)
    childCellsOf("s1",ts1,AggLevel.OneHour).map(Bytes.toInt) must contain(Bytes.toInt(cellKeyOf(ts1,AggLevel.FifteenMin)))
    for (i<-12*60 until 13*60 by 15)
      childCellsOf("s1",ts1,AggLevel.OneHour).map(Bytes.toInt) must contain(i/15)

    childCellsOf("s1",ts1,AggLevel.ThreeHour).length must equal(3)
    childCellsOf("s1",ts1,AggLevel.ThreeHour).map(Bytes.toString) must contain(Bytes.toString(cellKeyOf(ts1,AggLevel.OneHour)))
    for (i<-12 until 15)
      childCellsOf("s1",ts1,AggLevel.ThreeHour).map(Bytes.toInt) must contain(i)

    childCellsOf("s1",ts1,AggLevel.SixHour).length must equal(2)
    childCellsOf("s1",ts1,AggLevel.SixHour).map(Bytes.toString) must contain(Bytes.toString(cellKeyOf(ts1,AggLevel.ThreeHour)))
    for (i<-List(ts1.getDayOfYear+"-4",ts1.getDayOfYear+"-5"))
      childCellsOf("s1",ts1,AggLevel.SixHour).map(Bytes.toString) must contain(i)

    childCellsOf("s1",ts1,AggLevel.OneDay).length must equal(4)
    childCellsOf("s1",ts1,AggLevel.OneDay).map(Bytes.toString) must contain(Bytes.toString(cellKeyOf(ts1,AggLevel.SixHour)))
    for (i<-List(ts1.getDayOfYear+"-0",ts1.getDayOfYear+"-1"))
      childCellsOf("s1",ts1,AggLevel.OneDay).map(Bytes.toString) must contain(i)

    childCellsOf("s1",ts1,AggLevel.OneMonth).length must equal(28)
    childCellsOf("s1",ts1,AggLevel.OneMonth).map(Bytes.toInt) must contain(Bytes.toInt(cellKeyOf(ts1,AggLevel.OneDay)))
    for (i<-32 until 50)
      childCellsOf("s1",ts1,AggLevel.OneMonth).map(Bytes.toInt) must contain(i)

    childCellsOf("s1",ts1,AggLevel.OneYear).length must equal(12)
    childCellsOf("s1",ts1,AggLevel.OneYear).map(Bytes.toInt) must contain(Bytes.toInt(cellKeyOf(ts1,AggLevel.OneMonth)))
    for (i<-1 until 12)
      childCellsOf("s1",ts1,AggLevel.OneYear).map(Bytes.toInt) must contain(i)
  }

}
