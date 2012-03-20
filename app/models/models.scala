package models

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection
import sensordb.Utils

object AccessRestriction extends Enumeration("Public","Friends","Private") {
  val PUBLIC, FRIENDS, PRIVATE = Value
}

case class User(var name:String,
                var password:String,
                var timezone:Int,
                var picture:Option[String]=None,
                var website:Option[String]=None,
                var description:Option[String]=None,
                var token:String=Utils.uuid(),
                var updated_at:Long = System.currentTimeMillis() ,
                val created_at:Long = System.currentTimeMillis(),
                @Key("_id") val id:ObjectId = new ObjectId)
object User extends SalatDAO[User, ObjectId](collection = MongoConnection()("sensordb")("users")){
  def findByName(name:String):Option[User]=this.findOne(MongoDBObject("name"->name))
  def dropByName(name:String)=remove(MongoDBObject("name"->name))
  override def save(t:User)={
    t.updated_at=System.currentTimeMillis()
    super.save(t)
  }
}

case class Experiment(var name:String,
                      var user_id:String,
                      var timezone:Int,
                      var access_restriction:AccessRestriction.Value=AccessRestriction.PUBLIC,
                      var picture:Option[String]=None,
                      var website:Option[String]=None,
                      var description:Option[String]=None,
                      var token:String=Utils.uuid(),
                      var updated_at:Long = System.currentTimeMillis(),
                      val created_at:Long = System.currentTimeMillis(),
                      @Key("_id") val id:ObjectId = new ObjectId)
object Experiment extends SalatDAO[Experiment, ObjectId](collection = MongoConnection()("sensordb")("experiments"))

case class Node(var name:String,
                val experiment_id:String,
                val user_id:String,
                var latitude:Option[Double]=None,
                var longitude:Option[Double]=None,
                var altitude:Option[Double]=None,
                var picture:Option[String]=None,
                var website:Option[String]=None,
                var description:Option[String]=None,
                var token:String=Utils.uuid(),
                var updated_at:Long = System.currentTimeMillis(),
                val created_at:Long = System.currentTimeMillis(),
                @Key("_id") val id:ObjectId = new ObjectId)
object Node extends SalatDAO[Node, ObjectId](collection = MongoConnection()("sensordb")("nodes"))

case class Stream(var name:String,
                  var measurement_id: String,
                  var node_id:String,
                  var user_id:String,
                  var picture:Option[String],
                  var website:Option[String],
                  var description:Option[String],
                  var token:String=Utils.uuid(),
                  var updated_at:Long = System.currentTimeMillis(),
                  val created_at:Long = System.currentTimeMillis(),
                  @Key("_id") val id:ObjectId = new ObjectId)
object Stream extends SalatDAO[Stream, ObjectId](collection = MongoConnection()("sensordb")("streams"))

case class Analysis(var name:String,
                    var user_id:String,
                    var description:Option[String],
                    var updated_at:Long = System.currentTimeMillis(),
                    val created_at:Long = System.currentTimeMillis(),
                    @Key("_id") val id:ObjectId = new ObjectId)
object Analysis extends SalatDAO[Analysis, ObjectId](collection = MongoConnection()("sensordb")("analysis"))

case class WidgetInstance(var name:String,
                          var analysis_id:String,
                          var user_id:String,
                          var widget_id:String,
                          var config:String,
                          var description:Option[String],
                          var updated_at:Long = System.currentTimeMillis(),
                          var placement_order:Int,
                          val created_at:Long = System.currentTimeMillis(),
                          @Key("_id") val id:ObjectId = new ObjectId)
object WidgetInstance extends SalatDAO[WidgetInstance, ObjectId](collection = MongoConnection()("sensordb")("widgetinstances"))

case class Measurements(var name:String,
                        var website:Option[String],
                        var description:Option[String],
                        var updated_at:Long = System.currentTimeMillis(),
                        val created_at:Long = System.currentTimeMillis(),
                        @Key("_id") val id:ObjectId = new ObjectId)
object Measurements extends SalatDAO[Measurements, ObjectId](collection = MongoConnection()("sensordb")("measurements"))

case class Widget(var name:String,
                  var sample_config:String,
                  var description:Option[String],
                  var website:Option[String],
                  var updated_at:Long = System.currentTimeMillis(),
                  val created_at:Long = System.currentTimeMillis(),
                  @Key("_id") val id:ObjectId = new ObjectId)
object Widget extends SalatDAO[Widget, ObjectId](collection = MongoConnection()("sensordb")("widgets"))

case class UserWidgets( val user_id:String,
                        val widget_id:String, 
                        var updated_at:Long = System.currentTimeMillis(),
                        val created_at:Long = System.currentTimeMillis(),
                        @Key("_id") val id:ObjectId = new ObjectId)
object UserWidgets extends SalatDAO[UserWidgets, ObjectId](collection = MongoConnection()("sensordb")("userwidgets"))


