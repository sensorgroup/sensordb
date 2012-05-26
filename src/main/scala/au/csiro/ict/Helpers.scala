package au.csiro.ict

import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTimeZone, DateTime}

object Utils {
  val TIMESTAMP_YYYYD_FORMAT = DateTimeFormat.forPattern("yyyyD")
  val yyyyFormat = DateTimeFormat.forPattern("yyyy");
  val yyyyWWFormat = DateTimeFormat.forPattern("yyyyww");
  val yyyyMMFormat = DateTimeFormat.forPattern("yyyyMM");
  val yyyyDDDFormat = DateTimeFormat.forPattern("yyyyDDD");
  val yyyyDDDHHFormat = DateTimeFormat.forPattern("yyyyDDDHH");
  val yyyyDDDHHMMFormat = DateTimeFormat.forPattern("yyyyDDDHHmm");
  val ukDateTimeFormat = DateTimeFormat.forPattern("dd-MM-yyyy'T'HH:mm:ss")
  val ukDateFormat = DateTimeFormat.forPattern("dd-MM-yyyy")
  val TimeParser = DateTimeFormat.forPattern("HH:mm:ss")
  val zoneUTC = DateTimeZone.UTC
  val SEPARATOR = '$'
  val TZ_Sydney = DateTimeZone.forID("Australia/Sydney")
  val TZ_Sydney_ID = "Australia/Sydney"
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