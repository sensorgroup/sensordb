package au.csiro.ict

import au.csiro.ict.Cache._
import com.codahale.jerkson.Json._

object InsertionType extends Enumeration{
  type Action = Value
  val INSERT,UPDATE,DELETE,NOP = Value
  // Nop example, deleting a non existing element
}

object StreamStatistics {
  def findAction(set:Set[Int])(secIdx:Int,value:Option[Double]):InsertionType.Action={
    import InsertionType._
    set.contains(secIdx) match{
      case true => value match {
        case Some(v)=> UPDATE
        case None => DELETE
      }
      case false if value.isDefined => INSERT
      case false if value.isEmpty => NOP
    }
  }
  def getBitVectorFor(key:String):Option[Set[Int]]=stat_time_idx.get(key).map(parse[Set[Int]])

  def getStatFor(key:String):Option[List[Double]]=stat.get(key).map(parse[List[Double]])

  def getStatFor(keys:String*):Map[String,List[Double]]=keys.zip(stat.mget(keys).get.flatMap(_.map(parse[List[Double]]))).toMap

  def updateStatistics(sensorId:String, timeStamp:Int,value:Option[Double],invalidator:(String=>Unit)):Unit=
    updateStatisticsInstructions(sensorId,timeStamp,value,invalidator) match {
      case Some((streamDayKey,bitVector,Nil))=> //for updates and removes
        stat_time_idx.set(streamDayKey,generate(bitVector))
      case Some((streamDayKey,bitVector,statInfo))=> // for insertions
        stat_time_idx.set(streamDayKey,generate(bitVector))
        stat.set(streamDayKey,generate(statInfo))
      case None => //Nop
    }

  def calculateStandardDev(n:Double,sum:Double,sum2:Double)=java.lang.Math.sqrt((n*sum2 - sum*sum) / (n*(n-1)))

  def calculateStatTable(v:Double,max:Double,min:Double,count:Double,sum:Double,sum2:Double):List[Double]=
    List(if (v > max) v else max,if (v < min) v else min,count + 1,sum+v,sum2+v*v)

  def updateStatisticsInstructions(sensorId:String, timeStamp:Int,value:Option[Double],invalidator:(String=>Unit)):Option[(String,Set[Int],List[Double])]={
    val streamDayKey = Utils.generateRowKey(sensorId,timeStamp)
    val secIdx = Utils.getSecondOfDay(timeStamp)
    var bv=getBitVectorFor(streamDayKey).getOrElse(Set[Int]())
    var List(max,min,count,sum,m2) = getStatFor(streamDayKey).getOrElse(List(Double.MinValue,Double.MaxValue,0,0.0,0.0)) //Max,Min,Count,Sum,m2

    findAction(bv)(secIdx,value) match {
      case InsertionType.INSERT=>
        bv +=secIdx
        Some((streamDayKey,bv,calculateStatTable(value.get,max,min,count,sum,m2)))
      case e@InsertionType.UPDATE =>
        invalidator(streamDayKey)
        Some((streamDayKey,bv,Nil))
      case e@InsertionType.DELETE =>
        bv -=secIdx
        invalidator(streamDayKey)
        Some((streamDayKey,bv,Nil))
      case InsertionType.NOP=>
         None
    }
  }


}




