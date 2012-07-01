package au.csiro.ict

import akka.actor.Actor
import org.apache.hadoop.hbase.util.Bytes
import com.codahale.jerkson.Json._
import org.apache.commons.math.stat.descriptive.SummaryStatistics
import org.joda.time._
import format.{DateTimeFormat, DateTimeFormatter}
import org.bson.types.ObjectId

object AggregationLevel {
  val Levels:Map[String,AggregationLevel] = List(RawLevel,OneMinuteLevel,FiveMinuteLevel,FifteenMinuteLevel,OneHourLevel,ThreeHourLevel,SixHourLevel,OneDayLevel,OneMonthLevel,OneYearLevel).map(x=>x.id->x).toMap
  val ShortLevels:Map[String,AggregationLevel]  = Levels.values.map(x=>x.shortId->x).toMap
  def apply(levelNameInString:String) = Levels.get(levelNameInString)
  private val rowToTsSplitRex = ("""([a-z0-9A-Z]+)"""+Levels.values.map(_.shortId).mkString("(","|",")")+"""(\d+)""").r
  def rowToTs(rowKey:String):(String,AggregationLevel,String)={
    rowKey match {
      case rowToTsSplitRex(sid,agg,ts)=>
        val lvl = ShortLevels(agg)
        (sid,lvl,ts)
    }
  }
}
abstract class AggregationLevel {
  val colKeyRange:Range
  def validColumnKey(s:Int):Boolean=colKeyRange.contains(s.toInt)
  def createPeriod(fromInclusive:DateTime,toInclusive:DateTime):Iterator[DateTime] = new Iterator[DateTime]{
    var temp = fromInclusive
    def hasNext = !temp.isAfter(toInclusive)

    def next() = {
      val toReturn = temp;
      temp = temp.plus(rowIncrement)
      toReturn
    }
  }
  def getChildCells(ts:DateTime):Range
  def getChildCellsAsBytes(ts:DateTime):Seq[Array[Byte]]=getChildCells(ts).map(Bytes.toBytes)
  def rowKey(sid:String,ts:DateTime):String = sid + shortId + dateTimePattern.print(ts)
  def rowKeyAsBytes(sid:String,ts:DateTime)=Bytes.toBytes(rowKey(sid,ts))
  def rowColKey(sid:String,ts:DateTime) = Bytes.toBytes(rowKey(sid,ts))->Bytes.toBytes(getCellKeyFor(ts))
  val id:String
  val shortId:String
  val dateTimePattern:DateTimeFormatter
  val rowIncrement:ReadablePeriod
  def nextRowTimeStamp(ts:DateTime):DateTime = ts.plus(rowIncrement)
  def getCourserLevel():Option[AggregationLevel]
  def getFinerLevel():Option[AggregationLevel]
  def getCellKeyFor(ts:DateTime):Int
  def getCellKeyForAsByte(ts:DateTime):Array[Byte] = Bytes.toBytes(getCellKeyFor(ts))
  def colIndexToTimestamp(ts:DateTime,tsInInt:Int,columnIdx:Int):Int

}


object OneYearLevel extends AggregationLevel{
  val colKeyRange = 1990 until 2020
  val id="1-year"
  val shortId="_y"
  val rowIncrement = new Period().withYears(1)
  val dateTimePattern = DateTimeFormat.forPattern("0") // zero because I needed something for dateTimePattern, 0 is just a dummy pattern as "" is not a valid pattern for Joda
  override def getCellKeyFor(ts:DateTime) = ts.getYear
  override def getChildCells(ts:DateTime) = 0 until 12
  def getCourserLevel() = None
  def getFinerLevel() = Some(OneMonthLevel)
  override def colIndexToTimestamp(ts:DateTime,tsInInt:Int,columnIdx:Int) = (ts.withYear(columnIdx).withMonthOfYear(1).withDayOfYear(1).withMillisOfDay(0).getMillis/1000L).asInstanceOf[Int]


}
object OneMonthLevel extends AggregationLevel{
  val colKeyRange = 0 until 12
  val id = "1-month"
  val shortId="_x"
  val dateTimePattern = Utils.yyyyFormat
  val rowIncrement = new Period().withYears(1)
  override def getCellKeyFor(ts:DateTime) = ts.getMonthOfYear
  override def getChildCells(ts:DateTime) = (ts.withDayOfMonth(1).getDayOfYear until ts.plusMonths(1).withDayOfMonth(1).getDayOfYear)
  def getCourserLevel() = Some(OneYearLevel)
  def getFinerLevel() = Some(OneDayLevel)
  override def colIndexToTimestamp(ts:DateTime,tsInInt:Int,columnIdx:Int) = (ts.withMonthOfYear(columnIdx).withDayOfMonth(1).withMillisOfDay(0).getMillis/1000L).asInstanceOf[Int]

}

object OneDayLevel extends AggregationLevel{
  val colKeyRange = 0 until 366 // leap years
  val id = "1-day"
  val shortId = "_d"
  val dateTimePattern = Utils.yyyyFormat
  val rowIncrement = new Period().withYears(1)
  override def getCellKeyFor(ts:DateTime) = ts.getDayOfYear
  override def getChildCells(ts:DateTime) = (ts.getDayOfYear-1)*24/6 until (ts.getDayOfYear*24)/6
  def getCourserLevel() = Some(OneMonthLevel)
  def getFinerLevel() = Some(SixHourLevel)
  override def colIndexToTimestamp(ts:DateTime,tsInInt:Int,columnIdx:Int) = (ts.withDayOfYear(columnIdx).withMillisOfDay(0).getMillis/1000L).asInstanceOf[Int]

}
object SixHourLevel extends AggregationLevel{
  val colKeyRange = 0 until 366*4
  val id = "6-hour"
  val shortId = "_s"
  val rowIncrement = new Period().withYears(1)
  val dateTimePattern = Utils.yyyyFormat
  override def getCellKeyFor(ts:DateTime) = ((ts.getDayOfYear-1)*24+ ts.getHourOfDay) / 6
  override def getChildCells(ts:DateTime) = ((ts.getDayOfYear-1)*24 + ts.getHourOfDay)/3 until ((ts.getDayOfYear-1)*24 + ts.getHourOfDay+6)/3
  def getCourserLevel() = Some(OneDayLevel)
  def getFinerLevel() = Some(ThreeHourLevel)
  override def colIndexToTimestamp(ts:DateTime,tsInInt:Int,columnIdx:Int) = tsInInt + columnIdx*6*60*60
}
object ThreeHourLevel extends AggregationLevel{
  val colKeyRange = 0 until 366*8
  val id = "3-hour"
  val shortId = "_t"
  val rowIncrement = new Period().withYears(1)
  val dateTimePattern = Utils.yyyyFormat
  override def getCellKeyFor(ts:DateTime) =  ((ts.getDayOfYear-1)*24+ ts.getHourOfDay) / 3
  override def getChildCells(ts:DateTime) = (ts.getHourOfDay-ts.getHourOfDay%3) until (ts.getHourOfDay-ts.getHourOfDay%3+3)
  def getCourserLevel() = Some(SixHourLevel)
  def getFinerLevel() = Some(OneHourLevel)
  override def colIndexToTimestamp(ts:DateTime,tsInInt:Int,columnIdx:Int) =tsInInt + columnIdx*3*60*60

}
object OneHourLevel extends AggregationLevel{
  val colKeyRange = 0 until  24
  val id = "1-hour"
  val shortId = "_h"
  val dateTimePattern = Utils.yyyyDDDFormat
  val rowIncrement = new Period().withDays(1)
  override def getCellKeyFor(ts:DateTime)=ts.getHourOfDay
  override def getChildCells(ts:DateTime)= ts.getHourOfDay*60/15 until (ts.getHourOfDay+1)*60/15
  def getCourserLevel() = Some(ThreeHourLevel)
  def getFinerLevel() = Some(FifteenMinuteLevel)
  override def colIndexToTimestamp(ts:DateTime,tsInInt:Int,columnIdx:Int) =tsInInt + columnIdx*1*60*60
}
object FifteenMinuteLevel extends AggregationLevel{
  val colKeyRange = 0 until 60 by 15
  val id = "15-minute"
  val shortId="_o"
  val dateTimePattern = Utils.yyyyDDDFormat
  val rowIncrement = new Period().withDays(1)
  override def getCellKeyFor(ts:DateTime)=ts.getMinuteOfDay / 15
  override def colIndexToTimestamp(ts:DateTime,tsInInt:Int,columnIdx:Int) =tsInInt + columnIdx*15*60
  def getCourserLevel() = Some(OneHourLevel)
  def getFinerLevel() = Some(FiveMinuteLevel)
  def getChildCells(ts:DateTime)=(ts.getMinuteOfDay-ts.getMinuteOfDay%15)/5 until (ts.getMinuteOfDay-ts.getMinuteOfDay%15+15)/5
}

object FiveMinuteLevel extends AggregationLevel{
  val colKeyRange = 0 until 60 by 5
  val id = "5-minute"
  val shortId = "_f"
  val dateTimePattern = Utils.yyyyDDDFormat
  val rowIncrement = new Period().withDays(1)
  override def getCellKeyFor(ts:DateTime)=ts.getMinuteOfDay / 5
  override def colIndexToTimestamp(ts:DateTime,tsInInt:Int,columnIdx:Int) =tsInInt + columnIdx*5*60
  def getCourserLevel() = Some(FifteenMinuteLevel)
  def getFinerLevel() = Some(OneMinuteLevel)
  def getChildCells(ts:DateTime) = (ts.getMinuteOfDay-ts.getMinuteOfDay%5) until (ts.getMinuteOfDay-ts.getMinuteOfDay%5+5)
}
object OneMinuteLevel extends AggregationLevel{
  val colKeyRange = 0 until 60
  val id = "1-minute"
  val shortId="_m"
  val dateTimePattern = Utils.yyyyDDDFormat
  val rowIncrement = new Period().withDays(1)
  override def getCellKeyFor(ts:DateTime)=ts.getMinuteOfDay
  override def colIndexToTimestamp(ts:DateTime,tsInInt:Int,columnIdx:Int) = tsInInt + columnIdx*60
  def getCourserLevel() = Some(FiveMinuteLevel)
  def getFinerLevel() = Some(RawLevel)
  def getChildCells(ts:DateTime) = ts.getMinuteOfDay*60 until (ts.getMinuteOfDay+1)*60
}
object RawLevel extends AggregationLevel{
  val colKeyRange = 0 until 86400
  val id = "raw"
  val shortId = "_r"
  val dateTimePattern = Utils.yyyyDDDFormat
  val rowIncrement = new Period().withDays(1)
  override def getCellKeyFor(ts:DateTime)=ts.getSecondOfDay
  override def colIndexToTimestamp(ts:DateTime,tsInInt:Int,columnIdx:Int) = tsInInt + columnIdx
  def getCourserLevel() = Some(OneMinuteLevel)
  def getFinerLevel() = None
  def getChildCells(ts:DateTime) = 0 until 0
}

class StreamIdIterator(sids:Set[String],from:DateTime,to:DateTime,level:AggregationLevel) extends Iterator[(Array[Byte],DateTime,String)] {
  override def hasNext = !sids.isEmpty && (periodIter.hasNext || sidIter.hasNext)
  var sidIter = sids.iterator
  def createPeriodIterator() = if (level == OneYearLevel) level.createPeriod(from,from) else level.createPeriod(from,to)
  var periodIter = createPeriodIterator()
  var sid:String = null
  override def next():(Array[Byte],DateTime,String) =
    if (periodIter.hasNext && sid !=null){
      val ts = periodIter.next()
      (level.rowKeyAsBytes(sid,ts),ts,sid)
    } else {
      periodIter= createPeriodIterator()
      sid = sidIter.next()
      next()
    }
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
  import StatAggHelpers._

  def receive = {
    case Insert(lvl,sid,ts,value,_)=>
      val aggLevel = AggregationLevel(lvl).get
      val row = aggLevel.rowColKey(sid,ts)
      val List(min,max,count,sum,sumSq) = store.get(row._1,row._2).map((x:Array[Byte])=>parse[List[Double]](Bytes.toString(x))).getOrElse(INITIAL_STAT)
      var stats: List[Double] = updateStats(value, min, max, count, sum, sumSq)
      store.put(row._1,row._2,Bytes.toBytes(generate(stats)))
      if (aggLevel.getCourserLevel().isDefined)
        sender ! Insert(aggLevel.getCourserLevel().get.id,sid,ts,value,Some(StatResult(sid,ts,aggLevel.id,stats)))
      else
        sender ! Done(sid,ts)

    case Update(lvl,sid,ts,_) =>
      val aggLevel = AggregationLevel(lvl).get
      // TODO: Update should use coprocessors in HBase
      val previousRow = aggLevel.getFinerLevel().get.rowKeyAsBytes(sid,ts)
      val childCells:Seq[Array[Byte]] = aggLevel.getChildCellsAsBytes(ts)
      val stats = aggLevel match {
        case OneMinuteLevel=>
          val stats = new SummaryStatistics()
          store.get(previousRow,childCells).filter(x=> x!=null && x.length>0 ).foreach{x=>
            stats.addValue(Bytes.toDouble(x))
          }
          if (stats.getN==0)
            StatAggHelpers.INITIAL_STAT
          else{
            List[Double](stats.getMin,stats.getMax,stats.getN,stats.getSum,stats.getSumsq)
          }
        case otherLvls=>
          store.get(previousRow,childCells).filter(_!=null).map(x=> parse[List[Double]](Bytes.toString(x))).foldLeft(INITIAL_STAT)((sum,item)=>mergeStats(sum,item))
      }
      store.put(aggLevel.rowKeyAsBytes(sid,ts),aggLevel.getCellKeyForAsByte(ts),Bytes.toBytes(generate(stats)))

      if (aggLevel.getCourserLevel().isDefined)
        sender ! Update(aggLevel.getCourserLevel().get.id,sid,ts,Some(StatResult(sid,ts,aggLevel.id,stats)))
      else
        sender ! Done(sid,ts)

    case RawData(sid,data,tz)=>
      store.put(sid,data,tz)
      Cache.stat_time_idx.call{redis=>
        data.foreach{item =>
          val ts =new DateTime(item._1*1000L,tz)
          val rowKey = RawLevel.rowColKey(sid,ts)
          item._2 match{
            case Some(value)=>
              redis.sadd(rowKey._1,rowKey._2).toInt match {
                case 0 => sender ! Update(OneMinuteLevel.id,sid,ts,None)
                case 1 => sender ! Insert(OneMinuteLevel.id,sid,ts,value,None)
              }
            case None => redis.srem(rowKey._1,rowKey._2).toInt match {
              case 1 => sender ! Update(OneMinuteLevel.id,sid,ts,None)
              case removedNonExisting => sender ! Done(sid,ts)
            }
          }
        }
      }
  }
}
