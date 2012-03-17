package sensordb

import java.io.{PrintStream}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Period, Days, DateTimeZone}
import scala.collection.JavaConversions._


/**
 * Generates keys per day per stream bases
 * @param prefix
 * @param fromDay (inclusive)
 * @param toDay (inclusive)
 * @param separator Separator used to separate dayIdx from prefixes
 */
class KeyGen(prefix:List[String],fromDay: String, toDay: String,separator:Char=Utils.SEPARATOR) extends Iterator[List[String]] {
  override def hasNext = !from.isAfter(to)
  var from = Utils.format.parseDateTime(fromDay)
  val to = Utils.format.parseDateTime(toDay)
  override def next() = {
    val to_return= prefix.map(p=>new StringBuilder(p).append(separator).append(Utils.format.print(from)).toString())
    from = from.plusDays(1)
    to_return
  }
}

trait ChunkFormatter{
  def done()
  def insert(sensor:String, newYearDay:String,secInDay:Int,value:String):Boolean
}

trait ChunkWriter {
  def open()={
    openWriter()
    isOpenValue=true
  }
  var isOpenValue = false

  def insert(sensor:String, ts:String,value:String){
    if (isClosed()|| !isOpened()) throw new RuntimeException("Bad state exception.")
    insertData(sensor,ts,value)
  }
  def close() = {
    isClosedValue=true
    closeWriter()
  }

  protected def openWriter()
  protected def closeWriter()
  protected def insertData(sensor:String, ts:String,value:String)
  var isClosedValue = false
  def isClosed() = isClosedValue
  def isOpened() = isOpenValue
}

class InMemWriter extends ChunkWriter{
  def openWriter() = {}
  var to_return = List[List[String]]()
  def insertData(sensor:String, ts:String,value:String)= to_return::=List(sensor,ts,value)
  def closeWriter() = {}
  /*
  The return result is verse order of insert calls.
   */
  def getData = to_return
}

class JSONWriter(val output:PrintStream) extends ChunkWriter{
  def openWriter() =output.print("{")
  var started = false;
  def insertData(sensor:String, ts:String,value:String)= {
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
  var dayIdx:DateTime = null
  var tempYearDay:String = null
  def insert(sensor:String, newYearDay:String,secInDay:Int,value:String):Boolean={
    if (tempYearDay !=newYearDay) {
      dayIdx= Utils.format.parseDateTime(newYearDay)
      tempYearDay=newYearDay
    }
    val ts = Utils.inputTimeFormat.print(dayIdx.plusSeconds(secInDay))
    count+=1
    writer.insert(sensor,ts,value)
    true
  }
  def done() = writer.close()
}

trait SensorDataStore {
  def addNodeData(nodeId: String, data: Map[String, Map[String, String]])
  def queryNode(nodeId:String,keys:Iterator[List[String]],timeRange:Option[(Long, Long)] = None,chunker:ChunkFormatter)
  def dropNode(nodeId:String)
  def shutdown()
}
object Utils {
  val format = DateTimeFormat.forPattern("yyyyD")
  val inputTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
  val zoneUTC = DateTimeZone.UTC
  val SEPARATOR = '$'
  DateTimeZone.setDefault(zoneUTC)
  def generateRowKey(sensor:String, date:String) = sensor+"$"+date

}
class CassandraDataStore{
  /**Resource: https://github.com/rantav/hector/blob/master/core/src/test/java/me/prettyprint/cassandra/service/CassandraClusterTest.java#L102-156 */

  import me.prettyprint.cassandra.model._
  import me.prettyprint.cassandra.serializers._
  import me.prettyprint.cassandra.service._
  import me.prettyprint.hector.api._
  import me.prettyprint.hector.api.ddl._
  import me.prettyprint.hector.api.factory._
  import me.prettyprint.hector.api.mutation._

  val keyspace_name = "sensordb"
  val c = getClusterFor("sensordb-cluster", "localhost:9160")
  val ks = HFactory.createKeyspace(keyspace_name, c)
  val ss = StringSerializer.get()
  val ls = LongSerializer.get().asInstanceOf[Serializer[Any]]
  val ds = DoubleSerializer.get()

  def getClusterFor(clusterName: String, address: String) = HFactory.getOrCreateCluster(clusterName, address)

  def addCf(c: Cluster, ksName: String, cfName: String) = c.addColumnFamily(HFactory.createColumnFamilyDefinition(ksName, cfName, ComparatorType.LONGTYPE))

  def isColFamilyExists(cfName: String) = c.describeKeyspace(keyspace_name).getCfDefs().exists((cf) => cf.getName == cfName)

  def addNodeData(nodeId: String, data: Map[String, Map[String, String]]) = {
    if (!isColFamilyExists(nodeId)) addCf(c, ks.getKeyspaceName, nodeId)
    val mutator: Mutator[String] = HFactory.createMutator(ks, ss)
    var total = 0
    data.foreach { (s) =>
      val sensorId = s._1
      s._2.foreach {(v) =>
        val ts = Utils.inputTimeFormat.parseDateTime(v._1)
        val value = v._2
        val row_key = Utils.generateRowKey(sensorId,Utils.format.print(ts))
        mutator.addInsertion(row_key, nodeId, HFactory.createColumn(ts.getSecondOfDay.asInstanceOf[Long], value,ls,ss))
        total +=1
        if (total % 250 ==0) mutator.execute()
      }
    }
    mutator.execute()
  }

  def queryNode(colFamName:String,keys:Iterator[List[String]],colRange:Option[(Long, Long)] = None,chunker:ChunkFormatter){
    if (!isColFamilyExists(colFamName)) {
      chunker.done()
      return
    }
    val colStart = colRange.getOrElse(Pair(0L,86400L))._1.asInstanceOf[AnyVal]
    val colEnd = colRange.getOrElse(Pair(0L,86400L))._2.asInstanceOf[AnyVal]
    while(keys.hasNext){
      val keys_iterator = keys.next().iterator
      while(keys_iterator.hasNext){
        val key=keys_iterator.next()
        val measurment_dayIdx=key.split(Utils.SEPARATOR)
        val cols = new ColumnSliceIterator[String, Any, String](HFactory.createSliceQuery(ks, ss, ls, ss).setKey(key).setColumnFamily(colFamName), colStart, colEnd, false)
        while(cols.hasNext){
          val column=cols.next()
          val secInDay=column.getName.asInstanceOf[Long].asInstanceOf[Int]
          val value = column.getValue
          chunker.insert(measurment_dayIdx(0),measurment_dayIdx(1),secInDay,value)
        }
      }
    }
    chunker.done()
  }
  def dropNode(colFamName:String)= if (isColFamilyExists(colFamName)) c.dropColumnFamily(keyspace_name, colFamName,true)

  def shutdown()=c.getConnectionManager().shutdown()
}

object Sample{
  def main(args: Array[String]) {

    val time = System.currentTimeMillis
    //    for (day<- 0 until 50){
    //      val sample_data_map = List("temperature","humidity","light").map{(m) =>
    //        m -> {for (idx <- 0 until 86400*1 by 1) yield inputTimeFormat.print(time-idx*1000-86400*1000*day)->Math.random.toString} .toMap
    //      }.toMap
    //      addSensorData(c,keyspace,"node5",sample_data_map)
    //      println("Day:"+day)
    //    }
    //
    println("---------------------------")
    //    preparedDateRange(c, keyspace, "node5", dateRange(List("temperature","light","humidity"),"20001", "201590")).foreach(println);
    //    planB(keyspace,"node5",new KeyGen(List("temperature"),"20111", "201240"),Some((10,100)),{chunk=>
    //      true
    //    })
    //    val start=System.currentTimeMillis()
    //    var count = 0;
    val cs = new CassandraDataStore()
    cs.queryNode("node5",new KeyGen(List("light","humidity"),"201230", "201290"),None,new DefaultChunkFormatter(new JSONWriter(System.out)))
    //    println(count/((System.currentTimeMillis()-start)/1000.0))
    //    println("count"+count)

  }
}
