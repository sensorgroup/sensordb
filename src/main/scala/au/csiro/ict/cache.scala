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

}
