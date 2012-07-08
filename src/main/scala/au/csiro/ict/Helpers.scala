package au.csiro.ict

import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTimeZone, DateTime}

object Utils {
  val TIMESTAMP_YYYYD_FORMAT = DateTimeFormat.forPattern("yyyyD").withZoneUTC()
  val yyyyFormat = DateTimeFormat.forPattern("yyyy").withZoneUTC()
  val yyyyWWFormat = DateTimeFormat.forPattern("yyyyww").withZoneUTC()
  val yyyyMMFormat = DateTimeFormat.forPattern("yyyyMM").withZoneUTC()
  val yyyyDDDFormat = DateTimeFormat.forPattern("yyyyDDD").withZoneUTC()
  val yyyyDDDHHFormat = DateTimeFormat.forPattern("yyyyDDDHH").withZoneUTC()
  val yyyyDDDHHMMFormat = DateTimeFormat.forPattern("yyyyDDDHHmm").withZoneUTC()
  val ukDateTimeFormat = DateTimeFormat.forPattern("dd-MM-yyyy'T'HH:mm:ss").withZoneUTC()
  val ukDateFormat = DateTimeFormat.forPattern("dd-MM-yyyy").withZoneUTC()
  val TimeParser = DateTimeFormat.forPattern("HH:mm:ss").withZoneUTC()
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
  def dateTimeToInt(ts:DateTime):Int=(ts.getMillis/1000L).asInstanceOf[Int]
}