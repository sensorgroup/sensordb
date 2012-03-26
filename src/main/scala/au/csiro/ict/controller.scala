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
  val sanitize = Jsoup.clean(_:String, Whitelist.basic())
  private val logger: Logger = Logger[this.type]

  get("/register") {
    val validator = new Validator(params)

    validator.test("name",List(("Username is Required",x=>x.isDefined && !isBlankOrNull(x.get)),
      ("Username is not Valid",x=>isAlphanumeric(x.get)),
      ("Username must have 2 to 10 characters",x=>isInRange(x.get.size,2,10)),
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
    var website=params.get("website")
    var description=params.get("description").map(_.trim()).filterNot(isBlankOrNull).map(sanitize)

    if (!validator.errors.isEmpty)
      halt(400,generate(validator.errors))
    val name = params("name")
    val password = params("password")
    val timezone = params("timezone").toInt
    val email = params("email")
    val pic = None
    val user = User(name,password,timezone,email,pic,website,description)
    User.insert(user)
    //TODO: redirect to login
    //TODO: check image upload, validating if it is really image, putting it into the right folder, imagemagic styles
    generate(Map("id"->user.id.toString))
    //    Configuration("redis.queue.port")
    //    Json.parse(Source.fromFile("config.json"))
    //    generate(Configuration("1",1,"2",2,"3",3,"4",4))

  }

  notFound {
    findTemplate(requestPath) map {
      path =>
        contentType = "text/html"
        layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }
}
