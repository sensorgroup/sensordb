package au.csiro.ict

import scala.collection.JavaConversions._
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util._
import org.apache.hadoop.hbase._
import filter.{FirstKeyOnlyFilter, FilterList, KeyOnlyFilter}
import org.joda.time.{DateTimeZone, DateTime}
import com.codahale.jerkson.Json._

class HbaseStorage extends Storage {
  def getPrefixed(prefix:String):Iterable[Array[Byte]] = {
    hbase.getScanner(new Scan(Bytes.toBytes(prefix),Bytes.toBytes(nextString(prefix))).setFilter(new FilterList(new FirstKeyOnlyFilter,new KeyOnlyFilter))).map(x=>x.getRow)
  }

  def put(row: Array[Byte], col:Array[Byte],value:Array[Byte])= hbase.put(new Put(row).add(data,col,value))

  def get(row: Array[Byte], col:Array[Byte]):Option[Array[Byte]]= hbase.get(new Get(row).addColumn(data,col)).value() match {
    case null => None
    case value:Array[Byte]=> Some(value)
  }

  val conf = HBaseConfiguration.create()
  val hbase: HTableInterface = new HTable(conf, "data")
  val data = Bytes.toBytes("data")

  def get(row: Array[Byte], cols:Seq[Array[Byte]]):Seq[Array[Byte]]= hbase.get{
    val get = new Get(row)
    cols.foreach(x=>get.addColumn(data,x))
    get.setMaxVersions(1)
  } match {
    case null => Nil
    case notNull =>notNull.raw().map{x=>
      x.getValue
    }.toSeq
  }

  def put(s: String, values: Map[Int, Option[Double]],tz:DateTimeZone) = values.foreach{item=>
    val time = new DateTime(item._1*1000L,tz)
    val put = new Put(RawLevel.rowKeyAsBytes(s,time))
    put.add(data,RawLevel.getCellKeyForAsByte(time),item._2.map(Bytes.toBytes).getOrElse(null))
    hbase.put(put)
  }

  override def drop(prefix:String)={
    val rows = hbase.getScanner(new Scan(Bytes.toBytes(prefix),Bytes.toBytes(nextString(prefix))).setFilter(new FilterList(new FirstKeyOnlyFilter,new KeyOnlyFilter))).map(x=>new Delete(x.getRow)).toList
    rows.foreach(hbase.delete)
  }

  def nextString(s:String)=new StringBuilder(s.substring(0,s.length-1)).append((s.charAt(s.length-1)+1).asInstanceOf[Char]).toString()

  override def close() = hbase.close()

  def get(streamIds: Set[String], fromTime: Int, toTime: Int, columns: Option[(Int, Int)], tz: DateTimeZone, level: AggregationLevel, chunker: ChunkFormatter) {
    val fromDateTime = new DateTime(fromTime*1000L).withZone(tz)
    val toDateTime = new DateTime(toTime*1000L).withZone(tz)
    val parser = level.dateTimePattern.withZone(tz)
    streamIds.foreach{sid=>
      val scan = new Scan(level.rowKeyAsBytes(sid,fromDateTime),level.rowKeyAsBytes(sid,level.nextRowTimeStamp(toDateTime))).addFamily(data).setMaxVersions(1)

      val scanner = hbase.getScanner(scan)
      scanner.foreach{ row =>
        val rowString = Bytes.toString(row.getRow)
        val current = parser.parseDateTime(AggregationLevel.rowToTs(rowString)._3)
        val currentInTs = (current.getMillis/1000L).asInstanceOf[Int]
        row.raw().foreach{cell=>
          val value = cell.getValue
          if (value !=null){
            val key = Bytes.toInt(cell.getQualifier)
            if (columns.isEmpty || (columns.get._1 <= key && columns.get._2 >= key))  {
              if (level == RawLevel)
                chunker.insert(sid,level.colIndexToTimestamp(current,currentInTs,key),Bytes.toDouble(value))
              else{
                val List(minTs,maxTs,min,max,count,sum,sumSq) = parse[List[Double]](Bytes.toString(value))
                chunker.insert(sid,minTs,maxTs ,min,max,count,sum,sumSq) // level.colIndexToTimestamp(current,currentInTs,key)

              }
            }
          }
        }
      }
      scanner.close()
    }

  }
}