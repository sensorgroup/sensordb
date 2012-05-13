package au.csiro.ict
import collection.mutable.Queue
import collection.mutable.ListBuffer
import java.util.Collections
import org.apache.hadoop.fs.Path;
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver._
import org.apache.hadoop.hbase.util._
import org.apache.hadoop.hbase._
import filter.{FirstKeyOnlyFilter, FilterList, KeyOnlyFilter, PrefixFilter}
import org.joda.time.format.DateTimeFormat
import scala.util.Sorting
import Utils.yyyyDDDFormat
import org.joda.time.{DateTimeZone, DateTime}

class HbaseStorage extends Storage {
  def getPrefixed(prefix:String):Iterable[Array[Byte]] = {
    hbase.getScanner(new Scan(Bytes.toBytes(prefix),Bytes.toBytes(nextString(prefix))).setFilter(new FilterList(new FirstKeyOnlyFilter,new KeyOnlyFilter))).map(x=>x.getRow)
  }

  def put(row: Array[Byte], col:Array[Byte],value:Array[Byte])= hbase.put(new Put(row).add(data,col,value))

  def get(row: Array[Byte], col:Array[Byte]):Option[Array[Byte]]= hbase.get(new Get(row).addColumn(data,col)).value() match {
    case null => None
    case value:Array[Byte]=> Some(value)
  }

  class ResultScannerToIterator(rs:ResultScanner,tz:DateTimeZone) extends Iterator[(Int,Double)]{
    var queue =  new Queue[(Int,Double)]()
    val it:Iterator[Result] = rs.iterator()
    var dayTs:Int = 0
    def hasNext = !queue.isEmpty || it.hasNext

    def next():(Int,Double) = {
      if (queue.isEmpty){
        val nextItem = it.next()
        dayTs = (yyyyDDDFormat.withZone(tz).parseDateTime(Bytes.toString(nextItem.getRow).split("_").apply(1)).getMillis/1000L).asInstanceOf[Int]
        queue ++= (nextItem.getNoVersionMap.get(data).map(x=>(Bytes.toInt(x._1)+dayTs)->Bytes.toDouble(x._2)).toList.sortWith((a,b)=> a._1>b._1))
      }
      queue.dequeue
    }

    def close = rs.close()

  }
  val conf = HBaseConfiguration.create()
  val hbase: HTableInterface = new HTable(conf, "data")
  val data = Bytes.toBytes("data")

  def get(row: Array[Byte], cols:Seq[Array[Byte]]):Seq[(Array[Byte],Array[Byte])]= hbase.get{
    val get = new Get(row)
    cols.foreach(x=>get.addColumn(data,x))
    get
  }.getNoVersionMap match {
    case null => Nil
    case notNull =>notNull.values().head.toSeq
  }


  def put(s: String, values: Map[Int, Option[Double]],tz:DateTimeZone) = values.foreach{item=>
    val time = new DateTime(item._1*1000L,tz)
    val put = new Put(Bytes.toBytes(ColumnOrientedDBKeyHelper.rowKey(s,time,AggLevel.RAW)))
    put.add(data,Bytes.toBytes(time.getSecondOfDay),item._2.map(Bytes.toBytes).getOrElse(null))
    hbase.put(put)
  }

  override def drop(prefix:String)={
    val rows = hbase.getScanner(new Scan(Bytes.toBytes(prefix),Bytes.toBytes(nextString(prefix))).setFilter(new FilterList(new FirstKeyOnlyFilter,new KeyOnlyFilter))).map(x=>new Delete(x.getRow)).toList
    rows.foreach(hbase.delete)
  }

  def nextString(s:String)=new StringBuilder(s.substring(0,s.length-1)).append((s.charAt(s.length-1)+1).asInstanceOf[Char]).toString()

  override def get(s: String, from:Int,to:Int,tz:DateTimeZone) ={
    val fromTs = Bytes.toBytes(ColumnOrientedDBKeyHelper.rowKey(s,new DateTime(from*1000L,tz),AggLevel.RAW))
    val toTs = Bytes.toBytes(ColumnOrientedDBKeyHelper.rowKey(s,new DateTime(to*1000L,tz),AggLevel.RAW))
    new ResultScannerToIterator(hbase.getScanner(new Scan(fromTs,toTs).addFamily(data)),tz)
  }
  override def close() = hbase.close()

}