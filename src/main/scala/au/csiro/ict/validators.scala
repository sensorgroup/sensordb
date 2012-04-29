package au.csiro.ict

import org.jsoup.safety.Whitelist
import org.jsoup.Jsoup
import org.apache.commons.validator.GenericValidator._
import org.apache.commons.lang3.StringUtils._
import org.bson.types.ObjectId
import com.mongodb.casbah.Imports._
import java.util.Date
import javax.servlet.http.HttpSession
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import Cache._
  import com.codahale.jerkson.Json._
import scala.{Option, None}
import scala.Predef._

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

    def reset() = _errors=Nil
  }

  def PictureUrl(value:Option[String])(implicit validator:Validator)=value.orElse(EMPTY_STR).filter(url=>url.isEmpty || isUrl(url)).orElse(validator.addError( "Picture URL is not valid"))

  def WebUrl(value:Option[String])(implicit validator:Validator)=value.orElse(EMPTY_STR).filter{ url=> url.isEmpty || isUrl(url)}.orElse(validator.addError( "Webpage URL is not valid"))

  val timezones = List("-1200","-1130","-1100","-1030","-1000","-930","-900","-830","-800","-730","-700","-630","-600","-530","-500","-430","-400","-330","-300","-230","-200","-130","-100","-030","000" ,"030","100","130","200","230","300","330","400","430","500","530","600","630","700","730","800","830","900","930","1000","1030","1100","1130","1200")

  def TimeZone(value:Option[String])(implicit validator:Validator):Option[String]=value.filter(x=>timezones.indexOf(x)>=0).orElse(validator.addError( "Timezone is not valid"))

  def Privacy(value:Option[String])(implicit validator:Validator):Option[String]=value.filter(x => !x.trim.isEmpty && isInt(x) && isInRange(x.toInt,0,1)).orElse(Some(Cache.EXPERIMENT_ACCESS_PUBLIC))

  def Description(value:Option[String]):Option[String]=value.map(x=>sanitize(x.trim())).orElse(EMPTY_STR)

  def Name(value:Option[String])(implicit validator:Validator)=value.orElse(validator.addError( "Name is missing")).flatMap{v=>
    if (!isAlphanumericSpace(v))
      validator.addError("Name is not valid")
    else if (!isInRange(v.size,3,30))
      validator.addError("Name must have 3 to 30 characters")
    else
      Some(v)
  }

  def Email(value:Option[String])(implicit validator:Validator)=value.filter(isEmail).orElse(validator.addError("Email is invalid"))

  def Password(value:Option[String])(implicit validator:Validator):Option[String]=value.orElse(validator.addError( "Password is missing")).flatMap{p=>
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
    if (Users.count(Map("name"->u))==1)
      validator.addError("Username is not availale")
    else
      Some(u)
  }

  def UniqueEmail(v:Option[String])(implicit validator:Validator):Option[String]=v.flatMap{u=>
    if (Users.findOne(MongoDBObject("email"->u)).isDefined)
      validator.addError("Email is already used")
    else
      Some(u)
  }

  def UniqueName[B <: AnyRef](collection:MongoCollection,filters:(String,B)*)(implicit validator:Validator)=if (collection.count(filters.toMap)!=0){
    validator.addError("Name is not available")
    false
  }
  else true

  def OwnedBy[B <: AnyRef](col:MongoCollection,uid:ObjectId,entityId:ObjectId)(implicit validator:Validator)=if (col.count(MongoDBObject("_id"->entityId,"uid"->uid))==0){
    validator.addError("Access denied")
    false
  }
  else true

  /**
   *
   * @param session
   * @return (User_ID, User_Name)
   */
  def UserSession(session:HttpSession)(implicit validator:Validator):Option[(ObjectId,String)]={
    val sessionId=session.getAttribute(SESSION_ID)
    if(sessionId !=null) {
      cache.expire(sessionId,Cache.CACHE_TIMEOUT)
      Some(new ObjectId(Cache.cache.hget(sessionId,Cache.CACHE_UID).get)->cache.hget(sessionId,Cache.CACHE_USER_NAME).get)
    } else {
      None
    }
  }
  def EntityIdList(v:Option[String])(implicit validator:Validator):Set[ObjectId] = v match {
    case Some(sids:String) => try {
      parse[Set[String]](sids).map{ x=>
        if (ObjectId.isValid(x))
          new ObjectId(x)
        else
          throw new Exception("Invalid streamId:"+x)
      } match {
        case s:Set[ObjectId] if s.size>0 => s
        case empty=>
          validator.addError("Empty sid parameter")
          Set[ObjectId]()
      }
    }catch{
      case exception =>
        validator.addError("An invalid Stream id")
        Set[ObjectId]()
    }
    case others =>
      validator.addError("Missing or invalid sid parameter")
      Set[ObjectId]()
  }

  def EntityId(v:Option[String])(implicit validator:Validator):Option[ObjectId]=v.filter(org.bson.types.ObjectId.isValid).map(x=>new ObjectId(x)).orElse{
    validator.addError("Invalid entity id")
    None
  }
  // formats the date to yyyyD
  def DateParam(v:Option[String])(implicit validator:Validator):Option[String]=v.flatMap(x=>
    try {
      Some(Utils.TIMESTAMP_YYYYD_FORMAT.print(Utils.UkDateFormat.parseDateTime(x)))
    } catch {
      case err =>None
    }
  ).orElse{
    validator.addError("Invalid date parameter")
    None
  }
  def TimeParam(v:Option[String])(implicit validator:Validator):Option[Int]=v.flatMap(x=>
    try {
      val ts = Utils.TimeParser.parseDateTime(x)
      Some(ts.getSecondOfDay)
    } catch {
      case err =>None
    }
  ).orElse{
    validator.addError("Invalid date parameter")
    None
  }

  def ExperimentIdFromNodeId(nid:Option[ObjectId])(implicit validator:Validator):Option[ObjectId]=nid.flatMap(nid=>
    Nodes.findOne(Map("_id"->nid),Map("eid"->1)).flatMap(_.getAs[ObjectId]("eid"))
  )
  def NodeIdFromStreamId(sid:Option[ObjectId])(implicit validator:Validator):Option[ObjectId]=sid.flatMap(sid=>
    Streams.findOne(Map("_id"->sid),Map("nid"->1)).flatMap(_.getAs[ObjectId]("nid"))
  )

  def MeasurementId(unitId:Option[ObjectId])(implicit validator:Validator):Option[ObjectId]=unitId.filter(unitId=> Measurements.count(Map("_id"->unitId)) == 1)

  def LatLonAlt(v:Option[String])(implicit validator:Validator):Option[String]=v.orElse(EMPTY_STR).map(_.trim).filter(x=> x.isEmpty || isDouble(x))

  def Required(v:Option[String],errorMessage:String)(implicit validator:Validator):Option[String]=v.filterNot(isEmpty).orElse{
    validator.addError(errorMessage)
    None
  }
}


