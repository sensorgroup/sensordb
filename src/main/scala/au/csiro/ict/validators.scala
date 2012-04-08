package au.csiro.ict

import util.matching.Regex
import org.jsoup.safety.Whitelist
import org.jsoup.Jsoup
import org.apache.commons.validator.GenericValidator._
import org.apache.commons.lang3.StringUtils._
import org.bson.types.ObjectId
import com.mongodb.casbah.Imports._
import java.util.Date
import javax.servlet.http.HttpSession

object Validators {
  val sanitize = Jsoup.clean(_:String, Whitelist.basic())

  val EMPTY_STR = Some("")

  val SESSION_ID = "sdb"

  val EMPTY_OBJECT_ID=new ObjectId(new Date(1000L))

  case class Validator(){
    private var _errors:List[String] = Nil;
    def errors = _errors

    def addError(msg:String):Option[String]= {
      _errors::=msg
      None
    }
  }

  def PictureUrl(value:Option[String])(implicit validator:Validator)=value.orElse(EMPTY_STR).filter(url=>url.isEmpty || isUrl(url)).orElse(validator.addError( "Picture URL is not valid"))

  def WebUrl(value:Option[String])(implicit validator:Validator)=value.orElse(EMPTY_STR).filter{ url=> url.isEmpty || isUrl(url)}.orElse(validator.addError( "Webpage URL is not valid"))

  val timezones = List("-1200","-1130","-1100","-1030","-1000","-930","-900","-830","-800","-730","-700","-630","-600","-530","-500","-430","-400","-330","-300","-230","-200","-130","-100","-030","000" ,"030","100","130","200","230","300","330","400","430","500","530","600","630","700","730","800","830","900","930","1000","1030","1100","1130","1200")

  def TimeZone(value:Option[String])(implicit validator:Validator):Option[String]=value.filter(x=>timezones.indexOf(x)>=0).orElse(validator.addError( "Timezone is not valid"))

  def Privacy(value:Option[String])(implicit validator:Validator):Option[String]=value.filter(x => !x.trim.isEmpty && isInt(x) && isInRange(x.toInt,0,1)).orElse(Some("0"))

  def Description(value:Option[String]):Option[String]=value.map(_.trim()).map(sanitize).orElse(EMPTY_STR)

  def Name(value:Option[String])(implicit validator:Validator)=value.orElse(validator.addError( "Name is missing")).flatMap{v=>
    if (!isAlphanumeric(v))
      validator.addError("Name is not valid")
    else if (!isInRange(v.size,3,30))
      validator.addError("Name must have 3 to 30 characters")
    else
      Some(v)
  }

  def Email(value:Option[String])(implicit validator:Validator)=value.orElse(validator.addError( "Email is missing")).filter(isEmail).orElse(validator.addError("Email is invalid"))

  def Password(value:Option[String])(implicit validator:Validator):Option[String]=value.orElse(validator.addError( "Name is missing")).flatMap{p=>
    if (!isAsciiPrintable(p))
      validator.addError("Password should only contain valid printable ascii characters")
    else if (!isInRange(p.size,6,30))
      validator.addError("Password must have 6 to 30 characters")
    else
      Some(p)
  }
  def Username(value:Option[String])(implicit validator:Validator):Option[String]=value.orElse(validator.addError("Name is missing")).flatMap{u=>
    if (!isAlphanumeric(u))
      validator.addError("Name is not valid")
    else if (!isInRange(u.size,3,30))
      validator.addError("Name must have 3 to 30 characters")
    else
      Some(u)
  }

  def UniqueUsername(v:Option[String])(implicit validator:Validator):Option[String]=v.flatMap{u=>
    if (User.findByName(u).isDefined)
      validator.addError("Username is not availale")
    else
      Some(u)
  }

  def UniqueExperiment(userId:ObjectId,experiment_name:String,exclude_id:ObjectId=EMPTY_OBJECT_ID)(implicit validator:Validator):Boolean=
    if(Experiment.collection.findOne(Map("user_id"->userId,"name"->experiment_name,"_id"->MongoDBObject("$ne"->exclude_id))).isDefined){
      validator.addError("Experiment name is not available")
      false
    }
    else
      true


  /**
   *
   * @param session
   * @return (User_ID, User_Name)
   */
  def UserSession(session:HttpSession)(implicit validator:Validator):Option[(ObjectId,String)]={
    val sessionId=session.getAttribute(SESSION_ID)
    if(sessionId !=null) {
      Cache.cache.expire(sessionId,Cache.CACHE_TIMEOUT)
      Some(new ObjectId(Cache.cache.hget(sessionId,Cache.CACHE_USER_ID).get)->Cache.cache.hget(sessionId,Cache.CACHE_USER_NAME).get)
    } else {
      validator.addError("Invalid session")
      None
    }
  }

  def EntityId(v:Option[String])(implicit validator:Validator):Option[ObjectId]=v.filter(x=>org.bson.types.ObjectId.isValid(x)).map(x=>new ObjectId(x)).orElse{
    validator.addError("Invalid entity id")
    None
  }

  //  def withSession(v:Validator)(successBlock:(String,String)=>Unit)(implicit session:HttpSession)=
  //    Sessions.userSession.orElse{
  //      v.addError("No active user session available")
  //      None
  //    }.foreach(user=>successBlock.apply(user._1,user._2))

  //  def withName(field:String="name",v:Validator)(successBlock:(String,String)=>Unit)=params.get(field).filterNot(_.trim.isEmpty).filter{ name=>
  //      import RegexUtils._
  //      //    if (!("""[a-zA-Z0-9_ ]{3,30}""".r matches name))
  //
  //        true
  //    }.orElse{
  //      validator.addError("Name is required")
  //      None
  //    }
  //  }
  ////
  ////  def withUserPassword()(f: (String,String,Validator)=>Unit){
  ////    val validator = new Validator(params)
  ////    validator.test("name",List(("Username is Required",x=>x.isDefined && !isBlankOrNull(x.get)),
  ////      ("Username is not valid",x=>isAlphanumeric(x.get)),
  ////      ("Username must have 2 to 20 characters",x=>isInRange(x.get.size,2,20))))
  ////
  ////    validator.test("password",List(("Password is Required",x=>x.isDefined),
  ////      ("Password is not valid",x=>isAsciiPrintable(x.get)),
  ////      ("Password must have at least 6 characters",_.get.size>=6)))
  ////
  ////    if (!validator.errors.isEmpty) halt(400,generate(validator.errors))
  ////    val name = params("name")
  ////    val password = params("password")
  ////
  ////    f(name,password,validator)
  ////    forward()
  ////  }
}
