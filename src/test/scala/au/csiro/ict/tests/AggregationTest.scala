package au.csiro.ict.tests

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.scalatra.test.scalatest.ScalatraSuite
import org.apache.hadoop.hbase.util.Bytes
import akka.actor.{Props, ActorSystem}
import akka.dispatch.Await
import akka.testkit.{TestProbe, TestActorRef, TestKit}
import au.csiro.ict._
import org.joda.time.{DateTimeZone, DateTime}

class AggregationTest extends ScalatraSuite with FunSuite with BeforeAndAfterAll {

  test("Check if child cells are generated correctly") {
    val ts1 = new DateTime(2010,2,6,12,43,54,123,Utils.TZ_Sydney)
    OneMinuteLevel.getChildCells(ts1).length must equal(60)
    OneMinuteLevel.getChildCells(ts1) must contain(RawLevel.getCellKeyFor(ts1))
    for (i<-12*60*60+43*60 until 12*60*60+44*60 )
      OneMinuteLevel.getChildCells(ts1) must contain(i)

    FiveMinuteLevel.getChildCells(ts1).length must equal(5)
    FiveMinuteLevel.getChildCells(ts1) must contain(OneMinuteLevel.getCellKeyFor(ts1))
    for (i<-12*60+40 until 12*60+45 )
      FiveMinuteLevel.getChildCells(ts1) must contain(i)

    FifteenMinuteLevel.getChildCells(ts1).length must equal(3)
    FifteenMinuteLevel.getChildCells(ts1) must contain(FiveMinuteLevel.getCellKeyFor(ts1))
    for (i<-12*60+30 until 13*45 by 5 )
      FifteenMinuteLevel.getChildCells(ts1) must contain(i)

    OneHourLevel.getChildCells(ts1).length must equal(4)
    OneHourLevel.getChildCells(ts1) must contain(FifteenMinuteLevel.getCellKeyFor(ts1))
    for (i<-12*60 until 13*60 by 15)
      OneHourLevel.getChildCells(ts1) must contain(i/15)

    ThreeHourLevel.getChildCells(ts1).length must equal(3)
    ThreeHourLevel.getChildCells(ts1)  must contain(OneHourLevel.getCellKeyFor(ts1))
    for (i<-12 until 15)
      ThreeHourLevel.getChildCells(ts1) must contain(i)

    SixHourLevel.getChildCells(ts1).length must equal(2)
    SixHourLevel.getChildCells(ts1) must contain(ThreeHourLevel.getCellKeyFor(ts1))
    for (i<- ((ts1.getDayOfYear-1)*24+12)/3 until ((ts1.getDayOfYear-1)*24+18)/3)
      SixHourLevel.getChildCells(ts1) must contain(i)

    OneDayLevel.getChildCells(ts1).length must equal(4)
    OneDayLevel.getChildCells(ts1) must contain(SixHourLevel.getCellKeyFor(ts1))
    for (i<-((ts1.getDayOfYear-1)*24)/6 until ((ts1.getDayOfYear-1)*24)/6)
      OneDayLevel.getChildCells(ts1) must contain(i)

    OneMonthLevel.getChildCells(ts1).length must equal(28)
    OneMonthLevel.getChildCells(ts1) must contain(OneDayLevel.getCellKeyFor(ts1))
    for (i<-32 until 50)
      OneMonthLevel.getChildCells(ts1) must contain(i)

    OneYearLevel.getChildCells(ts1).length must equal(12)
    OneYearLevel.getChildCells(ts1) must contain(OneMonthLevel.getCellKeyFor(ts1))
    for (i<-1 until 12)
      OneYearLevel.getChildCells(ts1) must contain(i)
  }

}
