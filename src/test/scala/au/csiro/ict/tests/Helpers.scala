package au.csiro.ict.tests

import au.csiro.ict.Utils
import org.joda.time.{DateTimeZone, DateTime}

object SDBTestHelpers{

  def ukDateTimeToInt(s:String):Int=(Utils.ukDateTimeFormat.parseDateTime(s).getMillis/1000L).asInstanceOf[Int]
  def time2Int(x:String) = (Utils.yyyyDDDFormat.withZone(Utils.TZ_Sydney).parseDateTime(x).getMillis/1000L).asInstanceOf[Int]
  def time2DateTime(x:String) = new DateTime(Utils.yyyyDDDFormat.withZone(Utils.TZ_Sydney).parseDateTime(x).getMillis)


}