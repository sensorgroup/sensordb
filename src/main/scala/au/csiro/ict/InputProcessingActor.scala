package au.csiro.ict

import akka.actor._
import akka.routing.RoundRobinRouter
import akka.util.Duration
import akka.util.duration._
import akka.kernel.Bootable
import com.typesafe.config.ConfigFactory

trait SDBMsg
case class Done(queueName:String) extends SDBMsg
case class Task(queueName:String) extends SDBMsg

class InputProcessingWorker extends Actor{

  def receive = {
    case Task(queueName)=>
      println("I am a worker and now I am processing:"+queueName)
      sender ! Done(queueName)
  }
}

class InputProcessingMaster(val remoteWorker:ActorRef) extends Actor {

  var workers = Set[String]()

  def receive = {
    case msg@Task(queueName)=>
      if (!workers.contains(queueName)){
        println("create a worker and delegate the task")
        remoteWorker ! msg
        workers+=queueName
      }
    case Done(queueName)=>
      println("task done on "+queueName)
      workers-=queueName
  }
}

class InputProcessingSystemProxy extends Bootable{

  val system = ActorSystem("InputProcessingWorkersProxy", ConfigFactory.load.getConfig("InputProcessingWorkersProxy"))

  val remoteWorker = system.actorOf(Props[InputProcessingWorker], "InputProcessingWorker")

  val master = system.actorOf(Props(new InputProcessingMaster(remoteWorker)))

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
