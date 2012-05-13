package au.csiro.ict

import redis.clients.jedis.Jedis
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.apache.hadoop.hbase.util.Bytes
import org.joda.time.{DateTimeZone, DateTime}

class RedisStore extends Storage {
  val jedis = new Jedis("localhost")
  jedis.select(Cache.REDIS_STORE)
  def getPrefixed(prefix: String):Iterable[Array[Byte]] = {
    jedis.keys(prefix).map(Bytes.toBytes).toSeq
  }

  def put(row: Array[Byte], col: Array[Byte], value: Array[Byte]) {
    jedis.hset(row,col,value)
  }

  def get(row: Array[Byte], col: Array[Byte]):Option[Array[Byte]] = {
   jedis.hget(row,col) match {
     case null => None
     case value => Some(value)
   }
  }

  def get(row: Array[Byte], cols: Seq[Array[Byte]]):Seq[(Array[Byte],Array[Byte])]=
    cols.map(c=> c->jedis.hget(row,c))

  def put(streamId: String, values: Map[Int, Option[Double]],tz:DateTimeZone) {
    values.foreach{v=>
      val row = ColumnOrientedDBKeyHelper.rowOf(streamId,new DateTime(v._1*1000L,tz),AggLevel.RAW)
      if (v._2.isDefined){
        println("setting:"+Bytes.toInt(row._2))
        jedis.hset(Bytes.toBytes(row._1),row._2,Bytes.toBytes(v._2.get))
      }
      else
        jedis.hdel(Bytes.toBytes(row._1),row._2)
    }
  }

  def get(streamId: String, from: Int, to: Int,tz:DateTimeZone):Iterator[(Int,Double)] = {
    var toReturn = List[(Int,Double)]()
    var current = new DateTime(from*1000L).withZone(tz)
    while(current.isBefore(new DateTime(to*1000L).withZone(tz))){
      toReturn ++= jedis.hgetAll(Bytes.toBytes(ColumnOrientedDBKeyHelper.rowKey(streamId,current,AggLevel.RAW))).map(x=>(current.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).getMillis/1000L).asInstanceOf[Int]+Bytes.toInt(x._1)->Bytes.toDouble(x._2)).toList
      current = current.plusDays(1)
    }
    toReturn.reverse.iterator
  }

  def drop(streamId: String) {
     getPrefixed(streamId+"*").foreach((x)=>jedis.del(x))
  }

  def close() {
  }
}
