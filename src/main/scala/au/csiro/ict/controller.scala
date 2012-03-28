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
}
class Controller extends ScalatraServlet with ScalateSupport with FileUploadSupport {
  def withUserPassword()(f: (String,String,Validator)=>Unit){
    val validator = new Validator(params)
    validator.test("name",List(("Username is Required",x=>x.isDefined && !isBlankOrNull(x.get)),
      ("Username is not Valid",x=>isAlphanumeric(x.get)),
      ("Username must have 2 to 20 characters",x=>isInRange(x.get.size,2,20))))

    validator.test("password",List(("Password is Required",x=>x.isDefined),
      ("Password is not valid",x=>isAsciiPrintable(x.get)),
      ("Password must have at least 6 characters",_.get.size>=6)))

    if (!validator.errors.isEmpty) halt(400,generate(validator.errors))
    val name = params("name")
    val password = params("password")

    f(name,password,validator)
    forwardToSessionPath
  }
  def userSession=Sessions.userSession.map{user=>
    generate(Map("id"->user.id.toString,"token"->user.token))
  }.getOrElse("{}")
  
  def forwardToSessionPath=servletContext.getRequestDispatcher("/session").forward(request, response)


  val sanitize = Jsoup.clean(_:String, Whitelist.basic())
  private val logger: Logger = Logger[this.type]
  post("/register") {
    logger.info("User registering with username:"+params.get("name")+" and email:"+params.get("email"))

    val validator = new Validator(params)
    validator.test("name",List(("Username is Required",x=>x.isDefined && !isBlankOrNull(x.get)),
      ("Username is not Valid",x=>isAlphanumeric(x.get)),
      ("Username must have 2 to 20 characters",x=>isInRange(x.get.size,2,20)),
      ("Username is already used",x=>User.findByName(x.get).isEmpty)))

    validator.test("password",List(("Password is Required",x=>x.isDefined),
      ("Password is not valid",x=>isAsciiPrintable(x.get)),
      ("Password must have at least 6 characters",_.get.size>=6)))

    validator.test("email",List(("Email is Required",x=>x.isDefined && !isBlankOrNull(x.get)),
      ("Email is not valid",x=>isEmail(x.get))))

    validator.test("timezone",List(("Timezone is Required",x=>x.isDefined && !isBlankOrNull(x.get)),
      ("Timezone is not valid",x=>isInt(x.get) && isInRange (x.get.toInt,-1200,+1200))))

    validator.test("website",List(("Website is not valid",x=>x.isEmpty || isUrl(x.get))))

    var picture=if(ServletFileUpload.isMultipartContent(request)) fileParams.get("picture") else None

    if (!validator.errors.isEmpty) halt(400,generate(validator.errors))

    var website=params.get("website")
    var description=params.get("description").map(_.trim()).filterNot(isBlankOrNull).map(sanitize)
    val name = params("name")
    val password = params("password")
    val timezone = params("timezone").toInt
    val email = params("email")
    val pic = None
    val user = User(name,BCrypt.hashpw(password, BCrypt.gensalt()),timezone,email,pic,website,description)
    User.insert(user)
    Sessions.login(name,password)
    forwardToSessionPath
  }
  post("/logout"){ 
    Sessions.logout
    forwardToSessionPath
  }
  post("/session"){
    userSession
  }
  post("/login"){
    withUserPassword(){(user,password,validator)=>
      logger.info("User login request with username:"+user)
      if (User.findByName(user).isEmpty)
        halt(200)
      Sessions.login(user,password)
    }
  }
  post("/remove"){
    withUserPassword(){(name,password,validator)=>
      User.findByName(name).filter(r=> BCrypt.checkpw(password, r.password)) match {
        case Some(u:User)=>
          User.removeById(u.id)
          Sessions.logout
        case others=>
      }
      halt(200)
    }
  }

  notFound {
    findTemplate(requestPath) map {
      path =>
        contentType = "text/html"
        layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }
}
