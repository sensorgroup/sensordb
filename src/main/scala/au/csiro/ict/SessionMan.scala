package au.csiro.ict

import org.mindrot.jbcrypt.BCrypt
import com.redis._
import au.csiro.ict.Configuration._
import au.csiro.ict.{Configuration, Utils, User}
import org.bson.types.ObjectId
import javax.servlet.http.HttpSession

object Sessions {

  val SESSION_ID = "sdb"
  val CACHE_USER_ID="uid"
  val CACHE_TIMEOUT=15*60 // in seconds
  val CACHE_DB = 1
  val QUEUE_DB = 2
  val cache = new RedisClient(Configuration("redis.cache.host").get, Configuration("redis.cache.port").get.toInt)
  val queue = new RedisClient(Configuration("redis.queue.host").get, Configuration("redis.queue.port").get.toInt)
  cache.select(CACHE_DB)
  queue.select(QUEUE_DB)

  def userSession(implicit session:HttpSession):Option[User]={
    val sessionId=session.getAttribute(SESSION_ID)
    val cacheInfo:Option[String] = if(sessionId !=null) cache.hget(sessionId,CACHE_USER_ID) else None
    if(sessionId!=null && cacheInfo.isDefined){
      cache.expire(sessionId,CACHE_TIMEOUT)
      User.findOneByID(new ObjectId(cacheInfo.get))
    }else
      None
  }

  def login(user:String, password:String)(implicit session:HttpSession):Option[User]=
    userSession.orElse{
      User.findByName(user).filter(r=> BCrypt.checkpw(password, r.password)).headOption match {
        case Some(u:User)=>
          val session_id = Utils.uuid()
          session.setAttribute (SESSION_ID,session_id)
          cache.hset(session_id,CACHE_USER_ID,u.id.toString)
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
