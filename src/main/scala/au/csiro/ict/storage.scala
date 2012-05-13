package au.csiro.ict

import java.io.PrintWriter
import org.joda.time.{DateTimeZone, DateTime}

/**
 * Storage class is Timezone aware, this means:
 * When you call set, timestamp should be in milliseconds from EPOCH in local Timezone. A separate Timezone parameter can be provided to correctly create buckets
 * When retrieving data, timestamp should be in milliseconds from EPOCH in local Timezone.
 */
trait Storage {
  def getPrefixed(prefix:String):Iterable[Array[Byte]]
  def put(row: Array[Byte], col:Array[Byte],value:Array[Byte]):Unit
  def get(row: Array[Byte], col:Array[Byte]):Option[Array[Byte]]
  def get(row: Array[Byte], cols:Seq[Array[Byte]]):Seq[(Array[Byte],Array[Byte])]
  def put(streamId: String, values: Map[Int, Option[Double]],tz:DateTimeZone):Unit
  def get(streamId: String, from:Int,to:Int,tz:DateTimeZone):Iterator[(Int,Double)]
  def get(streamId: String, fromDate:Int,toDate:Int,fromSecOfDay:Int,toSecOfDay:Int,tz:DateTimeZone):Iterator[(Int,Double)] = get(streamId,fromDate,toDate,tz).filter{(x)=>
    val d = new DateTime(x._1*1000L,tz).getSecondOfDay
    d >=fromSecOfDay && d < toSecOfDay
  }
  def get(streamId: String, from:Int,to:Int,tz:DateTimeZone,cf:ChunkFormatter){
    get(streamId,from,to,tz).foldLeft(cf)((sum,item)=>sum.insert("s1",item._1,item._2)).done()
  }
  def get(streamId: String, from:Int,to:Int,fromSecOfDay:Int,toSecOfDay:Int,tz:DateTimeZone,cf:ChunkFormatter){
    get(streamId,from,to,fromSecOfDay,toSecOfDay,tz).foldLeft(cf)((sum,item)=>sum.insert("s1",item._1,item._2)).done()
  }
  def drop(streamId:String)
  def close()
}


trait ChunkFormatter{
  def done()
  def insert(sensor:String, ts:Int, value:Double):ChunkFormatter
}

trait ChunkWriter {
  def open()={
    openWriter()
    isOpenValue=true
  }
  var isOpenValue = false

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
  var isClosedValue = false
  def isClosed() = isClosedValue
  def isOpened() = isOpenValue
}

class InMemWriter extends ChunkWriter{
  def openWriter() = {}
  var to_return = List[(String,Int,Double)]()
  def insertData(sensor:String, ts:Int,value:Double)= to_return::=(sensor,ts,value)
  def closeWriter() = {}
  /*
  The return result is verse order of put calls.
   */
  def getData = to_return
}

class JSONWriter(val output:PrintWriter) extends ChunkWriter{
  def openWriter() =output.print("{")
  var started = false;
  def insertData(sensor:String, ts:Int,value:Double)= {
    if (started){
      output.print(",")
      started=true
    }
    val to_write = "["+sensor+","+ts+","+value+"]";
    output.print(to_write)
  }
  def closeWriter() = output.print("}")
}

class DefaultChunkFormatter(val writer:ChunkWriter) extends ChunkFormatter{
  writer.open()
  var count = 0
  def insert(sensor:String, ts:Int,value:Double):DefaultChunkFormatter={
    count+=1
    writer.insert(sensor,ts,value)
    this
  }
  def done() = writer.close()
}

