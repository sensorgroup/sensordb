package au.csiro.ict

import collection.mutable.ListBuffer
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTimeZone, DateTime}

class StorageStreamDayIdGenerator(prefix:Seq[String],var from:DateTime,to:DateTime) extends Iterator[String] {
  val separator:Char=Utils.SEPARATOR

  override def hasNext = !buffer.isEmpty || ( !prefix.isEmpty && !from.isAfter(to))

  val buffer = ListBuffer[String]()
  override def next() = {
    if (buffer.isEmpty){
      buffer ++= prefix.map(p=>new StringBuilder(p).append(separator).append(Utils.TIMESTAMP_YYYYD_FORMAT.print(from)).toString())
      from = from.plusDays(1)
    }
    buffer.remove(0)
  }
  def this(prefix:Seq[String],startDate: String, endDate: String)={
    this(prefix,Utils.TIMESTAMP_YYYYD_FORMAT.parseDateTime(startDate),Utils.TIMESTAMP_YYYYD_FORMAT.parseDateTime(endDate))
  }
}

object Utils {
  val TIMESTAMP_YYYYD_FORMAT = DateTimeFormat.forPattern("yyyyD")
  //  val isoDateTimeFormat = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss")
  val yyyyDDDFormat = DateTimeFormat.forPattern("yyyyDDD");
  val ukDateTimeFormat = DateTimeFormat.forPattern("dd-MM-yyyy'T'HH:mm:ss")
  val ukDateFormat = DateTimeFormat.forPattern("dd-MM-yyyy")
  val UkDateFormat = DateTimeFormat.forPattern("dd-MM-yyyy")
  val TimeParser = DateTimeFormat.forPattern("HH:mm:ss")
  val zoneUTC = DateTimeZone.UTC
  val SEPARATOR = '$'
  def uuid() = java.util.UUID.randomUUID().toString
  DateTimeZone.setDefault(zoneUTC)
  def generateRowKey(sensor:String, tsInSeconds:Int) = sensor+"$"+Utils.yyyyDDDFormat.print(tsInSeconds*1000L)
  def parseRowKey(rowKey:String):(String,Int) = {
    val Array(sid,dayInyyyyDDD)=rowKey.split("$")
    sid -> (yyyyDDDFormat.parseDateTime(dayInyyyyDDD).getMillis/1000L).asInstanceOf[Int]
  }
  def generateNidStreamDayKey(nid:String, streamDayKey:String) = nid+"@"+streamDayKey

  val TOKEN_LEN = Utils.uuid().length
  val KeyPattern = ("[a-zA-Z0-9\\-]{"+TOKEN_LEN+"}").r.pattern
  def keyPatternMatcher(s:String) = KeyPattern.matcher(s).matches
  def inputQueueIdFor(nId:String,streamId:String)=  "q@"+nId+"@"+streamId
  def getSecondOfDay(ts:Long):Int=new DateTime(ts*1000).getSecondOfDay
  //  def isoToInt(s:String):Int=(isoDateTimeFormat.parseDateTime(s).getMillis/1000).asInstanceOf[Int]
  def ukDateTimeToInt(s:String):Int=(ukDateTimeFormat.parseDateTime(s).getMillis/1000).asInstanceOf[Int]
}