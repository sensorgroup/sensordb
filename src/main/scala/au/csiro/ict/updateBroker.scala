package au.csiro.ict

import akka.actor._
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime

case class StatResult(streamId:String,ts:DateTime,aggLeve:String,stats:List[Double])
trait SDBMsg
case class Done(sid:String,ts:DateTime) extends SDBMsg
case class RawData(sid:String,value:Map[Int,Option[Double]]) extends SDBMsg
case class Insert(aggLevel:String,sid:String,ts:DateTime,value:Double,sr:Option[StatResult]) extends SDBMsg
case class Update(nextAggLevel:String,sid:String,ts:DateTime,sr:Option[StatResult]) extends SDBMsg

class UpdateBroker(val staticAggregator:ActorRef) extends Actor with Logger {
  def receive = {
    case msg@RawData(sid,value) =>
      staticAggregator ! msg

    case msg@Insert(aggLevel,sid,ts,value,previuosCalcResult) => staticAggregator !msg
    case msg@Update(aggLevel,sid,ts,previuosCalcResult) => staticAggregator !msg
    case msg@Done(sid,ts)=>  /** use this to keep track of active workers. **/
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


