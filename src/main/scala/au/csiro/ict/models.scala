package au.csiro.ict

import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.Imports._


//case class Analysis(var name:String,
//                    var user_id:String,
//                    var description:Option[String],
//                    var updated_at:Long = System.currentTimeMillis(),
//                    val created_at:Long = System.currentTimeMillis(),
//                    @Key("_id") val id:ObjectId = new ObjectId)
object Analysis {
  val collection = MongoConnection()("sensordb")("analysis")
}

//case class WidgetInstance(var name:String,
//                          var analysis_id:String,
//                          var user_id:String,
//                          var widget_id:String,
//                          var config:String,
//                          var description:Option[String],
//                          var updated_at:Long = System.currentTimeMillis(),
//                          var placement_order:Int,
//                          val created_at:Long = System.currentTimeMillis(),
//                          @Key("_id") val id:ObjectId = new ObjectId)
object WidgetInstance {
  val collection = MongoConnection()("sensordb")("widgetinstances")
}

//case class Measurements(var name:String,
//                        var website:Option[String],
//                        var description:Option[String],
//                        var updated_at:Long = System.currentTimeMillis(),
//                        val created_at:Long = System.currentTimeMillis(),
//                        @Key("_id") val id:ObjectId = new ObjectId)

//case class Widget(var name:String,
//                  var sample_config:String,
//                  var description:Option[String],
//                  var website:Option[String],
//                  var updated_at:Long = System.currentTimeMillis(),
//                  val created_at:Long = System.currentTimeMillis(),
//                  @Key("_id") val id:ObjectId = new ObjectId)

object Widget {
  val collection = MongoConnection()("sensordb")("widgets")
}

//case class UserWidgets( val user_id:String,
//                        val widget_id:String,
//                        var updated_at:Long = System.currentTimeMillis(),
//                        val created_at:Long = System.currentTimeMillis(),
//                        @Key("_id") val id:ObjectId = new ObjectId)
object UserWidgets {
  val collection = MongoConnection()("sensordb")("userwidgets")
}


