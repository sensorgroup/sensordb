package au.csiro.ict

import akka.actor.Actor
import org.apache.hadoop.hbase.util.Bytes
import com.codahale.jerkson.Json._
import org.apache.commons.math.stat.descriptive.SummaryStatistics
import org.joda.time.DateTime

object AggLevel extends Enumeration{
  type AggLevel = Value

  val RAW,OneMin,FiveMin,FifteenMin,OneHour,ThreeHour,SixHour,OneDay,OneMonth,OneYear = Value
}

object StatAggHelpers{
  val INITIAL_STAT = List(Double.MaxValue,Double.MinValue,0.0,0.0,0.0)
  def updateStats(v:Double,min:Double,max:Double,count:Double,sum:Double,sum2:Double):List[Double]=
    List(if (v < min) v else min,if (v > max) v else max,count + 1,sum+v,sum2+v*v)
  def mergeStats(s1:List[Double],s2:List[Double]):List[Double]={
    val List(min1,max1,count1,sum1,sumSq1) = s1
    val List(min2,max2,count2,sum2,sumSq2) = s2
    List(if (min1 < min2) min1 else min2,if (max1 > max2) max1 else max2,count1+count2,sum1+sum2,sumSq1+sumSq2)
  }
}
class StaticAggregator(val store:Storage) extends Actor{
  import AggLevel._
  import ColumnOrientedDBKeyHelper._
  import StatAggHelpers._

  def receive = {
    case Insert(aggLevel,sid,ts,value,_)=>
      val row = rowOf(sid,ts,aggLevel)
      val List(min,max,count,sum,sumSq) = store.get(Bytes.toBytes(row._1),row._2).map((x:Array[Byte])=>parse[List[Double]](Bytes.toString(x))).getOrElse(INITIAL_STAT)
      var stats: List[Double] = updateStats(value, min, max, count, sum, sumSq)
      store.put(Bytes.toBytes(row._1),row._2,Bytes.toBytes(generate(stats)))
      if (aggLevel.id+1 < AggLevel.maxId)
        sender ! Insert(AggLevel(aggLevel.id+1),sid,ts,value,Some(StatResult(sid,ts,aggLevel,stats)))
      else
        sender ! Done(sid,ts)

    case Update(aggLevel,sid,ts,_)=>
      // TODO: Update should use coprocessors in HBase
      val previousRow = Bytes.toBytes(rowKey(sid,ts,AggLevel(aggLevel.id-1)))
      val childCells:Seq[Array[Byte]] = childCellsOf(sid,ts,aggLevel)
      val stats = aggLevel match {
        case OneMin=>
          val stats = new SummaryStatistics()
          store.get(previousRow,childCells).filter(_._2 !=null).foreach{x=>
            stats.addValue(Bytes.toDouble(x._2))
          }
          if (stats.getN==0)
            StatAggHelpers.INITIAL_STAT
          else
            List[Double](stats.getMin,stats.getMax,stats.getN,stats.getSum,stats.getSumsq)
        case otherLvls=>
          store.get(previousRow,childCells).filter(_._2 !=null).map(x=> parse[List[Double]](Bytes.toString(x._2))).foldLeft(INITIAL_STAT)((sum,item)=>mergeStats(sum,item))
      }
      store.put(Bytes.toBytes(rowKey(sid,ts,aggLevel)),cellKeyOf(ts,aggLevel),Bytes.toBytes(generate(stats)))

      if (aggLevel.id+1 < AggLevel.maxId)
        sender ! Update(AggLevel(aggLevel.id+1),sid,ts,Some(StatResult(sid,ts,aggLevel,stats)))
      else
        sender ! Done(sid,ts)

    case RawData(sid,data,tz)=>
      store.put(sid,data,tz)
      Cache.stat_time_idx.call{statIdx=>
        data.foreach{item =>
          val ts =new DateTime(item._1*1000L,tz)
          val rawKey = rowOf(sid,ts,RAW)
          item._2 match{
            case Some(value)=>
              statIdx.sadd(Bytes.toBytes(rawKey._1),rawKey._2).toInt match {
                case 0 => sender ! Update(OneMin,sid,ts,None)
                case 1 => sender ! Insert(OneMin,sid,ts,value,None)
              }
            case None => statIdx.srem(Bytes.toBytes(rawKey._1),rawKey._2).toInt match {
              case 1 => sender ! Update(OneMin,sid,ts,None)
              case removedNonExisting => sender ! Done(sid,ts)
            }
          }
        }
      }
  }
}
