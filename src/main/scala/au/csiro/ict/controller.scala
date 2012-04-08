package au.csiro.ict

import org.scalatra._
import scalate.ScalateSupport
import grizzled.slf4j.Logger
import org.scalatra.fileupload.FileUploadSupport
import com.codahale.jerkson.Json._
import io.Source
import java.util.Properties
import java.io.FileReader
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import org.apache.commons.fileupload.servlet.ServletFileUpload
import scala.{None, Option}
import org.mindrot.jbcrypt.BCrypt
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import au.csiro.ict.Validators._
import org.bson.types.ObjectId
import javax.servlet.http.HttpSession


object Configuration{
  private val config = new Properties()
  config.load(getClass.getResourceAsStream("/config.properties"))
  val config_map = config.entrySet().map(x=>(x.getKey.toString.trim().toLowerCase,x.getValue.toString.trim())).toMap[String, String]
  def apply(name:String):Option[String]= config_map.get(name.toLowerCase.trim())
}

class Controller extends ScalatraServlet with ScalateSupport with FileUploadSupport with FlashMapSupport {

  def forward(path:String="/session")=servletContext.getRequestDispatcher(path).forward(request, response)

  def DBObjectToJSON(ob:Option[DBObject]):String = generate(ob.map(x=>x.filter(_._2 != null).mapValues(z=>z.toString)))

  private val logger: Logger = Logger[this.type]

  val protectedFields = Map("token"->0,"password"->0,"email"->0)

  implicit def errors:Validator = request.getAttribute("__errors") match {
    case v:Validator =>v
    case others =>
      val validator = new Validator()
      request.setAttribute("__errors",validator)
      validator
  }

  def haltMsg(newErrorMessage:String=null) = {
    if(newErrorMessage!=null)
      errors.addError(newErrorMessage)
    if (!errors.errors.isEmpty)
      halt(400,generate(Map("errors"->errors.errors)))
  }

  def login(name:String, password:String)(implicit session:HttpSession):Option[DBObject]= {
    UserSession(session) match{
      case Some((userId,userName))=>User.collection.findOne(Map("_id"->userId))
      case None=>
        User.collection.findOne(Map("name"->name)).filter(r=> BCrypt.checkpw(password, r.getAs[String]("password").get)).map{ user=>
          val session_id = Utils.uuid()
          session.setAttribute (SESSION_ID,session_id)
          Cache.cache.hset(session_id,Cache.CACHE_USER_ID, user._id.get.toString)
          Cache.cache.hset(session_id,Cache.CACHE_USER_NAME, user.getAs[String]("name").get)
          Cache.cache.expire(session_id,Cache.CACHE_TIMEOUT)
          user
        }
    }
  }
  def logout()={
    val ident=session.getAttribute(SESSION_ID)
    if (ident !=null) Cache.cache.del(ident)
    session.invalidate()
    forward()
  }

  post("/session"){
    val current:Option[ObjectId] = UserSession(session).map(_._1)
    val user:Option[ObjectId] = params.get("user").filterNot(_.trim.isEmpty).flatMap{uname=>
      User.collection.findOne(Map("name"->uname),Map("_id"->1))
    }.flatMap(x=>x.getAs[ObjectId]("_id"))
    val fields = if (current.exists(x=>user.isEmpty || x.equals(user.get)))
      Map("password"->0)
    else
      protectedFields
    user.orElse(current).flatMap((u:ObjectId)=>User.collection.findOne(Map("_id"->u))).map{user=>
      val uid = user._id.get
      user.put("_id",uid.toString)
      generate(Map("user"->user,
        "experiments"->Experiment.collection.find(Map("user_id"->uid),fields),
        "nodes"->Node.collection.find(Map("user_id"->uid),fields),
        "streams"->Stream.collection.find(Map("user_id"->uid),fields)))
    }.getOrElse("{}")
  }

  post("/register") {
    logger.info("User registering with username:"+params.get("name")+" and email:"+params.get("email"))
    (UniqueUsername(Username(params.get("name"))),Password(params.get("password")),TimeZone(params.get("timezone")),Email(params.get("email")),Description(params.get("description")),PictureUrl(params.get("picture")),WebUrl(params.get("website"))) match {
      case (Some(name),Some(password),Some(timezone),Some(email),Some(description),Some(pic),Some(website))=>
        val user = Map("name"->name,"token"->Utils.uuid(),"password"->BCrypt.hashpw(password, BCrypt.gensalt()),"timezone"->timezone,"email"->email,"picture"->pic,"website"->website,"description"->description)
        User.collection.insert(user)
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
        User.collection.findOne(Map("name"->user)).filter(r=> BCrypt.checkpw(password, r.getAs[String]("password").get)).foreach{u=>
          User.collection.remove(Map("name"->user))
          logout()
        }
      case errors=> haltMsg()
    }

  }

  delete("/experiments"){
    //TODO: Test cascading deletes
    (UserSession(session),EntityId(params.get("id"))) match{
      case (Some((userId,userName)),Some(expId))=>
        Experiment.collection.remove(Map("user_id"->userId,"_id"->expId))
        val failed=Experiment.collection.findOne(Map("user_id"->userId,"_id"->expId)).isDefined
        if(failed)
          haltMsg("Delete Failed")
        else
          halt(200,"Delete succeeded")
      case errors=>haltMsg()
    }
  }
  //
  //  //todo: test that experiment information with private flag is not available through session
  post("/experiments"){
    // Add a new experiments
    (UserSession(session),Name(params.get("name")),Description(params.get("description")),TimeZone(params.get("timezone")),WebUrl(params.get("website")),PictureUrl(params.get("picture")),Privacy(params.get("public_access"))) match{
      case (Some((user_id,user_name)),Some(name),Some(description),Some(timezone),Some(website),Some(picture),Some(public_access)) if UniqueExperiment(user_id,name)=>
        Experiment.collection.insert(Map("name"->name,"user_id"->user_id,"timezone"->timezone,"access_restriction"-> public_access,
          "picture"->picture,"website"->website, "token"->Utils.uuid(),
          "updated_at"->System.currentTimeMillis(),
          "created_at"->System.currentTimeMillis(),
          "description"->description))

        DBObjectToJSON(Experiment.collection.findOne(Map("user_id"->user_id,"name"->name)))
      case error=>haltMsg()
    }
  }
  put("/experiments"){
    // update/replace an experiment information
    val validators:Map[String,()=>Option[String]] = Map("website"-> (()=>WebUrl(params.get("field"))),
      "name"-> (()=> Name(params.get("field"))),
      "timezone"-> (()=> TimeZone(params.get("field"))),
      "description"-> (()=> Description(params.get("field"))),
      "picture"-> (()=> PictureUrl(params.get("field"))),
      "access_restriction"-> (()=> Privacy(params.get("field"))))

    (UserSession(session),EntityId(params.get("eid")),params.get("field").filter(validators.keys.contains),params.get("value")) match {
      case (Some((userId,userName)),Some(eid),Some(field),Some(value)) if validators(field).apply().isDefined && (field.equals("name") && UniqueExperiment(userId,value,eid)) =>
        Experiment.collection.findAndModify(Map("user_id"->userId,"_id"->eid),$set(field->value))
        DBObjectToJSON(Experiment.collection.findOne(Map("user_id"->userId,"_id"->eid)))
      case errors => haltMsg()
    }
  }
  get("/nodes"){
    // List the nodes

  }
  delete("/nodes"){
    (UserSession(session),EntityId(params.get("sid")),EntityId(params.get("eid"))) match {
      case (Some((userId,userName)),Some(id),Some(eid))=>
      case errors => haltMsg()
    }
  }
  //
  put("/nodes"){
    // update/replace an nodes information
  }

  post("/nodes"){
    // Add a new nodes

  }
  get("/nodes"){
    // List the nodes

  }
  get("/nodes"){
    // List the nodes

  }
  delete("/streams"){
    (UserSession(session),EntityId(params.get("sid")),EntityId(params.get("eid")),EntityId(params.get("nid"))) match {
      case (Some((userId,userName)),Some(sid),Some(eid),Some(nid))=>
      case errors => haltMsg()
    }
  }

  put("/streams"){
    // update/replace an streams information
  }

  post("/streams"){
    // Add a new streams

  }
  get("/streams"){
    // List the streams

  }
  notFound {
    findTemplate(requestPath) map {
      path =>
        contentType = "text/html"
        layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }
}
