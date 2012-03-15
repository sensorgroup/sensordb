import java.text.SimpleDateFormat
import java.util.{Calendar, GregorianCalendar}
import me.prettyprint.hector.api.query.MultigetSliceQuery
import org.joda.time.format.DateTimeFormat
import org.joda.time.{Period, Days, DateTimeZone}
import scala.collection.JavaConversions._

object CassandraStore {
  /**Resource: https://github.com/rantav/hector/blob/master/core/src/test/java/me/prettyprint/cassandra/service/CassandraClusterTest.java#L102-156 */

  import me.prettyprint.cassandra.model._
  import me.prettyprint.cassandra.serializers._
  import me.prettyprint.cassandra.service._
  import me.prettyprint.hector.api._
  import me.prettyprint.hector.api.ddl._
  import me.prettyprint.hector.api.factory._
  import me.prettyprint.hector.api.mutation._

  val keyspace_name = "sensordb"
  val ss = StringSerializer.get()
  val ls = LongSerializer.get().asInstanceOf[Serializer[Any]]
  val ds = DoubleSerializer.get()
  val format = DateTimeFormat.forPattern("yyyyD")
  val inputTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
  val zoneUTC = DateTimeZone.UTC
  val SEPARATOR = "$"

  DateTimeZone.setDefault(zoneUTC)

  def getClusterFor(clusterName: String, address: String) = HFactory.getOrCreateCluster(clusterName, address)

  def dropCf(c: Cluster, ksName: String, cfName: String) = c.dropColumnFamily(ksName, cfName);

  def addCf(c: Cluster, ksName: String, cfName: String) = c.addColumnFamily(HFactory.createColumnFamilyDefinition(ksName, cfName, ComparatorType.UTF8TYPE))

  def colFamily(c: Cluster, ksName: String, cfName: String) = c.describeKeyspace(ksName).getCfDefs().exists((cf) => cf.getName == cfName)

  def generateRowKey(sensor:String, date:String) = sensor+"$"+date

  def addSensorData(c: Cluster, ks: Keyspace, nodeId: String, data: Map[String, Map[String, String]]) = {
    if (!colFamily(c, ks.getKeyspaceName, nodeId)) addCf(c, ks.getKeyspaceName, nodeId)

    val mutator: Mutator[String] = HFactory.createMutator(ks, ss)
    data.foreach {
      (s) =>
        val sensorId = s._1
        s._2.foreach {
          (v) =>
            val ts = inputTimeFormat.parseDateTime(v._1)
            val value = v._2
            val row_key = generateRowKey(sensorId,format.print(ts))
            mutator.addInsertion(row_key, nodeId, HFactory.createColumn(ts.getSecondOfDay, value,ls,ss))
        }
    }
    mutator.execute()
  }

  def getAllSensorData(c: Cluster, ks: Keyspace, nodeId: String, keys:List[String],column_range:Option[(Int, Int)] = None ) = {

    val multigetSliceQuery = HFactory.createMultigetSliceQuery(ks, ss, ls, ss)
    multigetSliceQuery.setColumnFamily(nodeId)
    multigetSliceQuery.setKeys(keys)
    if(column_range.isDefined)
      multigetSliceQuery.setRange(column_range.get._1,column_range.get._2 , false, 3)
    else
      multigetSliceQuery.setRange(null, null, false, 3)

    val result = multigetSliceQuery.execute()
    val rows = result.get()

    Nil
  }

  def dateRange(prefix:String,fromDay: String, toDay: String,separator:String=SEPARATOR) = {
    var from = format.parseDateTime(fromDay)
    val to = format.parseDateTime(toDay)
    var to_return = List[String]()
    while (from.isBefore(to)) {
      to_return ::= new StringBuilder(prefix).append(separator).append(format.print(from)).toString()
      from = from.plusDays(1)
    }
    to_return
  }

  def main(args: Array[String]) {
    val c = getClusterFor("sensordb-cluster", "localhost:9160")
    val keyspace = HFactory.createKeyspace(keyspace_name, c)
    //    addSensorData(c,keyspace,"node5",Map("temp1"->Map("1"->"1.1","20"->"20.20","10"->"10.10"),"temp2"->Map("1"->"1.1","20"->"20.20","10"->"10.10")))
    getAllSensorData(c, keyspace, "node5", dateRange("temperature","20081", "200891")).foreach(println);
    //    println("1")
    c.getConnectionManager().shutdown()
  }
}
