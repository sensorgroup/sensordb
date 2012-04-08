package au.csiro.ict

import com.mongodb.casbah.MongoConnection
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.mongodb.casbah.Imports._

//case class User(var name:String,
//                var password:String,
//                var timezone:Int,
//                var email:String,
//                var picture:Option[String]=None,
//                var website:Option[String]=None,
//                var description:Option[String]=None,
//                var token:String=Utils.uuid(),
//                var updated_at:Long = System.currentTimeMillis() ,
//                val created_at:Long = System.currentTimeMillis(),
//                @Key("_id") val id:ObjectId = new ObjectId)
object User {
  val collection = MongoConnection()("sensordb")("users")
  def findByName(name:String,fields:Map[String,Int]=Map()):Option[DBObject]=collection.findOne(Map("name"->name),fields)
  def dropByName(name:String)=collection.remove(Map("name"->name))
  def save(t:MongoDBObject)={
    t.put("updated_at",System.currentTimeMillis())
    collection.save(t)
  }
}
//case class Node(var name:String,
//                val experiment_id:String,
//                val user_id:String,
//                var latitude:Option[Double]=None,
//                var longitude:Option[Double]=None,
//                var altitude:Option[Double]=None,
//                var picture:Option[String]=None,
//                var website:Option[String]=None,
//                var description:Option[String]=None,
//                var token:String=Utils.uuid(),
//                var updated_at:Long = System.currentTimeMillis(),
//                val created_at:Long = System.currentTimeMillis(),
//                @Key("_id") val id:ObjectId = new ObjectId)

//case class Stream(var name:String,
//                  var measurement_id: String,
//                  var node_id:String,
//                  var user_id:String,
//                  var picture:Option[String],
//                  var website:Option[String],
//                  var description:Option[String],
//                  var token:String=Utils.uuid(),
//                  var updated_at:Long = System.currentTimeMillis(),
//                  val created_at:Long = System.currentTimeMillis(),
//                  @Key("_id") val id:ObjectId = new ObjectId)

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
object Measurements {
  val collection = MongoConnection()("sensordb")("measurements")
}

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


