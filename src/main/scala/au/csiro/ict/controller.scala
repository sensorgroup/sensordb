package au.csiro.ict

import org.scalatra._
import scalate.ScalateSupport
import grizzled.slf4j.Logger
import org.scalatra.fileupload.FileUploadSupport
import com.codahale.jerkson.Json._
import io.Source
import org.apache.commons.validator.GenericValidator._
import org.apache.commons.lang3.StringUtils._
import java.util.Properties
import java.io.FileReader
import scala.collection.JavaConversions._
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import org.apache.commons.fileupload.servlet.ServletFileUpload
import scala.{None, Option}
import org.mindrot.jbcrypt.BCrypt
import javax.swing.plaf.OptionPaneUI
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._

object Configuration{
  private val config = new Properties()
  config.load(getClass.getResourceAsStream("/config.properties"))
  val config_map = config.entrySet().map(x=>(x.getKey.toString.trim().toLowerCase,x.getValue.toString.trim())).toMap[String, String]
  def apply(name:String):Option[String]= config_map.get(name.toLowerCase.trim())
}

case class Validator(params:Map[String,String]){
  private var _errors:List[String] = Nil;
  def errors = _errors
  def test(name:String, validator:List[(String,(Option[String])=>Boolean)])=
    validator.foldLeft(None:Option[String])((err,item)=>
      if(err.isEmpty)
        if (!item._2(params.get(name)))
          Some(item._1)
        else
          None
      else
        err
    ).foreach(x=>_errors::=x)
  def addError(msg:String):Boolean= {
    _errors::=msg
    false
  }
}
class Controller extends ScalatraServlet with ScalateSupport with FileUploadSupport {
  def withUserPassword()(f: (String,String,Validator)=>Unit){
    val validator = new Validator(params)
    validator.test("name",List(("Username is Required",x=>x.isDefined && !isBlankOrNull(x.get)),
      ("Username is not valid",x=>isAlphanumeric(x.get)),
      ("Username must have 2 to 20 characters",x=>isInRange(x.get.size,2,20))))

    validator.test("password",List(("Password is Required",x=>x.isDefined),
      ("Password is not valid",x=>isAsciiPrintable(x.get)),
      ("Password must have at least 6 characters",_.get.size>=6)))

    if (!validator.errors.isEmpty) halt(400,generate(validator.errors))
    val name = params("name")
    val password = params("password")

    f(name,password,validator)
    forward()
  }
  def validSession(implicit validator:Validator):Option[(String,String)]=Sessions.userSession.orElse{
    validator.addError("No active user session available")
    None
  }

  def validPictureUrl(implicit validator:Validator):Option[String]=params.get("picture").filter{ url=>
    if(!url.isEmpty && !isUrl(url))
      validator.addError( "Picture URL is not valid")
    else
      true
  }
  val timezones = List("-1200","-1130","-1100","-1030","-1000","-930","-900","-830","-800","-730","-700","-630","-600","-530","-500","-430","-400","-330","-300","-230","-200","-130","-100","-030","000" ,"030","100","130","200","230","300","330","400","430","500","530","600","630","700","730","800","830","900","930","1000","1030","1100","1130","1200")

  def validTimeZone(implicit validator:Validator):Option[String]={
    params.get("timezone").filter(x=>timezones.indexOf(x)>=0).orElse{
      validator.addError( "Timezone is not valid")
      None
    }
  }
  def validBoolean(name:String)(implicit validator:Validator):Boolean=
    params.get(name).filterNot(_.trim.isEmpty).filter(x=>x.equalsIgnoreCase("true")||x.equalsIgnoreCase("yes")||x.equalsIgnoreCase("1")).isDefined

  def validWebUrl(implicit validator:Validator):Option[String]=params.get("webpage").filter{ url=>
    if(!url.isEmpty && !isUrl(url))
      validator.addError( "Webpage URL is not valid")
    else
      true
  }

  def validName(implicit validator:Validator):Option[String]=params.get("name").filterNot(_.trim.isEmpty).filter{ name=>
    if (!isAlphanumericSpace(name))
      validator.addError("Name is not valid")
    else if (!isInRange(name.size,3,30))
      validator.addError("Name must have 5 to 30 characters")
    else
      true
  }.orElse{
    validator.addError("Name is required")
    None
  }

  def validDescription=params.get("description").map(_.trim()).filterNot(isBlankOrNull).map(sanitize)

  def validRequiredAlphaNumericField(field:String)(implicit validator:Validator):Option[String]=params.get(field).filter(isAlphanumeric).map(_.trim).orElse{
    validator.addError(capitalize(field)+" is required")
    None
  }

  def halfIfError(implicit validator:Validator)=if (!validator.errors.isEmpty) halt(400,generate(Map("errors"->validator.errors)))

  def forward(path:String="/session")=servletContext.getRequestDispatcher(path).forward(request, response)

  val sanitize = Jsoup.clean(_:String, Whitelist.basic())

  private val logger: Logger = Logger[this.type]

  val protectedFields = Map("token"->0,"password"->0,"email"->0)

  post("/session"){
    val current = Sessions.userSession.map(_._1)
    val user = params.get("user").filterNot(_.trim.isEmpty).flatMap{uname=>
      User.collection.findOne(Map("name"->uname),Map("_id"->1))
    }.map(x=>x.getAs[ObjectId]("_id").toString)
     val fields = if (current.exists(x=>user.isEmpty || x.equals(user.get)))
      Map("password"->0)
    else
      protectedFields
    user.orElse(current).flatMap(u=>User.collection.findOne(Map("_id"->new ObjectId(u)))).map{user=>
      val uid = user._id.get.toString
      user.put("_id",uid)
      generate(Map("user"->user,
        "experiments"->Experiment.collection.find(Map("user_id"->uid),fields),
        "nodes"->Node.collection.find(Map("user_id"->uid),fields),
        "streams"->Stream.collection.find(Map("user_id"->uid),fields)))
    }.getOrElse("{}")
  }


  post("/register") {
    logger.info("User registering with username:"+params.get("name")+" and email:"+params.get("email"))

    val validator = new Validator(params)
    validator.test("name",List(("Username is Required",x=>x.isDefined && !isBlankOrNull(x.get)),
      ("Username is not valid",x=>isAlphanumeric(x.get)),
      ("Username must have 2 to 20 characters",x=>isInRange(x.get.size,2,20)),
      ("Username is already used",x=>User.findByName(x.get).isEmpty)))

    validator.test("password",List(("Password is Required",x=>x.isDefined),
      ("Password is not valid",x=>isAsciiPrintable(x.get)),
      ("Password must have at least 6 characters",_.get.size>=6)))

    validator.test("email",List(("Email is Required",x=>x.isDefined && !isBlankOrNull(x.get)),
      ("Email is not valid",x=>isEmail(x.get))))

    validator.test("timezone",List(("Timezone is Required",x=>x.isDefined && !isBlankOrNull(x.get)),
      ("Timezone is not valid",x=>isInt(x.get) && isInRange (x.get.toInt,-1200,+1200))))

    validator.test("website",List(("Website URL is not valid",x=>x.isEmpty || isUrl(x.get))))

    validator.test("picture",List(("Picture URL is not valid",x=>x.isEmpty || isUrl(x.get))))

    if (!validator.errors.isEmpty) halt(400,generate(validator.errors))

    var website=params.get("website")
    var description=params.get("description").map(_.trim()).filterNot(isBlankOrNull).map(sanitize)
    val name = params("name")
    val password = params("password")
    val timezone = params("timezone").toInt
    val email = params("email")
    val pic = params.get("picture")
    val user = Map("name"->name,"token"->Utils.uuid(),"password"->BCrypt.hashpw(password, BCrypt.gensalt()),"timezone"->timezone,"email"->email,"picture"->pic,"website"->website,"description"->description)
    User.collection.insert(user)
    Sessions.login(name,password)
    forward()
  }
  post("/logout"){
    Sessions.logout
    forward()
  }

  post("/login"){
    withUserPassword(){(user,password,validator)=>
      logger.info("User login request with username:"+user)
      Sessions.login(user,password)
    }
  }
  post("/remove"){
    withUserPassword(){(name,password,validator)=>
      User.collection.findOne(Map("name"->name)).filter(r=> BCrypt.checkpw(password, r.getAs[String]("password").get))
        .foreach{u=>
        User.collection.remove(Map("name"->name))
        Sessions.logout
      }
    }
    halt(200)
  }

  delete("/experiments"){
    implicit val validator = new Validator(params)
    val user_id = validSession
    val experimentId=validRequiredAlphaNumericField("experiment_id")
    halfIfError
    Experiment.collection.remove(Map("user_id"->user_id.get,"experimentId"->experimentId.get))
    val failed=Experiment.collection.findOne(Map("user_id"->user_id.get,"experimentId"->experimentId.get)).isDefined
    if(failed) halt(400,"Delete failed")
    halt(200,"Delete succeeded")
  }

  //todo: test that experiment information with private flag is not available through session
  post("/experiments"){
    // Add a new experiments
    implicit val validator = new Validator(params)
    val name = validName
    val description = validDescription
    val website = validWebUrl
    val picture = validPictureUrl
    val user_id = validSession
    val timezone = validTimeZone
    val public = validBoolean("public_access")
    if(user_id.isDefined && name.isDefined && Experiment.collection.findOne(Map("user_id"->user_id.get._1,"name"->name.get)).isDefined)
      validator.addError("Experiment name is not available")
    halfIfError
    val newExperiment = Map("name"->name.get,
      "user_id"->user_id.get._1,
      "timezone"->timezone.get,
      "access_restriction"-> (if(public) Experiment.ACCESS_PUBLIC else Experiment.ACCESS_PRIVATE),
      "picture"->picture,
      "website"->website,
      "token"->Utils.uuid(),
      "updated_at"->System.currentTimeMillis(),
      "created_at"->System.currentTimeMillis(),
      "description"->description)

    Experiment.collection.insert(newExperiment)
    generate(Experiment.collection.findOne(Map("user_id"->user_id.get._1,"name"->name.get)).map(x=>x.put("_id",x.get("_id").toString)))
  }
  put("/experiments"){
    // update/replace an experiment information
  }
  get("/nodes"){
    // List the nodes

  }
  delete("/nodes"){
    implicit val validator = new Validator(params)
    val experimentId=validRequiredAlphaNumericField("experiment_id")
    val nodeId=validRequiredAlphaNumericField("node_id")
    halfIfError
  }

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
    implicit val validator = new Validator(params)
    val experimentId=validRequiredAlphaNumericField("experiment_id")
    val nodeId=validRequiredAlphaNumericField("node_id")
    val streamId=validRequiredAlphaNumericField("stream_id")
    halfIfError
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
