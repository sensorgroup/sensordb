package sensordb

import org.mindrot.jbcrypt.BCrypt
import redis.clients.jedis.Jedis
import org.apache.commons.validator.GenericValidator._
import models.User

object SessionMan {

//  val SESSION_ID = "sdb"
//  val CACHE_TIMEOUT=15*60 // in seconds
//  val CACHE_DB = 1
//  val QUEUE_DB = 2
//  val cache = new Jedis(Play.configuration.getString("redis.cache.host").get,Play.configuration.getInt("redis.cache.port").get)
//  cache.select(CACHE_DB)
//  val queue = new Jedis(Play.configuration.getString("redis.queue.host").get,Play.configuration.getInt("redis.cache.port").get)
//  queue.select(QUEUE_DB)
//
//  class SDBRequest(implicit session:Session){
//    val session_id:Option[String] = session(SESSION_ID) match{
//      case token:String if(!isBlankOrNull(token) && cache.exists(token)) =>
//        cache.expire(token,CACHE_TIMEOUT)
//        Some(token)
//      case others => None
//    }
//
//    def authenticate(user:String, password:String):Option[String]=
//      if(session_id.isEmpty)
//        User.findByName(user).headOption.filter(r=> BCrypt.checkpw(password, r.password)).map(_.id.toString) match {
//          case Some(uid:String)=>
//            val session_id = Utils.uuid()
//            session + SESSION_ID-> session_id
//            cache.hset(session_id,"uid",uid)
//            cache.expire(session_id,CACHE_TIMEOUT)
//            Some(uid)
//          case _ => None // Failed login
//        }
//      else
//        None
//
//    def addUser(username:String, password:String,picture_path:String, description:String, website:String, timezone:Int):Option[String] = {
//      val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
//      // insert into db
//      authenticate(username,hashedPassword)
//    }
//  }
}
