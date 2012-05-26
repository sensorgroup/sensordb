package au.csiro.ict

import org.mindrot.jbcrypt.BCrypt
import com.redis._
import org.bson.types.ObjectId
import scala.collection.JavaConversions._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import java.util.Properties
import redis.clients.jedis.{Protocol, Jedis, JedisPoolConfig, JedisPool}
import org.joda.time.DateTimeZone
import com.typesafe.config.ConfigFactory

class RedisPool(val host:String,val port:Int,dbIndex:Int){
  private val pool = new JedisPool(new JedisPoolConfig(), host,port,Protocol.DEFAULT_TIMEOUT,null,dbIndex)
  def call[T](x:(Jedis=>T)):T={
    val jedis:Jedis = pool.getResource
    val t:T = x(jedis)
    pool.returnResourceObject(jedis)
    t
  }
}

object Cache {
  private val conf = ConfigFactory.load()
  val SensorDBConf  = conf.getConfig("SensorDB")

  val CACHE_UID="uid"

  val CACHE_USER_NAME="user"
  val CACHE_TIMEOUT=15*60 // in seconds
  val SESSIONS_DB = 1 // User sessions, keeping track of logged in users
  val CACHE_DB = 2 // General Cache
  val STREAM_STAT_TIME_IDX = 3 // storing presence index per stream per day, bit index of 86400 elements (mainly zeros)
  val REDIS_STORE = 4 // used to store raw and aggregated sensor data permanently instead of HBase

  val sessions = new RedisClient(SensorDBConf.getString("session.redis.host"), SensorDBConf.getInt("session.redis.port"))
  sessions.select(SESSIONS_DB)

  val stat_time_idx = new RedisPool(SensorDBConf.getString("bitindex.redis.host"), SensorDBConf.getInt("bitindex.redis.port"),STREAM_STAT_TIME_IDX)
  val caching = new RedisPool(SensorDBConf.getString("cache.redis.host"), SensorDBConf.getInt("cache.redis.port"),CACHE_DB)

  lazy val store:Storage = new RedisStore()

  val EXPERIMENT_ACCESS_PUBLIC="0"

  val EXPERIMENT_ACCESS_PRIVATE="1"

  val EXPERIMENT_ACCESS_FRIENDS="2"

  val ACCESS_RESTRICTION_FIELD = "access_restriction"

  val MongoDB = MongoConnection(SensorDBConf.getString("structural-store.mongodb.host"),SensorDBConf.getInt("structural-store.mongodb.port"))("sensordb")

  val Experiments = MongoDB("experiments")

  val Nodes = MongoDB("nodes")

  val Users = MongoDB("users")

  val Streams = MongoDB("streams")

  val Measurements = MongoDB("measurements")

  def addExperiment(name: String, uid: ObjectId, timezone: String, public_access: String, picture: String, website: String, description: String):Option[ObjectId] ={
    val toInsert = MongoDBObject("name" -> name, "uid" -> uid, "timezone" -> timezone, ACCESS_RESTRICTION_FIELD -> public_access,
      "picture" -> picture, "website" -> website,
      "updated_at" -> System.currentTimeMillis(),
      "created_at" -> System.currentTimeMillis(),
      "description" -> description)
    Experiments.insert(toInsert)
    toInsert._id
  }
  def addUser(name: String, password: String, email: String, pic: String, website: String, description: String,active:Boolean=true):Option[ObjectId]= {
    val user = MongoDBObject("name" -> name,
      "password" -> BCrypt.hashpw(password, BCrypt.gensalt()),
      "email" -> email,
      "picture" -> pic,
      "active" -> active,
      "website" -> website,
      "description" -> description,
      "created_at" -> System.currentTimeMillis(),
      "updated_at" -> System.currentTimeMillis())
    Users.insert(user)
    user._id
  }

  def NidUidFromSid(sid: ObjectId) = Streams.findOne(MongoDBObject("_id" -> sid), MongoDBObject("nid" -> 1, "uid" -> 1))

  def experimentTimeZoneFromNid(nid:ObjectId):DateTimeZone = {
    val eid:ObjectId = Nodes.findOneByID(nid,MongoDBObject("eid"->1)).get.getAs[ObjectId]("eid").get
    val experimentInfo = Experiments.findOneByID(eid,MongoDBObject("timezone"->1))
    val tz:String = experimentInfo.get.getAs[String]("timezone").get
    DateTimeZone.forID(tz)
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
    List(Streams,Nodes,Experiments).foreach(_.remove(MongoDBObject("uid"->uId)))
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
