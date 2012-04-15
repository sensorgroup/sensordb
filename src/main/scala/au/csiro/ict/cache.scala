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

  val Experiments = MongoConnection()("sensordb")("experiments")

  val Nodes = MongoConnection()("sensordb")("nodes")

  val Users = MongoConnection()("sensordb")("users")

  val Streams = MongoConnection()("sensordb")("streams")

  val Measurements = MongoConnection()("sensordb")("measurements")

  def addExperiment(name: String, uid: ObjectId, timezone: String, public_access: String, picture: String, website: String, description: String) {
    Experiments.insert(Map("name" -> name, "uid" -> uid, "timezone" -> timezone, "access_restriction" -> public_access,
      "picture" -> picture, "website" -> website, "token" -> Utils.uuid(),
      "updated_at" -> System.currentTimeMillis(),
      "created_at" -> System.currentTimeMillis(),
      "description" -> description))
  }
  def addUser(name: String, password: String, timezone: String, email: String, pic: String, website: String, description: String) {
    val user = Map("name" -> name,
      "token" -> Utils.uuid(),
      "password" -> BCrypt.hashpw(password, BCrypt.gensalt()),
      "timezone" -> timezone,
      "email" -> email,
      "picture" -> pic,
      "website" -> website,
      "description" -> description,
      "created_at" -> System.currentTimeMillis(),
      "updated_at" -> System.currentTimeMillis())
    Users.insert(user)
  }

  def addStream(name: String, uid: ObjectId, nid: ObjectId, mid: ObjectId, picture: String, website: String, description: String) {
    Streams.insert(Map("name" -> name, "uid" -> uid, "nid" -> nid, "mid" -> mid,
      "picture" -> picture, "website" -> website, "token" -> Utils.uuid(),
      "updated_at" -> System.currentTimeMillis(),
      "created_at" -> System.currentTimeMillis(),
      "description" -> description))
  }

  def addNode(name: String, uid: ObjectId, eid: ObjectId, lat: String, lon: String, alt: String, picture: String, website: String, description: String) {
    Nodes.insert(Map("name" -> name, "uid" -> uid, "eid" -> eid, "lat" -> lat, "lon" -> lon, "alt" -> alt,
      "picture" -> picture, "website" -> website, "token" -> Utils.uuid(),
      "updated_at" -> System.currentTimeMillis(),
      "created_at" -> System.currentTimeMillis(),
      "description" -> description))
  }
  def delUser(user: String) {
    Users.remove(Map("name" -> user))
  }

  def delExperiment(uid: ObjectId, expId: ObjectId) {
    Experiments.remove(Map("uid" -> uid, "_id" -> expId))
  }

  def delStream(uid: ObjectId, sid: ObjectId) {
    Streams.remove(Map("uid" -> uid, "_id" -> sid))
  }

  def delNode(uid: ObjectId, nid: ObjectId) {
    Nodes.remove(Map("uid" -> uid, "_id" -> nid))
  }

}
