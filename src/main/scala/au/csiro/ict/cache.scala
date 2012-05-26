package au.csiro.ict

import org.mindrot.jbcrypt.BCrypt
import com.redis._
import org.bson.types.ObjectId
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
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

  val EXPERIMENT_ACCESS_PUBLIC=0

  val EXPERIMENT_ACCESS_FRIENDS=1

  val EXPERIMENT_ACCESS_PRIVATE=2

  val ACCESS_RESTRICTION_FIELD = "access_restriction"

  val MongoDB = MongoConnection(SensorDBConf.getString("structural-store.mongodb.host"),SensorDBConf.getInt("structural-store.mongodb.port"))("sensordb")

  val Experiments = MongoDB("experiments")

  val Nodes = MongoDB("nodes")

  val Users = MongoDB("users")

  val Streams = MongoDB("streams")

  val Measurements = MongoDB("measurements")

  def addExperiment(name: String, uid: ObjectId, timezone: String, public_access: Int, picture: String, website: String, description: String):Option[ObjectId] ={
    val toInsert = MongoDBObject("name" -> name,
      "uid" -> uid,
      "timezone" -> timezone,
      ACCESS_RESTRICTION_FIELD -> public_access,
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

  /**
   * Finds UserId and NodeId for a given StreamId
   * @param sid StreamId
   * @return, the first element of the pair is userId while the second element in the pair is nodeId.
   */
  def NidUidFromSid(sid: ObjectId):Option[(ObjectId,ObjectId)] = Streams.findOne(MongoDBObject("_id" -> sid), MongoDBObject("nid" -> 1, "uid" -> 1)).map((x)=>x.getAs[ObjectId]("uid").get->x.getAs[ObjectId]("nid").get)

  def experimentTimeZoneFromNid(nid:ObjectId):Option[(ObjectId,Int,DateTimeZone)] =
    Nodes.findOneByID(nid,MongoDBObject("eid"->1)).flatMap(_.getAs[ObjectId]("eid") match {
      case Some(eid:ObjectId)=>
        Experiments.findOneByID(eid,MongoDBObject("timezone"->1,ACCESS_RESTRICTION_FIELD->1)).map((exp)=> (eid,exp.getAs[Int](ACCESS_RESTRICTION_FIELD).get, DateTimeZone.forID(exp.getAs[String]("timezone").get)))
      case others =>
        println("====>"+others)
        None
    })

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
    Nodes
    toInsert._id
  }
  def delUser(uId: ObjectId) {
    Experiments.find(MongoDBObject("uid"->uId)).foreach((x)=>delExperiment(uId,x.getAs[ObjectId]("_id").get))
    Users.remove(MongoDBObject("_id" -> uId))
  }

  def delExperiment(uId: ObjectId, expId: ObjectId) {
    Nodes.find(MongoDBObject("uid"->uId,"eid"->expId),MongoDBObject("_id"->1)).map(_.getAs[ObjectId]("_id").get).foreach{nId=> delNode(uId,nId) }
    Experiments.remove(MongoDBObject("uid" -> uId, "_id" -> expId))
  }

  def delNode(uId: ObjectId, nId: ObjectId) {
    Streams.find(MongoDBObject("uid"->uId,"nid"->nId),MongoDBObject("_id"->1)).map(_.getAs[ObjectId]("_id").get).foreach{sid=>
      delStream(uId,sid)
    }
    Nodes.remove(Map("uid" -> uId, "_id" -> nId))
  }

  def delStream(uId: ObjectId, sid: ObjectId) {
    Streams.findAndRemove(MongoDBObject("uid" -> uId, "_id" -> sid)).map(_.getAs[ObjectId]("_id").get).foreach{sid=>
      store.drop(sid.toString)
    }
  }

}
