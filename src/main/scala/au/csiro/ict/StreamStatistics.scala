package au.csiro.ict

import au.csiro.ict.Cache._
import com.codahale.jerkson.Json._
import org.apache.commons.math3.stat.descriptive.SummaryStatistics

object InsertionType extends Enumeration{
  type Action = Value
  val INSERT,UPDATE,DELETE,NOP = Value
  // Nop example, deleting a non existing element
}

class StatChunker(streamDayKey:String) extends ChunkFormatter{
  def done() = {
    if(summaryStat.getN>0){
      stat.set(streamDayKey,generate(List(summaryStat.getMax,summaryStat.getMin,summaryStat.getN,summaryStat.getSum,summaryStat.getSumsq)))
  }else
      stat.del(streamDayKey)
  }
  val summaryStat = new SummaryStatistics()
  def insert(sensor:String, newYearDay:String,secInDay:Int,value:String):Boolean ={
    if (!value.equals("null"))
      summaryStat.addValue(value.toDouble)
    true
  }
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
  def getBitVectorFor(streamDayKey:String):Set[Int]=stat_time_idx.get(streamDayKey).map(parse[Set[Int]]).getOrElse(Set[Int]())

  def getStatFor(streamDayKey:String):Option[List[Double]]=stat.get(streamDayKey).map(parse[List[Double]])

//  def getStatFor(streamDayKeys:String*):Map[String,List[Double]]=streamDayKeys.zip(stat.mget(streamDayKeys).get.flatMap(_.map(parse[List[Double]]))).toMap

  def updateInterDayStatistics(nid:String,sensorDayKey:String){
    store.queryNode(nid,List(sensorDayKey).iterator,None,new StatChunker(sensorDayKey))
  }

  def updateIntraDayStatistics(sensorId:String, timeStamp:Int,value:Option[Double],invalidator:(String=>Unit)):Unit=
    updateStatisticsInstructions(sensorId,timeStamp,value) match {
      case Some((streamDayKey,bitVector,Nil))=> //for updates and removes
        stat_time_idx.set(streamDayKey,generate(bitVector))
        invalidator(streamDayKey)

      case Some((streamDayKey,bitVector,statInfo))=> // for insertions
        stat_time_idx.set(streamDayKey,generate(bitVector))
        stat.set(streamDayKey,generate(statInfo))
      case None => //Nop
    }

  def calculateStandardDev(n:Double,sum:Double,sum2:Double)=java.lang.Math.sqrt((n*sum2 - sum*sum) / (n*(n-1)))

  def calculateStatTable(v:Double,max:Double,min:Double,count:Double,sum:Double,sum2:Double):List[Double]=
    List(if (v > max) v else max,if (v < min) v else min,count + 1,sum+v,sum2+v*v)

  def updateStatisticsInstructions(sensorId:String, timeStamp:Int,value:Option[Double]):Option[(String,Set[Int],List[Double])]={
    val streamDayKey = Utils.generateRowKey(sensorId,timeStamp)
    val secIdx = Utils.getSecondOfDay(timeStamp)
    val bv=getBitVectorFor(streamDayKey)
    val List(max,min,count,sum,m2) = getStatFor(streamDayKey).getOrElse(List(Double.MinValue,Double.MaxValue,0,0.0,0.0)) //Max,Min,Count,Sum,SumSq

    findAction(bv)(secIdx,value) match {
      case InsertionType.INSERT=> Some((streamDayKey,bv +secIdx,calculateStatTable(value.get,max,min,count,sum,m2)))
      case e@InsertionType.UPDATE => Some((streamDayKey,bv,Nil))
      case e@InsertionType.DELETE => Some((streamDayKey,bv-secIdx,Nil))
      case InsertionType.NOP=> None
    }
  }


}




