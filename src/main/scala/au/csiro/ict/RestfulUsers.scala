package au.csiro.ict

import org.mindrot.jbcrypt.BCrypt
import org.bson.types.ObjectId
import javax.servlet.http.HttpSession
import com.mongodb.DBObject
import org.scalatra.ScalatraServlet
import au.csiro.ict.Validators._
import au.csiro.ict.Cache._
import com.mongodb.casbah.Imports._
import scala.collection.JavaConversions._

trait RestfulUsers {
  self:ScalatraServlet with RestfulHelpers=>

  import SDBSerializer.generate

  val protectedFields = Map("token"->0,"password"->0,"email"->0)

  def login(name:String, password:String)(implicit session:HttpSession):Option[DBObject]= {
    UserSession(session) match{
      case Some((uid,userName))=>Users.findOne(Map("_id"->uid))
      case None=>
        Users.findOne(Map("name"->name)).filter(r=> BCrypt.checkpw(password, r.getAs[String]("password").get)).map{ user=>
          val session_id = Utils.uuid()
          session.setAttribute (SESSION_ID,session_id)
          cache.hset(session_id,CACHE_UID, user._id.get.toString)
          cache.hset(session_id,CACHE_USER_NAME, user.getAs[String]("name").get)
          cache.expire(session_id,CACHE_TIMEOUT)
          user
        }
    }
  }
  def logout()={
    val ident=session.getAttribute(SESSION_ID)
    if (ident !=null) cache.del(ident)
    session.invalidate()
    forward()
  }

  post("/session"){
    val current:Option[ObjectId] = UserSession(session).map(_._1)
    val user:Option[ObjectId] = params.get("user").filterNot(_.trim.isEmpty).flatMap{uname=>
      Users.findOne(Map("name"->uname),Map("_id"->1))
    }.flatMap(x=>x.getAs[ObjectId]("_id"))
    val fields = if (current.exists(x=>user.isEmpty || x.equals(user.get)))
      Map("password"->0)
    else
      protectedFields
    user.orElse(current).flatMap((u:ObjectId)=>Users.findOne(Map("_id"->u),fields)).map{user=>
      val uid = user._id.get
      user.put("_id",uid.toString)
      generate(Map("user"->user,
        "experiments"->Experiments.find(Map("uid"->uid),fields), //.map((o:DBObject)=> o.toMap+("_id"->o("_id").toString)),
        "nodes"->Nodes.find(Map("uid"->uid),fields),
        "streams"->Streams.find(Map("uid"->uid),fields)))
    }.getOrElse("{}")
  }

  post("/register") {
    logger.info("User registering with username:"+params.get("name")+" and email:"+params.get("email"))
    (UniqueUsername(Username(params.get("name"))),Password(params.get("password")),TimeZone(params.get("timezone")),UniqueEmail(Email(params.get("email"))),Description(params.get("description")),PictureUrl(params.get("picture")),WebUrl(params.get("website"))) match {
      case (Some(name),Some(password),Some(timezone),Some(email),Some(description),Some(pic),Some(website))=>
        val user = Map("name"->name,
          "token"->Utils.uuid(),
          "password"->BCrypt.hashpw(password, BCrypt.gensalt()),
          "timezone"->timezone,
          "email"->email,
          "picture"->pic,
          "website"->website,
          "description"->description,
          "created_at"->System.currentTimeMillis(),
          "updated_at"->System.currentTimeMillis())
        Users.insert(user)
        login(name,password)
        forward()
      case errors => haltMsg()
    }
  }
  post("/logout"){
    logout()
  }

  post("/login"){
    (Username(params.get("name")),Password(params.get("password"))) match {
      case (Some(user),Some(password))=>
        logger.info("User login request with username:"+user)
        login(user,password)
        forward()
      case errors=>  haltMsg()
    }
  }
  post("/remove"){
    (Username(params.get("name")),Password(params.get("password"))) match {
      case (Some(user),Some(password))=>
        Users.findOne(Map("name"->user)).filter(r=> BCrypt.checkpw(password, r.getAs[String]("password").get)).foreach{u=>
          Users.remove(Map("name"->user))
          logout()
        }
      case errors=> haltMsg()
    }

  }

}
