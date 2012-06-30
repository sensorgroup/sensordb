package au.csiro.ict

import java.io.{Writer, PrintWriter}
import org.joda.time.DateTimeZone

/**
 * Storage class is Timezone aware, this means:
 * When you call set, timestamp should be in milliseconds from EPOCH in local Timezone. A separate Timezone parameter can be provided to correctly create buckets
 * When retrieving data, timestamp should be in milliseconds from EPOCH in local Timezone.
 */
trait Storage {
  def getPrefixed(prefix:String):Iterable[Array[Byte]]

  def get(streamIds:Set[String],fromTime:Int,toTime:Int,columns:Option[(Int,Int)],tz:DateTimeZone,level:AggregationLevel,chunker:ChunkFormatter):Unit
  def get(row:Array[Byte],column:Array[Byte]):Option[Array[Byte]]
  def get(row: Array[Byte], cols:Seq[Array[Byte]]):Seq[Array[Byte]]

  def put(row: Array[Byte], col:Array[Byte],value:Array[Byte]):Unit
  def put(streamId: String, values: Map[Int, Option[Double]],tz:DateTimeZone):Unit

  def drop(streamId:String)
  def close()
}


trait ChunkFormatter{
  def done()
  def insert(sensor:String, ts:Int, value:Double):ChunkFormatter
  def insert(sensor:String, ts:Int, min:Double,max:Double,count:Double,sum:Double,sumSq:Double):ChunkFormatter
}

trait ChunkWriter {
  def open()={
    openWriter()
    isOpenValue=true
  }
  var isOpenValue = false

  def insert(sensor:String, ts:Int,min:Double,max:Double,count:Double,sum:Double,sumSq:Double){
    if (isClosed()|| !isOpened()) throw new RuntimeException("Bad state exception.")
    insertData(sensor,ts,min,max,count,sum,sumSq)
  }
  def insert(sensor:String, ts:Int,value:Double){
    if (isClosed()|| !isOpened()) throw new RuntimeException("Bad state exception.")
    insertData(sensor,ts,value)
  }
  def close() = {
    isClosedValue=true
    closeWriter()
  }

  protected def openWriter()
  protected def closeWriter()
  protected def insertData(sensor:String, ts:Int,value:Double)
  protected def insertData(sensor:String, ts:Int,min:Double,max:Double,count:Double,sum:Double,sumSq:Double)
  var isClosedValue = false
  def isClosed() = isClosedValue
  def isOpened() = isOpenValue
}

class InMemWriter extends ChunkWriter{
  def openWriter() = {}
  var to_return = List[(String,Int,Double)]()
  var to_return_stat = List[(String,Int,Double,Double,Double,Double,Double)]()
  def insertData(sensor:String, ts:Int,value:Double)= to_return::=(sensor,ts,value)
  def insertData(sensor:String, ts:Int,min:Double,max:Double,count:Double,sum:Double,sumSq:Double)= to_return_stat::=(sensor,ts,min,max,count,sum,sumSq)
  def closeWriter() = {}
  /*
  The return result is verse order of put calls.
   */
  def getData = to_return
}

class JSONWriter(val output:Writer) extends ChunkWriter{
  def openWriter() =output.write("{")
  var started = false;
  def insertData(sid:String, ts:Int,min:Double,max:Double,count:Double,sum:Double,sumSq:Double)= {
    if (started){
      output.write(",")
    }
    started=true
    output.write(List(sid,ts,min,max,count,sum,sumSq).mkString("[",",","]"))
  }

  def insertData(sensor:String, ts:Int,value:Double)= {
    if (started){
      output.write(",")
    }
    started=true
    val to_write = "["+sensor+","+ts+","+value+"]";
    output.write(to_write)
  }
  def closeWriter() = {
    output.write("}")
    output.flush()
  }
}
class JSONWriter2(val output:Writer) extends ChunkWriter{
  var seenIds = Set[String]()
  def openWriter() =output.write("{")
  var started = false
  var previousSid:String = ""
  def insertData(sid:String, ts:Int,min:Double,max:Double,count:Double,sum:Double,sumSq:Double)= {
    val toAppend = new StringBuilder("[").append(ts).append(",").append(min).append(",").append(max).append(",").append(count).append(",").append(sum).append(",").append(sumSq).append("]").toString()
    if (sid != previousSid){
      if (seenIds.contains(sid)) throw new RuntimeException("This is a bug, bad output produced ...")
      seenIds+=sid
      if (previousSid!="")
        output.write("],")
      output.write("\""+sid+"\":[")
      output.write(toAppend)
      previousSid = sid
      started=true
    }else {
      output.write(",")
      output.write(toAppend)
    }
  }

  def insertData(sid:String, ts:Int,value:Double)= {
    val toAppend = new StringBuilder("[").append(ts).append(",").append(value).append("]").toString()
    if (sid != previousSid){
      if (started)
        output.write("],")
      output.write("\""+sid+"\":[")
      output.write(toAppend)
      previousSid = sid
      started = true
    }else {
      output.write(",")
      output.write(toAppend)
    }
  }
  def closeWriter() = {
    if (started)
      output.write("]")
    output.write("}")
    output.flush()
  }
}

class DefaultChunkFormatter(val writer:ChunkWriter) extends ChunkFormatter{
  writer.open()
  var item_count = 0
  def insert(sensor:String, ts:Int,value:Double):DefaultChunkFormatter={
    item_count+=1
    writer.insert(sensor,ts,value)
    this
  }

  def insert(sensor: String, ts: Int, min: Double, max: Double, count: Double, sum: Double, sumSq: Double):DefaultChunkFormatter = {
    item_count+=1
    writer.insert(sensor,ts,min,max,count,sum,sumSq)
    this
  }

  def done() = writer.close()
}

