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
import filter.PrefixFilter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import scala.util.Sorting
import Utils.yyyyDDDFormat

class HbaseStorage extends Storage2 {

  class ResultScannerToIterator(rs:ResultScanner) extends Iterator[(Int,Double)]{
    var queue =  new Queue[(Int,Double)]()
    val it:Iterator[Result] = rs.iterator()
    var dayTs:Int = 0
    def hasNext = !queue.isEmpty || it.hasNext

    def next():(Int,Double) = {
      if (queue.isEmpty){
        val nextItem = it.next()
        dayTs = (yyyyDDDFormat.parseDateTime(Bytes.toString(nextItem.getRow).split("\\$").apply(1)).getMillis/1000L).asInstanceOf[Int]
        queue ++= (nextItem.getNoVersionMap.get(seconds).map(x=>(Bytes.toInt(x._1)+dayTs)->Bytes.toDouble(x._2)).toList.sortWith((a,b)=> a._1>b._1))
      }
      queue.dequeue
    }

    def close = rs.close()

  }
  val conf = HBaseConfiguration.create()
  val second_table: HTableInterface = new HTable(conf, "data")
  val seconds = Bytes.toBytes("seconds")


  def put(s: String, values: Map[Int, Option[Double]]) = values.foreach{item=>
    val time = new DateTime(item._1*1000L)
    val put = new Put(Bytes.toBytes(s+"$"+yyyyDDDFormat.print(time)))
    put.add(seconds,Bytes.toBytes(time.getSecondOfDay),item._2.map(Bytes.toBytes).getOrElse(null))
    second_table.put(put)
  }

  override def drop(streamId:String){
    second_table.getScanner(new Scan().setFilter(new PrefixFilter(Bytes.toBytes(streamId)))).foreach{ (x)=>
      second_table.delete(new Delete(x.getRow))
    }
  }

  override def get(s: String, from:Int,to:Int) ={
    val fromTs = Bytes.toBytes(s+"$"+yyyyDDDFormat.print(new DateTime(from*1000L)))
    val toTs = Bytes.toBytes(s+"$"+yyyyDDDFormat.print(new DateTime(to*1000L)))
    new ResultScannerToIterator(second_table.getScanner(new Scan(fromTs,toTs).addFamily(seconds)))
  }
  override def close() = second_table.close()

}