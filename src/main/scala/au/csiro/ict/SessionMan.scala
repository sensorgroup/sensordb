package au.csiro.ict

import org.mindrot.jbcrypt.BCrypt
import com.redis._
import au.csiro.ict.Configuration._
import au.csiro.ict.{Configuration, Utils, User}
import org.bson.types.ObjectId
import javax.servlet.http.HttpSession
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._

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

  def userSession(implicit session:HttpSession):Option[String]={
    val sessionId=session.getAttribute(SESSION_ID)
    if(sessionId !=null) {
      cache.expire(sessionId,CACHE_TIMEOUT)
      cache.hget(sessionId,CACHE_USER_NAME)
    } else None
  }

  def login(user:String, password:String)(implicit session:HttpSession):Option[User]=
    userSession.flatMap(x=>User.findByName(x)).orElse{
      User.findByName(user).filter(r=> BCrypt.checkpw(password, r.password)).headOption match {
        case Some(u:User)=>
          val session_id = Utils.uuid()
          session.setAttribute (SESSION_ID,session_id)
          cache.hset(session_id,CACHE_USER_ID,u.id.toString)
          cache.hset(session_id,CACHE_USER_NAME,u.name)
          cache.expire(session_id,CACHE_TIMEOUT)
          Some(u)
        case _ => None // Failed login
      }
    }

  def logout(implicit session:HttpSession)={
    val ident=session.getAttribute(SESSION_ID)
    if (ident !=null) cache.del(ident)
    session.invalidate()
  }


}
