package au.csiro.ict

import redis.clients.jedis.Jedis
import scala.collection.JavaConversions._
import org.apache.hadoop.hbase.util.Bytes
import org.joda.time.{DateTime, DateTimeZone}
import com.codahale.jerkson.Json._

class RedisStore extends Storage {

  private val jedis = new RedisPool(Cache.SensorDBConf.getString("data-store.redis.host"),Cache.SensorDBConf.getInt("data-store.redis.port"),Cache.REDIS_STORE)
  def getPrefixed(prefix: String):Iterable[Array[Byte]] = {
    jedis.call(j=>j.keys(prefix+"*").map(Bytes.toBytes).toSeq)
  }

  def put(row: Array[Byte], col: Array[Byte], value: Array[Byte]) {
    jedis.call(j=>j.hset(row,col,value))
  }

  def put(streamId: String, values: Map[Int, Option[Double]]) {

    values.foreach{v=>
      val row = RawLevel.rowColKey(streamId,new DateTime(v._1*1000L))
      if (v._2.isDefined){
        jedis.call{jedis=>jedis.hset(row._1,row._2,Bytes.toBytes(v._2.get))}
      }
      else
        jedis.call{jedis=>jedis.hdel(row._1,row._2)}
    }
  }
  def get(streamIds:Set[String],fromTime:Int,toTime:Int,columns:Option[(Int,Int)],level:AggregationLevel,chunker:ChunkFormatter){
    val fromDateTime = new DateTime(fromTime*1000L)
    val toDateTime = new DateTime(toTime*1000L)
    val period = new StreamIdIterator(streamIds,fromDateTime,toDateTime,level)
    while (period.hasNext){
      val (row,ts,sid)=period.next()
      var current = ts.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0)
      val currentInTs = (current.getMillis/1000L).asInstanceOf[Int]
      val data =  jedis.call{jedis=>jedis.hgetAll(row)}
      if (data !=null && !data.isEmpty){
        data.iterator.foreach{kv:(Array[Byte],Array[Byte])=>
          if (kv._2 != null){
            val key =Bytes.toInt(kv._1)
            if (columns.isEmpty || (columns.get._1 <= key && columns.get._2 >= key)) {
              if (level == RawLevel)
                chunker.insert(sid,level.colIndexToTimestamp(current,currentInTs,key),Bytes.toDouble(kv._2))
              else{
                val List(minTs,maxTs,min,max,count,sum,sumSq) = parse[List[Double]](Bytes.toString(kv._2))
                chunker.insert(sid,minTs,maxTs, min,max,count,sum,sumSq)
              }
            }
          }
        }
      }
    }
  }

  def get(row:Array[Byte],col:Array[Byte]):Option[Array[Byte]] = {
    jedis.call{jedis=>jedis.hget(row,col)} match {
      case null => None
      case value => Some(value)
    }
  }

  def get(row: Array[Byte], cols:Seq[Array[Byte]]):Seq[Array[Byte]]= jedis.call{jedis=>jedis.hmget(row,cols :_*)}

  def drop(streamId: String) {
      getPrefixed(streamId).foreach((x)=>jedis.call{jedis=>jedis.del(x)})
  }

  def close() {
  }
}
