package au.csiro.ict

import org.scalatra._
import scalate.ScalateSupport
import grizzled.slf4j.Logger
import org.scalatra.fileupload.FileUploadSupport
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
import Cache._

object SDBSerializer extends com.codahale.jerkson.Json{
  import org.codehaus.jackson.Version
  import org.codehaus.jackson.map.Module
  import org.codehaus.jackson.map.Module.SetupContext
  import org.codehaus.jackson.`type`.JavaType
  import org.codehaus.jackson.map._
  import annotate.JsonCachable
  import org.codehaus.jackson.JsonGenerator

  class ObjectIdModule extends Module{
    def version = new Version(0, 2, 0, "")
    def getModuleName = "sensordb"
    def setupModule(context: SetupContext) {
      context.addSerializers(new ObjectIdSerializer)
    }
  }
  class ObjectIdSerializer extends Serializers.Base {
    override def findSerializer(config: SerializationConfig, javaType: JavaType, beanDesc: BeanDescription, beanProp: BeanProperty) = {
      val ser: Object = if (classOf[ObjectId].isAssignableFrom(beanDesc.getBeanClass)) { new ObjectIdSerlization } else null
      ser.asInstanceOf[JsonSerializer[Object]]
    }
    @JsonCachable
    class ObjectIdSerlization extends JsonSerializer[ObjectId] {
      def serialize(value: ObjectId, json: JsonGenerator, provider: SerializerProvider) {
        json.writeString(value.toString)
      }
    }
  }
  mapper.registerModule(new ObjectIdModule())
}

object Configuration{
  private val config = new Properties()
  config.load(getClass.getResourceAsStream("/config.properties"))
  val config_map = config.entrySet().map(x=>(x.getKey.toString.trim().toLowerCase,x.getValue.toString.trim())).toMap[String, String]
  def apply(name:String):Option[String]= config_map.get(name.toLowerCase.trim())
}

class Controller extends ScalatraServlet with ScalateSupport with FileUploadSupport with FlashMapSupport {

  import SDBSerializer.generate

  def forward(path:String="/session")=servletContext.getRequestDispatcher(path).forward(request, response)

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

  delete("/experiments"){
    //TODO: Test cascading deletes
    (UserSession(session),EntityId(params.get("id"))) match{
      case (Some((uid,userName)),Some(expId))=>
        Experiments.remove(Map("uid"->uid,"_id"->expId))
        val failed=Experiments.findOne(Map("uid"->uid,"_id"->expId)).isDefined
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
      case (Some((uid,user_name)),Some(name),Some(description),Some(timezone),Some(website),Some(picture),Some(public_access)) if UniqueName(Experiments,name,"uid"->uid)=>
        Experiments.insert(Map("name"->name,"uid"->uid,"timezone"->timezone,"access_restriction"-> public_access,
          "picture"->picture,"website"->website, "token"->Utils.uuid(),
          "updated_at"->System.currentTimeMillis(),
          "created_at"->System.currentTimeMillis(),
          "description"->description))

        generate(Experiments.findOne(Map("uid"->uid,"name"->name)))
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
      case (Some((uid,userName)),Some(eid),Some(field),Some(value)) if validators(field).apply().isDefined && (field.equals("name") && UniqueName(Experiments,value,"uid"->uid)) =>
        Experiments.findAndModify(Map("uid"->uid,"_id"->eid),$set(field->value,"updated_at"->System.currentTimeMillis()))
        generate(Experiments.findOne(Map("uid"->uid,"_id"->eid)))
      case errors => haltMsg()
    }
  }
  get("/nodes"){
    // List the nodes

  }
  delete("/nodes"){
    (UserSession(session),EntityId(params.get("sid")),EntityId(params.get("eid"))) match {
      case (Some((uid,userName)),Some(nid),Some(eid))=>
        Nodes.remove(Map("uid"->uid,"_id"->nid,"eid"->eid))
        if(Nodes.findOne(Map("uid"->uid,"_id"->nid,"eid"->eid)).isDefined)
          haltMsg("Delete Failed")
        else
          halt(200,"Delete succeeded")
      case errors => haltMsg()
    }
  }

  put("/nodes"){
    // update/replace an nodes information
    val validators:Map[String,()=>Option[_]] = Map(
      "name"-> (()=> Name(params.get("field"))),
      "lat"-> (()=> LatLonAlt(params.get("field"))),
      "alt"-> (()=> LatLonAlt(params.get("field"))),
      "lon"-> (()=> LatLonAlt(params.get("field"))),
      "description"-> (()=> Description(params.get("field"))),
      "picture"-> (()=> PictureUrl(params.get("field"))),
      "website"-> (()=>WebUrl(params.get("field"))),
      "eid"-> (()=> EntityId(params.get("field"))))

    (UserSession(session),EntityId(params.get("nid")),ExperimentIdFromNodeId(EntityId(params.get("nid"))),params.get("field").filter(validators.keys.contains),params.get("value")) match {
      case (Some((uid,userName)),Some(nid),Some(eid),Some(field),Some(value)) if validators(field).apply().isDefined && (if(field== "name") UniqueName(Nodes,value,"eid"->eid) else true) && (if(field=="eid") OwnedBy(Experiments,uid,validators.apply(field).asInstanceOf[ObjectId]) else true) =>
        Nodes.findAndModify(Map("uid"->uid,"_id"->nid),$set(field->value,"updated_at"->System.currentTimeMillis()))
        generate(Nodes.findOne(Map("uid"->uid,"_id"->nid)))
      case errors => haltMsg()
    }
  }

  post("/nodes"){
    // Create a new node
    (UserSession(session),Name(params.get("name")),EntityId(params.get("eid")),LatLonAlt(params.get("lat")),LatLonAlt(params.get("lon")),LatLonAlt(params.get("alt")),Description(params.get("description")),WebUrl(params.get("website")),PictureUrl(params.get("picture"))) match{
      case (Some((uid,user_name)),Some(name),Some(eid),lat,lon,alt,Some(description),Some(website),Some(picture)) if UniqueName(Nodes,name,"eid"->eid)=>
        Nodes.insert(Map("name"->name,"uid"->uid,"eid"->eid,"lat"->lat,"lon"->lon,"alt"->alt,
          "picture"->picture,"website"->website, "token"->Utils.uuid(),
          "updated_at"->System.currentTimeMillis(),
          "created_at"->System.currentTimeMillis(),
          "description"->description))
        generate(Nodes.findOne(Map("uid"->uid,"name"->name,"eid"->eid)))
      case error=>haltMsg()
    }

  }

  delete("/streams"){
    (UserSession(session),EntityId(params.get("sid")),EntityId(params.get("eid")),EntityId(params.get("nid"))) match {
      case (Some((uid,userName)),Some(sid),Some(eid),Some(nid))=>
      case errors => haltMsg()
    }
  }

  put("/streams"){
    // update/replace stream information
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
