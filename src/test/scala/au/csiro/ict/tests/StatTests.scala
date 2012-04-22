package au.csiro.ict.tests

import org.scalatest.FunSuite
import org.scalatra.test.scalatest.ScalatraSuite
import au.csiro.ict.{Cache, Utils, StreamStatistics}
import org.apache.commons.math3.stat.descriptive.{StatisticalSummaryValues, AggregateSummaryStatistics}
import org.apache.commons.math3.util.MathUtils


class StatTests extends ScalatraSuite with FunSuite {
  val sid = "test-" + Math.random.toString
  val ts1 = (System.currentTimeMillis() / 1000L).asInstanceOf[Int]
  val ts2 = ts1 + 100
  val ts3 = ts1 + 200
  val value1 = 101.0
  val value2 = 102.0
  val value3 = 103.0

  var mock_called = List[String]();

  def mock(key: String): Unit = {
    mock_called = key :: mock_called
  }

  test("inserting new sensor readings to stats") {
    StreamStatistics.updateStatistics(sid, ts1, Some(value1), mock) // insertion
    mock_called.length should equal(0)

    StreamStatistics.updateStatistics(sid, ts2, Some(value2), mock) // insertion
    mock_called.length should equal(0)
  }
  test("updating an existing sensor reading ") {
    StreamStatistics.updateStatistics(sid, ts1, Some(value1), mock) // update
    mock_called.length should equal(1)
    mock_called.head should include(sid)
    mock_called.head should include(Utils.TIMESTAMP_YYYYD_FORMAT.print(ts1*1000L))

    StreamStatistics.updateStatistics(sid, ts2, Some(value2), mock) // update
    mock_called.length should equal(2)
    mock_called.head should include(sid)
    mock_called.head should include(Utils.TIMESTAMP_YYYYD_FORMAT.print(ts2*1000L))
  }

  test("deleting existing sensor readings") {

    StreamStatistics.updateStatistics(sid, ts1, None, mock) // remove
    mock_called.length should equal(3)
    mock_called.head should include(sid)
    mock_called.head should include(Utils.TIMESTAMP_YYYYD_FORMAT.print(ts1*1000L))

    StreamStatistics.updateStatistics(sid, ts2, None, mock) // remove
    mock_called.length should equal(4)
    mock_called.head should include(sid)
    mock_called.head should include(Utils.TIMESTAMP_YYYYD_FORMAT.print(ts2*1000L))

  }
  test("deleting already deleted sensor readings") {
    StreamStatistics.updateStatistics(sid, ts1, None, mock) // remove
    mock_called.length should equal(4) //no change from previous round

  }
  test("deleting non existing sensor readings") {
    StreamStatistics.updateStatistics(sid, ts3, None, mock) // remove of non exsiting item
    mock_called.length should equal(4) //no change from previous round
  }
  test("Inserting on already deleted sensor readings") {
    StreamStatistics.updateStatistics(sid, ts1, None, mock) // insert
    mock_called.length should equal(4)

    StreamStatistics.updateStatistics(sid, ts2, None, mock) // insert
    mock_called.length should equal(4)
  }
  test("Actual min,max,sum,count,std calculations") {
   val List(max_1,min_1,count_1,sum_1,sum2_1) = StreamStatistics.calculateStatTable(-10,Double.MinValue,Double.MaxValue,0,0,0)
   val List(max_2,min_2,count_2,sum_2,sum2_2) = StreamStatistics.calculateStatTable(100,max_1,min_1,count_1,sum_1,sum2_1)
   val List(max_3,min_3,count_3,sum_3,sum2_3) = StreamStatistics.calculateStatTable(11,max_2,min_2,count_2,sum_2,sum2_2)
    max_3 should equal(100.0)
    min_3 should equal(-10)
    sum_3 should equal(101)
    count_3 should equal(3)

    StreamStatistics.calculateStandardDev(count_3,sum_3,sum2_3).toString should startWith("58.39")
  }
}
