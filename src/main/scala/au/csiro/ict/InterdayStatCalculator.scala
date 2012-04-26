package au.csiro.ict

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import Cache._

object InterdayStatCalculator extends Logger{
  val PERIOD=200
  /**
   *
   * @return number of tasks and total milliseconds took to completely process this queue
   */
  def process() = {
    val start = System.currentTimeMillis()
    var count = 0
    stat.srandmember(InterdayStatIncomingQueueName).map{key=>
      val Array(nid,sidKey) = key.split("@")
      stat.smove(InterdayStatIncomingQueueName,InterdayStatProcessingQueueName,key)
      StreamStatistics.updateInterDayStatistics(nid,sidKey)
      stat.srem(InterdayStatProcessingQueueName,key)
      count+=1
    }
    if (count ==0)
      None
    else
      Some(count,(System.currentTimeMillis()-start))
  }

  def main(args:Array[String]){
    while(true)
      process.orElse{
        println("No task, next check within "+PERIOD+" msec")
        Thread.sleep(PERIOD)
        None
      }
  }
}
