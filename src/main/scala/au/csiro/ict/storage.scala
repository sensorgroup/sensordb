package au.csiro.ict

import java.io.PrintWriter
import org.joda.time.DateTime

trait Storage2 {
  def put(streamId: String, values: Map[Int, Option[Double]]):Unit
  def get(streamId: String, from:Int,to:Int):Iterator[(Int,Double)]
  def get(streamId: String, fromDate:Int,toDate:Int,fromSecOfDay:Int,toSecOfDay:Int):Iterator[(Int,Double)] = get(streamId,fromDate,toDate).filter{(x)=>
    val d = new DateTime(x._1*1000L).getSecondOfDay
    d >=fromSecOfDay && d < toSecOfDay
  }
  def get(streamId: String, from:Int,to:Int,cf:ChunkFormatter){
    get(streamId,from,to).foldLeft(cf)((sum,item)=>sum.insert("s1",item._1,item._2)).done()
  }
  def get(streamId: String, from:Int,to:Int,fromSecOfDay:Int,toSecOfDay:Int,cf:ChunkFormatter){
    get(streamId,from,to,fromSecOfDay,toSecOfDay).foldLeft(cf)((sum,item)=>sum.insert("s1",item._1,item._2)).done()
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

