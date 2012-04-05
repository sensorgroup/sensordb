package au.csiro.ict

import org.mindrot.jbcrypt.BCrypt
import com.redis._
import org.bson.types.ObjectId
import javax.servlet.http.HttpSession
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._

object Sessions {

  val SESSION_ID = "sdb"
  val CACHE_USER_ID="uid"
  val CACHE_USER_NAME="user"
  val CACHE_TIMEOUT=15*60 // in seconds
  val CACHE_DB = 1
  val QUEUE_DB = 2
  val cache = new RedisClient(Configuration("redis.cache.host").get, Configuration("redis.cache.port").get.toInt)
  val queue = new RedisClient(Configuration("redis.queue.host").get, Configuration("redis.queue.port").get.toInt)
  cache.select(CACHE_DB)
  queue.select(QUEUE_DB)

  /**
   *
   * @param session
   * @return (User_ID, User_Name)
   */
  def userSession(implicit session:HttpSession):Option[(String,String)]={
    val sessionId=session.getAttribute(SESSION_ID)
    if(sessionId !=null) {
      cache.expire(sessionId,CACHE_TIMEOUT)
      Some(cache.hget(sessionId,CACHE_USER_ID).get->cache.hget(sessionId,CACHE_USER_NAME).get)
    } else None
  }

  def login(user:String, password:String)(implicit session:HttpSession):Option[DBObject]= {
    userSession.flatMap(x=>User.collection.findOne(Map("_id"->x._1))).orElse{
      User.collection.findOne(Map("name"->user)).filter(r=> BCrypt.checkpw(password, r.getAs[String]("password").get)).map{ u=>
        val session_id = Utils.uuid()
        session.setAttribute (SESSION_ID,session_id)
        cache.hset(session_id,CACHE_USER_ID,u._id.get.toString)
        cache.hset(session_id,CACHE_USER_NAME,u.getAs[String]("name").get)
        cache.expire(session_id,CACHE_TIMEOUT)
        u
      }
    }
  }

  def logout(implicit session:HttpSession)={
    val ident=session.getAttribute(SESSION_ID)
    if (ident !=null) cache.del(ident)
    session.invalidate()
  }


}
