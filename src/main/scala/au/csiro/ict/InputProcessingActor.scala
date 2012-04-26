package au.csiro.ict

import akka.actor._
import akka.routing.RoundRobinRouter
import akka.util.Duration
import akka.util.duration._
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory
import com.codahale.jerkson.Json._
import au.csiro.ict.Cache.{store}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

trait SDBMsg
case class Done(queueName:String) extends SDBMsg
case class Task(queueName:String) extends SDBMsg

class InputProcessingWorker extends Actor{

  def receive = {
    case Task(queueName)=>
      val Array(_,nid,sid)=queueName.split("@")
      Cache.queue.call{queue=>
        while(queue.llen(queueName)>0){
          val msg=queue.lrange(queueName,0,0).head
          val data = parse[Map[Int,Option[String]]](msg)
          store.addNodeData(nid,Map(sid->data))
          data.foreach(item => StreamStatistics.updateIntraDayStatistics(sid,item._1,item._2.map(_.toDouble), (streamDayKey:String)=>
            Cache.stat.sadd(Cache.InterdayStatIncomingQueueName,Utils.generateNidStreamDayKey(nid,streamDayKey))))
          queue.lpop(queueName)
        }
      }
      sender ! Done(queueName)
  }
}
// todo: making sure there is only one instanceof this actor exists
class InputProcessingMaster(val workers:ActorRef) extends Actor with Logger {

  var workerSet = Set[String]()

  def receive = {
    case msg@Task(queueName)=>
      if (!workerSet.contains(queueName)){
        workers ! msg
        workerSet+=queueName
      }else {
        logger.debug("A new message received for already active worker on queue: "+queueName)
      }
      if (logger.isDebugEnabled){
        logger.debug("Current contents of queue:"+workerSet.mkString("[",",","]"))
        logger.info("Current length of queue:"+workerSet.size)
      }
    case Done(queueName)=>
      println("task done on --------------->"+queueName)
      workerSet-=queueName
  }
}

class InputProcessingSystemProxy extends Bootable{

  val system = ActorSystem("InputProcessingWorkersProxy", ConfigFactory.load.getConfig("InputProcessingWorkersProxy"))

  val worker = system.actorOf(Props[InputProcessingWorker], "InputProcessingWorker")

  val master = system.actorOf(Props(new InputProcessingMaster(worker)))

  def startup() {
  }

  def process(msg:SDBMsg) = master ! msg

  def shutdown() {
    system.shutdown()
  }

}
class InputProcessingSystem extends Bootable{

  val system = ActorSystem("InputProcessingSystem", ConfigFactory.load.getConfig("InputProcessingWorkers"))

  val worker = system.actorOf(Props[InputProcessingWorker], "InputProcessingWorker")

  def process(msg:SDBMsg) = worker ! msg

  def startup() {
  }

  def shutdown() {
    system.shutdown()
  }
}

object InputProcessingBackend{
  lazy val ips = new InputProcessingSystem()
  def main(args:Array[String]){
    ips.startup()
    println("input processing backend started, waiting for messages ... ")
  }
}
