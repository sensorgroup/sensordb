package au.csiro.ict

import org.mindrot.jbcrypt.BCrypt
import org.bson.types.ObjectId
import javax.servlet.http.HttpSession
import org.scalatra.ScalatraServlet
import au.csiro.ict.Validators._
import au.csiro.ict.Cache._
import com.mongodb.casbah.Imports._
import au.csiro.ict.JsonGenerator.generate

trait RestfulUsers {
  self:ScalatraServlet with RestfulHelpers with Logger=>

  def login(name:String, password:String)(implicit session:HttpSession):Option[ObjectId]= {
    UserSession(session) match{
      case Some((uid,userName))=> Some(uid)
      case None=>
        Users.findOne(Map("name"->name,"active"->true)).filter(r=> BCrypt.checkpw(password, r.getAs[String]("password").get)).map{ user=>
          val session_id = Utils.uuid()
          session.setAttribute (SESSION_ID,session_id)
          sessions.hset(session_id,CACHE_UID, user._id.get.toString)
          sessions.hset(session_id,CACHE_USER_NAME, user.getAs[String]("name").get)
          sessions.expire(session_id,CACHE_TIMEOUT)
          user._id.get
        }.orElse{
          haltMsg("Login failed")
          None
        }
    }
  }
  def logout()={
    val ident=session.getAttribute(SESSION_ID)
    if (ident !=null) sessions.del(ident)
    session.invalidate()
    sendSession()
  }

  get("/session"){
    sendSession()
  }
  get("/users"){
    generate(Users.find(MongoDBObject("active"->true),MongoDBObject("picture"->"1","description"->"1","name"->"1","website"->"1")))
  }

  post("/logout"){
    logout()
  }

  post("/login"){
    (Username(params.get("name")),Password(params.get("password"))) match {
      case (Some(user),Some(password))=>
        logger.info("User login request with username:"+user)
        login(user,password)
        sendSession()
      case errors=>  haltMsg()
    }
  }

  post("/remove"){
    (Username(params.get("name")),Password(params.get("password"))) match {
      case (Some(user),Some(password))=>
        Users.findOne(Map("name"->user)).filter(r=> BCrypt.checkpw(password, r.getAs[String]("password").get)).foreach{u=>
          delUser(u._id.get)
          logout()
        }
      case errors=> haltMsg()
    }

  }

}
