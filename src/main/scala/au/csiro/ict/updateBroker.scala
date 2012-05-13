package au.csiro.ict

import akka.actor._
import akka.routing.RoundRobinRouter
import akka.util.Duration
import akka.util.duration._
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory
import com.codahale.jerkson.Json._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import java.util.Date
import org.apache.hadoop.hbase.util.Bytes
import org.apache.commons.math.stat.descriptive.SummaryStatistics
import org.joda.time.{DateTimeZone, DateTime}

case class StatResult(streamId:String,ts:DateTime,aggLeve:AggLevel.AggLevel,stats:List[Double])
trait SDBMsg
case class Done(sid:String,ts:DateTime) extends SDBMsg
case class RawData(sid:String,value:Map[Int,Option[Double]],tz:DateTimeZone) extends SDBMsg
case class Insert(aggLevel:AggLevel.AggLevel,sid:String,ts:DateTime,value:Double,sr:Option[StatResult]) extends SDBMsg
case class Update(nextAggLevel:AggLevel.AggLevel,sid:String,ts:DateTime,sr:Option[StatResult]) extends SDBMsg

object ColumnOrientedDBKeyHelper{
  import AggLevel._

//  def rowKey(sensor:String,ts:Int,aggLevel:AggLevel.AggLevel=RAW):String= rowKey(sensor,new DateTime(ts*1000L),aggLevel)

  def rowKey(sensor:String,ts:DateTime,aggLevel:AggLevel.AggLevel):String= aggLevel match {
    case RAW=>      sensor +"_"+Utils.yyyyDDDFormat.print(ts)
    case OneMin=>   sensor +"_"+ "1m"+Utils.yyyyDDDFormat.print(ts)
    case FiveMin=>  sensor +"_"+ "5m"+Utils.yyyyDDDFormat.print(ts)
    case FifteenMin=> sensor +"_"+ "15m"+Utils.yyyyDDDFormat.print(ts)
    case OneHour=>    sensor +"_"+ "1h"+Utils.yyyyDDDFormat.print(ts)
    case ThreeHour=>  sensor +"_"+ "3hly"+Utils.yyyyFormat.print(ts)
    case SixHour=>  sensor +"_"+ "6hly"+Utils.yyyyFormat.print(ts)
    case OneDay=>   sensor +"_"+ "daily"+Utils.yyyyFormat.print(ts)
    //    case OneWeek=>  sensor +"_"+ "week"+Utils.yyyyFormat.print(ts)
    case OneMonth=> sensor +"_"+ "mon"+Utils.yyyyFormat.print(ts)
    case OneYear=>  sensor +"_"+ "yearly"
    case others => throw new Exception("Unrecognized agg level:"+aggLevel.toString)
  }
//  def cellKeyOf(ts:Int,aggLevel:AggLevel.AggLevel):Array[Byte]= cellKeyOf(new DateTime(ts*1000L),aggLevel)

  def cellKeyOf(ts:DateTime,aggLevel:AggLevel.AggLevel):Array[Byte]= aggLevel match {
    case RAW=>      Bytes.toBytes(ts.getSecondOfDay)
    case OneMin=>   Bytes.toBytes(ts.getMinuteOfDay)
    case FiveMin=>  Bytes.toBytes(ts.getMinuteOfDay/5)
    case FifteenMin=> Bytes.toBytes(ts.getMinuteOfDay/15)
    case OneHour=>  Bytes.toBytes(ts.getHourOfDay)
    case ThreeHour=>  Bytes.toBytes(ts.getDayOfYear +"-"+ ts.getHourOfDay/3)
    case SixHour=>  Bytes.toBytes(ts.getDayOfYear +"-"+ ts.getHourOfDay/6)
    case OneDay=>   Bytes.toBytes(ts.getDayOfYear)
    //    case OneWeek=>  Bytes.toBytes(ts.getWeekOfWeekyear)
    case OneMonth=> Bytes.toBytes(ts.getMonthOfYear)
    case OneYear=>  Bytes.toBytes(ts.getYear)
  }

//  def rowOf(sensor:String,ts:Int,aggLevel:AggLevel.AggLevel=RAW):(String,Array[Byte]) = rowOf(sensor,new DateTime(ts*1000L),aggLevel)

  def rowOf(sensor:String,ts:DateTime,aggLevel:AggLevel.AggLevel):(String,Array[Byte]) = rowKey(sensor,ts,aggLevel)->cellKeyOf(ts,aggLevel)

//  def childCellsOf(sensor:String,ts:Int,aggLevel:AggLevel.AggLevel):Seq[Array[Byte]] = childCellsOf(sensor,new DateTime(ts*1000L),aggLevel)

  def childCellsOf(sensor:String,ts:DateTime,aggLevel:AggLevel.AggLevel):Seq[Array[Byte]] = aggLevel match {
    case OneMin=>   (ts.getMinuteOfDay*60 until (ts.getMinuteOfDay+1)*60).map(Bytes.toBytes)
    case FiveMin=>  ((ts.getMinuteOfDay-ts.getMinuteOfDay%5) until (ts.getMinuteOfDay-ts.getMinuteOfDay%5+5)).map(Bytes.toBytes)
    case FifteenMin=> ((ts.getMinuteOfDay-ts.getMinuteOfDay%15 until (ts.getMinuteOfDay-ts.getMinuteOfDay%15+15) by 5)).map((x)=>Bytes.toBytes(x/5))
    case OneHour=>  (ts.getHourOfDay*60 until (ts.getHourOfDay+1)*60 by 15).map((x)=>Bytes.toBytes(x/15))
    case ThreeHour=>  ((ts.getHourOfDay-ts.getHourOfDay%3) until (ts.getHourOfDay-ts.getHourOfDay%3+3)).map(x=>Bytes.toBytes(x))
    case SixHour=>  ((ts.getHourOfDay-ts.getHourOfDay%6) until (ts.getHourOfDay-ts.getHourOfDay%6+6) by 3).map(x=>Bytes.toBytes(ts.getDayOfYear+"-"+x/3))
    case OneDay=>   List(ts.getDayOfYear+"-0",ts.getDayOfYear+"-1",ts.getDayOfYear+"-2",ts.getDayOfYear+"-3").map(Bytes.toBytes)
    //    case OneWeek=>  ts.getWeekOfWeekyear
    case OneMonth=> (ts.withDayOfMonth(1).getDayOfYear until ts.plusMonths(1).withDayOfMonth(1).getDayOfYear).map(Bytes.toBytes)
    case OneYear=> (0 until 12).map(Bytes.toBytes)
  }
}

/**
 * Todo: UpdateBroker should use persistent queue, anything form of persistent would do.
 */

class UpdateBroker(val workers:ActorRef) extends Actor with Logger {
  def receive = {
    case msg@RawData(sid,value,tz) => workers ! msg
    case msg@Insert(aggLevel,sid,ts,value,previuosCalcResult) => workers !msg
    case msg@Update(aggLevel,sid,ts,previuosCalcResult) => workers !msg
    case Done(sid,ts)=> /** TODO: use this to keep track of active workers. **/
  }
}

class UpdateBrokerProxy(s:Storage) extends Bootable{

  val system = ActorSystem("InputProcessingWorkersProxy", ConfigFactory.load.getConfig("InputProcessingWorkersProxy"))

  val worker = system.actorOf(Props(new StaticAggregator(s)), "InputProcessingWorker")

  val master = system.actorOf(Props(new UpdateBroker(worker)))

  def startup() {
  }

  def process(msg:SDBMsg) = master ! msg

  def shutdown() {
    system.shutdown()
  }

}

object UpdateBrokerBackend{
  //  lazy val ips = new InputProcessingSystem()
  //  def main(args:Array[String]){
  //    ips.startup()
  //    println("input processing backend started, waiting for messages ... ")
  //  }
}
