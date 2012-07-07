package au.csiro.ict.tests

import org.scalatest.matchers.ShouldMatchers
import akka.testkit.TestKit
import akka.testkit.TestActorRef
import akka.dispatch.Future
import akka.actor._
import Actor._
import au.csiro.ict.{Done, StaticAggregator, RedisStore}
import org.scalatest.{FunSuite, WordSpec, BeforeAndAfterAll}

import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import org.scalatra.test.scalatest.ScalatraSuite
import au.csiro.ict._
import org.joda.time.DateTime

class StaticAggregatorActorTests(_system: ActorSystem) extends TestKit(_system) with  ScalatraSuite with ImplicitSender with FunSuite with BeforeAndAfterAll   {

  def this() = this(ActorSystem())

  override def afterAll {
    system.shutdown()
  }

  val store = new RedisStore()
  val staticAggregator = system.actorOf(Props(new StaticAggregator(store)))
  val broker = system.actorOf(Props(new UpdateBroker(staticAggregator)))
  val ts2012_1_1_0_0_0_0 = new DateTime(2012,1,1,0,0,0,0)
  var ts2012_1_1_0_0_0_0_int = Utils.dateTimeToInt(ts2012_1_1_0_0_0_0)
  val ts2012_1_1_18_0_0_0 = new DateTime(2012,1,1,18,0,0,0)
  val ts2012_1_1_18_0_0_0_int = Utils.dateTimeToInt(ts2012_1_1_18_0_0_0)
  val ts2012_1_1_18_0_3_0 = new DateTime(2012,1,1,18,0,3,0)
  val ts2012_1_1_18_0_3_0_int = Utils.dateTimeToInt(ts2012_1_1_18_0_3_0)

  test("Static Aggregated Statistics Calculation Actor; First inserted received"){
    Cache.stat_time_idx.call((x)=>x.flushDB())
    Cache.stat_time_idx.call{(x)=>
      x.select(Cache.REDIS_STORE)
      x.flushDB()
      x.select(Cache.STREAM_STAT_TIME_IDX)
    }
    store.drop("s1")
    var ts1Int: Int = (ts2012_1_1_0_0_0_0.getMillis / 1000L).asInstanceOf[Int]
    val sid="s1"
    val lvl0 = RawData(sid,Map(ts1Int->Some(5)))
    val lvl1=Insert(OneMinuteLevel.id,sid,ts2012_1_1_0_0_0_0,5,None)
    val lvl2=Insert(FiveMinuteLevel.id,sid,ts2012_1_1_0_0_0_0,5,Some(StatResult(sid,ts2012_1_1_0_0_0_0,OneMinuteLevel.id,List[Double](ts1Int,ts1Int,5,5,5,5,1.0,5,5*5))))
    val lvl3=Insert(FifteenMinuteLevel.id,sid,ts2012_1_1_0_0_0_0,5,Some(StatResult(sid,ts2012_1_1_0_0_0_0,FiveMinuteLevel.id,List[Double](ts1Int,ts1Int,5,5,5,5,1.0,5,5*5))))
    val lvl4=Insert(OneHourLevel.id,sid,ts2012_1_1_0_0_0_0,5,Some(StatResult(sid,ts2012_1_1_0_0_0_0,FifteenMinuteLevel.id,List[Double](ts1Int,ts1Int,5,5,5,5,1.0,5,5*5))))
    val lvl5=Insert(ThreeHourLevel.id,sid,ts2012_1_1_0_0_0_0,5,Some(StatResult(sid,ts2012_1_1_0_0_0_0,OneHourLevel.id,List[Double](ts1Int,ts1Int,5,5,5,5,1.0,5,5*5))))
    val lvl6=Insert(SixHourLevel.id,sid,ts2012_1_1_0_0_0_0,5,Some(StatResult(sid,ts2012_1_1_0_0_0_0,ThreeHourLevel.id,List[Double](ts1Int,ts1Int,5,5,5,5,1.0,5,5*5))))
    val lvl7=Insert(OneDayLevel.id,sid,ts2012_1_1_0_0_0_0,5,Some(StatResult(sid,ts2012_1_1_0_0_0_0,SixHourLevel.id,List[Double](ts1Int,ts1Int,5,5,5,5,1.0,5,5*5))))
    val lvl8=Insert(OneMonthLevel.id,sid,ts2012_1_1_0_0_0_0,5,Some(StatResult(sid,ts2012_1_1_0_0_0_0,OneDayLevel.id,List[Double](ts1Int,ts1Int,5,5,5,5,1.0,5,5*5))))
    val lvl9=Insert(OneYearLevel.id,sid,ts2012_1_1_0_0_0_0,5,Some(StatResult(sid,ts2012_1_1_0_0_0_0,OneMonthLevel.id,List[Double](ts1Int,ts1Int,5,5,5,5,1.0,5,5*5))))
    val lvl10=Done(sid,ts2012_1_1_0_0_0_0)
    staticAggregator ! lvl0
    expectMsg(lvl1)
    staticAggregator ! lvl1
    expectMsg(lvl2)
    staticAggregator ! lvl2
    expectMsg(lvl3)
    staticAggregator ! lvl3
    expectMsg(lvl4)
    staticAggregator ! lvl4
    expectMsg(lvl5)
    staticAggregator ! lvl5
    expectMsg(lvl6)
    staticAggregator ! lvl6
    expectMsg(lvl7)
    staticAggregator ! lvl7
    expectMsg(lvl8)
    staticAggregator ! lvl8
    expectMsg(lvl9)
    staticAggregator ! lvl9
    expectMsg(lvl10)
  }
  test("Static Aggregated Statistics Calculation Actor; A Second insert received"){
    var ts1Int = Utils.dateTimeToInt(ts2012_1_1_18_0_0_0)
    val sid="s1"
    val lvl0 = RawData(sid,Map(ts1Int->Some(1)))
    val lvl1=Insert(OneMinuteLevel.id,sid,ts2012_1_1_18_0_0_0,1,None)
    val lvl2=Insert(FiveMinuteLevel.id,sid,ts2012_1_1_18_0_0_0,1,Some(StatResult(sid,ts2012_1_1_18_0_0_0,OneMinuteLevel.id,List[Double](ts1Int,ts1Int,1,1,1,1,1.0,1,1*1))))
    val lvl3=Insert(FifteenMinuteLevel.id,sid,ts2012_1_1_18_0_0_0,1,Some(StatResult(sid,ts2012_1_1_18_0_0_0,FiveMinuteLevel.id,List[Double](ts1Int,ts1Int,1,1,1,1,1.0,1,1*1))))
    val lvl4=Insert(OneHourLevel.id,sid,ts2012_1_1_18_0_0_0,1,Some(StatResult(sid,ts2012_1_1_18_0_0_0,FifteenMinuteLevel.id,List[Double](ts1Int,ts1Int,1,1,1,1,1.0,1,1*1))))
    val lvl5=Insert(ThreeHourLevel.id,sid,ts2012_1_1_18_0_0_0,1,Some(StatResult(sid,ts2012_1_1_18_0_0_0,OneHourLevel.id,List[Double](ts1Int,ts1Int,1,1,1,1,1.0,1,1*1))))
    val lvl6=Insert(SixHourLevel.id,sid,ts2012_1_1_18_0_0_0,1,Some(StatResult(sid,ts2012_1_1_18_0_0_0,ThreeHourLevel.id,List[Double](ts1Int,ts1Int,1,1,1,1,1.0,1,1*1))))
    val lvl7=Insert(OneDayLevel.id,sid,ts2012_1_1_18_0_0_0,1,Some(StatResult(sid,ts2012_1_1_18_0_0_0,SixHourLevel.id,List[Double](ts1Int,ts1Int,1,1,1,1,1.0,1,1*1))))
    val lvl8=Insert(OneMonthLevel.id,sid,ts2012_1_1_18_0_0_0,1,Some(StatResult(sid,ts2012_1_1_18_0_0_0,OneDayLevel.id,List[Double](ts2012_1_1_0_0_0_0_int,ts1Int,5,1,1,5,2.0,6,5*5+1*1))))
    val lvl9=Insert(OneYearLevel.id,sid,ts2012_1_1_18_0_0_0,1,Some(StatResult(sid,ts2012_1_1_18_0_0_0,OneMonthLevel.id,List[Double](ts2012_1_1_0_0_0_0_int,ts1Int,5,1,1,5,2.0,6,5*5+1*1))))
    val lvl10=Done(sid,ts2012_1_1_18_0_0_0)
    staticAggregator ! lvl0
    expectMsg(lvl1)
    staticAggregator ! lvl1
    expectMsg(lvl2)
    staticAggregator ! lvl2
    expectMsg(lvl3)
    staticAggregator ! lvl3
    expectMsg(lvl4)
    staticAggregator ! lvl4
    expectMsg(lvl5)
    staticAggregator ! lvl5
    expectMsg(lvl6)
    staticAggregator ! lvl6
    expectMsg(lvl7)
    staticAggregator ! lvl7
    expectMsg(lvl8)
    staticAggregator ! lvl8
    expectMsg(lvl9)
    staticAggregator ! lvl9
    expectMsg(lvl10)
  }
  test("Static Aggregated Statistics Calculation Actor; An update over non-existing Item received"){
    val sid="s1"
    val lvl0 = RawData(sid,Map(Utils.dateTimeToInt(ts2012_1_1_18_0_3_0)->None))
    val lvl10=Done(sid,ts2012_1_1_18_0_3_0)
    staticAggregator ! lvl0
    expectMsg(lvl10)
  }
  test("Static Aggregated Statistics Calculation Actor; An update over first item received"){
    val sid="s1"
    val lvl0 = RawData(sid,Map(ts2012_1_1_0_0_0_0_int->Some(-2.0)))
    val lvl1=Update(OneMinuteLevel.id,sid,ts2012_1_1_0_0_0_0,None)
    val lvl2=Update(FiveMinuteLevel.id,sid,ts2012_1_1_0_0_0_0,Some(StatResult(sid,ts2012_1_1_0_0_0_0,OneMinuteLevel.id,List[Double](ts2012_1_1_0_0_0_0_int,ts2012_1_1_0_0_0_0_int,-2,-2,-2,-2,1.0,-2,4))))
    val lvl3=Update(FifteenMinuteLevel.id,sid,ts2012_1_1_0_0_0_0,Some(StatResult(sid,ts2012_1_1_0_0_0_0,FiveMinuteLevel.id,List[Double](ts2012_1_1_0_0_0_0_int,ts2012_1_1_0_0_0_0_int,-2,-2,-2,-2,1.0,-2,4))))
    val lvl4=Update(OneHourLevel.id,sid,ts2012_1_1_0_0_0_0,Some(StatResult(sid,ts2012_1_1_0_0_0_0,FifteenMinuteLevel.id,List[Double](ts2012_1_1_0_0_0_0_int,ts2012_1_1_0_0_0_0_int,-2,-2,-2,-2,1.0,-2,4))))
    val lvl5=Update(ThreeHourLevel.id,sid,ts2012_1_1_0_0_0_0,Some(StatResult(sid,ts2012_1_1_0_0_0_0,OneHourLevel.id,List[Double](ts2012_1_1_0_0_0_0_int,ts2012_1_1_0_0_0_0_int,-2,-2,-2,-2,1.0,-2,4))))
    val lvl6=Update(SixHourLevel.id,sid,ts2012_1_1_0_0_0_0,Some(StatResult(sid,ts2012_1_1_0_0_0_0,ThreeHourLevel.id,List[Double](ts2012_1_1_0_0_0_0_int,ts2012_1_1_0_0_0_0_int,-2,-2,-2,-2,1.0,-2,4))))
    val lvl7=Update(OneDayLevel.id,sid,ts2012_1_1_0_0_0_0,Some(StatResult(sid,ts2012_1_1_0_0_0_0,SixHourLevel.id,List[Double](ts2012_1_1_0_0_0_0_int,ts2012_1_1_0_0_0_0_int,-2,-2,-2,-2,1.0,-2,4))))
    val lvl8=Update(OneMonthLevel.id,sid,ts2012_1_1_0_0_0_0,Some(StatResult(sid,ts2012_1_1_0_0_0_0,OneDayLevel.id,List[Double](ts2012_1_1_0_0_0_0_int,ts2012_1_1_18_0_0_0_int,-2,1,-2,1,2.0,-1,4+1))))
    val lvl9=Update(OneYearLevel.id,sid,ts2012_1_1_0_0_0_0,Some(StatResult(sid,ts2012_1_1_0_0_0_0,OneMonthLevel.id,List[Double](ts2012_1_1_0_0_0_0_int,ts2012_1_1_18_0_0_0_int,-2,1,-2,1,2.0,-1,4+1))))
    val lvl10=Done(sid,ts2012_1_1_0_0_0_0)
    staticAggregator ! lvl0
    expectMsg(lvl1)
    staticAggregator ! lvl1
    expectMsg(lvl2)
    staticAggregator ! lvl2
    expectMsg(lvl3)
    staticAggregator ! lvl3
    expectMsg(lvl4)
    staticAggregator ! lvl4
    expectMsg(lvl5)
    staticAggregator ! lvl5
    expectMsg(lvl6)
    staticAggregator ! lvl6
    expectMsg(lvl7)
    staticAggregator ! lvl7
    expectMsg(lvl8)
    staticAggregator ! lvl8
    expectMsg(lvl9)
    staticAggregator ! lvl9
    expectMsg(lvl10)
  }
  ////  test("Bulk aggregation, all inserts"){
  ////    store.drop("sbulk")
  ////    val ts1 = new DateTime(2012,1,1,0,0,0,0)
  ////    val rawData = (for(i<-0 until 100) yield (ts1.getMillis/1000L + i).asInstanceOf[Int] -> Some(i.toDouble)).toMap
  ////    val expectedDone = for(i<-0 until 100) yield Done("sbulk",ts1.plusSeconds(i))
  ////    broker ! RawData("sbulk",rawData)
  ////    expectMsgAllOf(expectedDone :_*) // this test doesn't work, as reference to Sender is lost in the broker during the process hence it cant reply back
  ////  }
}
