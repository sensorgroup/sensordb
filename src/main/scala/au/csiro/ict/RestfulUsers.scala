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
        Users.findOne(Map("name"->name)).filter(r=> BCrypt.checkpw(password, r.getAs[String]("password").get)).map{ user=>
          val session_id = Utils.uuid()
          session.setAttribute (SESSION_ID,session_id)
          cache.hset(session_id,CACHE_UID, user._id.get.toString)
          cache.hset(session_id,CACHE_USER_NAME, user.getAs[String]("name").get)
          cache.expire(session_id,CACHE_TIMEOUT)
          user._id.get
        }.orElse{
          haltMsg("Login failed")
          None
        }
    }
  }
  def logout()={
    val ident=session.getAttribute(SESSION_ID)
    if (ident !=null) cache.del(ident)
    session.invalidate()
    sendSession()
  }

  get("/session"){
    sendSession()
  }
  get("/users"){
    generate(Users.find(MongoDBObject(),MongoDBObject("picture"->"1","description"->"1","name"->"1","website"->"1")))
  }
  post("/register") {
    logger.info("User registering with username:"+params.get("name")+" and email:"+params.get("email"))
    (UniqueUsername(Username(params.get("name"))),
      Password(params.get("password")),
      UniqueEmail(Email(params.get("email"))),
      Description(params.get("description")),
      PictureUrl(params.get("picture")),
      WebUrl(params.get("website"))) match {
      case (Some(name),Some(password),Some(email),Some(description),Some(pic),Some(website))=>
        addUser(name, password, email, pic, website, description)
        login(name,password)
        sendSession()
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
