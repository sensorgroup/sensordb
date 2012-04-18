package au.csiro.ict

import org.mindrot.jbcrypt.BCrypt
import com.redis._
import org.bson.types.ObjectId
import javax.servlet.http.HttpSession
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import au.csiro.ict.Validators.Validator
import java.util.Properties

object Configuration{
  private val config = new Properties()
  config.load(getClass.getResourceAsStream("/config.properties"))
  val config_map = config.entrySet().map(x=>(x.getKey.toString.trim().toLowerCase,x.getValue.toString.trim())).toMap[String, String]
  def apply(name:String):Option[String]= config_map.get(name.toLowerCase.trim())
}

object Cache {

  val store:SensorDataStore = new CassandraDataStore()

  val CACHE_UID="uid"
  val CACHE_USER_NAME="user"
  val CACHE_TIMEOUT=15*60 // in seconds
  val CACHE_DB = 1
  val QUEUE_DB = 2
  val cache = new RedisClient(Configuration("redis.cache.host").get, Configuration("redis.cache.port").get.toInt)
  val queue = new RedisClient(Configuration("redis.queue.host").get, Configuration("redis.queue.port").get.toInt)
  cache.select(CACHE_DB)
  queue.select(QUEUE_DB)

  val EXPERIMENT_ACCESS_PUBLIC=0

  val EXPERIMENT_ACCESS_PRIVATE=1

  val EXPERIMENT_ACCESS_FRIENDS=2

  val ACCESS_RESTRICTION_FIELD = "access_restriction"

  val Experiments = MongoConnection()("sensordb")("experiments")

  val Nodes = MongoConnection()("sensordb")("nodes")

  val Users = MongoConnection()("sensordb")("users")

  val Streams = MongoConnection()("sensordb")("streams")

  val Measurements = MongoConnection()("sensordb")("measurements")

  def addExperiment(name: String, uid: ObjectId, timezone: String, public_access: String, picture: String, website: String, description: String):Option[ObjectId] ={
    val toInsert = MongoDBObject("name" -> name, "uid" -> uid, "timezone" -> timezone, ACCESS_RESTRICTION_FIELD -> public_access,
      "picture" -> picture, "website" -> website,
      "updated_at" -> System.currentTimeMillis(),
      "created_at" -> System.currentTimeMillis(),
      "description" -> description)
    Experiments.insert(toInsert)
    toInsert._id
  }
  def addUser(name: String, password: String, timezone: String, email: String, pic: String, website: String, description: String):Option[ObjectId]= {
    val user = MongoDBObject("name" -> name,
      "password" -> BCrypt.hashpw(password, BCrypt.gensalt()),
      "timezone" -> timezone,
      "email" -> email,
      "picture" -> pic,
      "website" -> website,
      "description" -> description,
      "created_at" -> System.currentTimeMillis(),
      "updated_at" -> System.currentTimeMillis())
    Users.insert(user)
    user._id
  }

  def addStream(name: String, uid: ObjectId, nid: ObjectId, mid: ObjectId, picture: String, website: String, description: String,tokenOption:Option[String]=None):Option[ObjectId]= {
    val toInsert = MongoDBObject("name" -> name, "uid" -> uid, "nid" -> nid, "mid" -> mid,
      "picture" -> picture, "website" -> website, "token" -> tokenOption.getOrElse(Utils.uuid()),
      "updated_at" -> System.currentTimeMillis(),
      "created_at" -> System.currentTimeMillis(),
      "description" -> description)
    Streams.insert(toInsert)
    toInsert._id
  }

  def addNode(name: String, uid: ObjectId, eid: ObjectId, lat: String, lon: String, alt: String, picture: String, website: String, description: String):Option[ObjectId] ={
    val toInsert = MongoDBObject("name" -> name, "uid" -> uid, "eid" -> eid, "lat" -> lat, "lon" -> lon, "alt" -> alt,
      "picture" -> picture, "website" -> website,
      "updated_at" -> System.currentTimeMillis(),
      "created_at" -> System.currentTimeMillis(),
      "description" -> description)
    Nodes.insert(toInsert)
    toInsert._id
  }
  def delUser(uId: ObjectId) {
    Streams.remove(MongoDBObject("uid"->uId))
    Nodes.remove(MongoDBObject("uid"->uId))
    Experiments.remove(MongoDBObject("uid"->uId))
    Users.remove(MongoDBObject("_id" -> uId))
  }

  def delExperiment(uId: ObjectId, expId: ObjectId) {
    Nodes.find(MongoDBObject("uid"->uId,"eid"->expId),MongoDBObject("_id"->1)).foreach{nId=>
      Streams.remove(MongoDBObject("uid"->uId,"nid"->nId))
    }
    Nodes.remove(MongoDBObject("uid"->uId,"eid"->expId))
    Experiments.remove(MongoDBObject("uid" -> uId, "_id" -> expId))
  }

  def delNode(uId: ObjectId, nId: ObjectId) {
    Streams.remove(MongoDBObject("uid"->uId,"nid"->nId))
    Nodes.remove(Map("uid" -> uId, "_id" -> nId))
  }

  def delStream(uId: ObjectId, sid: ObjectId) {
    Streams.remove(Map("uid" -> uId, "_id" -> sid))
  }

}
