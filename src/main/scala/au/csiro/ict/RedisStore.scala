package au.csiro.ict

import redis.clients.jedis.Jedis
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.apache.hadoop.hbase.util.Bytes
import org.joda.time.DateTime
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


  def get(streamIds:Set[String],from:Option[DateTime],to:Option[DateTime],columns:Option[(Int,Int)],level:AggregationLevel,chunker:ChunkFormatter){
    streamIds.iterator.flatMap{sid=>
      val prefix = sid+level.shortId+"*"
      val fromKey = from.map(x=>level.rowKeyAsBytes(sid,x)).getOrElse(null)
      val toKey =  to.map(x=>level.rowKeyAsBytes(sid,x)).getOrElse(null)
      jedis.call(_.keys(Bytes.toBytes(prefix))).filter{row_key=>
        if (fromKey == null && toKey == null)
          true
        else if (fromKey != null && toKey != null)
          Bytes.compareTo(row_key,fromKey) >=0 && Bytes.compareTo(row_key,toKey) <=0
        else if (fromKey != null)
          Bytes.compareTo(row_key,fromKey) >=0
        else
          Bytes.compareTo(toKey,row_key) <=0
      }
    }.foreach{rowKey=>
      val value = jedis.call(_.hgetAll(rowKey))
      if (value != null){
        val (sid,lvl,ts) = AggregationLevel.rowToTs(Bytes.toString(rowKey))
        val tsInDT = lvl.dateTimePattern.parseDateTime(ts)
        value.filterKeys{k=>
          columns.isEmpty || {
            val colIdx = Bytes.toInt(k)
            (columns.get._1 <= colIdx && columns.get._2 >= colIdx)
          }
        }.foreach{kv=>
          if (level == RawLevel)
            chunker.insert(sid,level.colIndexToTimestamp(tsInDT,Utils.dateTimeToInt(tsInDT),Bytes.toInt(kv._1)),Bytes.toDouble(kv._2))
          else{
            val List(minTs,maxTs,minTsValue,maxTsValue,min,max,count,sum,sumSq) = parse[List[Double]](Bytes.toString(kv._2))
            chunker.insert(sid,minTs,maxTs, minTsValue,maxTsValue,min,max,count,sum,sumSq)
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
