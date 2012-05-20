package au.csiro.ict

import org.jsoup.safety.Whitelist
import org.jsoup.Jsoup
import org.apache.commons.validator.GenericValidator._
import org.apache.commons.lang3.StringUtils._
import org.bson.types.ObjectId
import com.mongodb.casbah.Imports._
import java.util.Date
import javax.servlet.http.HttpSession
import Cache._
import com.codahale.jerkson.Json._
import scala.{Option, None}
import scala.Predef._
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.DateTime

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

  def IntParam(value:Option[String])(implicit validator:Validator):Option[Int]=value.filter(x => isInt(x)).map(_.toInt)

  def Description(value:Option[String]):Option[String]=value.map(x=>sanitize(x.trim())).orElse(EMPTY_STR)

  def Name(value:Option[String])(implicit validator:Validator)=value.orElse(validator.addError( "Name is missing")).flatMap{v=>
    if (!isAlphanumericSpace(v))
      validator.addError("Name is not valid")
    else if (!isInRange(v.size,3,30))
      validator.addError("Name must have 3 to 30 characters")
    else
      Some(v)
  }

  val METADATA_NAME_REGEX="""[a-zA-Z][a-zA-Z0-9_\s]{0,29}""".r.pattern.matcher(_:String).matches()

  def PatternMatch(value:Option[String],pattern:(String)=>Boolean)(implicit validator:Validator)=value.orElse(validator.addError( "Value is missing")).flatMap{v=>
    if (!isInRange(v.size,1,30))
      validator.addError("Value must have 1 to 30 characters")
    else if (!pattern(v))
      validator.addError("Value is not valid")
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
  def AggregationLevelParam(v:Option[String])(implicit validator:Validator):Option[AggregationLevel]=v.flatMap{v=>
    AggregationLevel(v.trim.toLowerCase)
  }

  def EntityIdList(v:Option[String])(implicit validator:Validator):Set[ObjectId] = v match {
    case Some(sid:String) if ObjectId.isValid(sid)=> Set(new ObjectId(sid))
    case Some(sids:String) => try {
      parse[Set[String]](sids).map{ x=>
        if (ObjectId.isValid(x))
          new ObjectId(x)
        else
          throw new RuntimeException("Bad streamid array: "+x)
      } match {
        case s:Set[ObjectId] if !s.isEmpty => s
        case empty=>
          validator.addError("Empty sid parameter, sid is an array of stream ids.")
          empty
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
  def DateParam(v:Option[String])(implicit validator:Validator):Option[Int]=v.flatMap(x=>
    try {
      Some((Utils.ukDateFormat.parseDateTime(x).getMillis/1000L).asInstanceOf[Int])
    } catch {
      case err =>None
    }
  ).orElse{
    validator.addError("Invalid date parameter")
    None
  }
  def DateTimeParam(v:Option[String])(implicit validator:Validator):Option[DateTime]=v.flatMap(x=>
    try {
      Some(Utils.ukDateFormat.parseDateTime(x))
    } catch {
      case err =>None
    }
  ).orElse{
    validator.addError("Invalid date parameter")
    None
  }
  // formats the date to yyyy-MM-dd'T'HH:mm:ss.SSSZZ
  val  isoTSFormatter = ISODateTimeFormat.dateTime()

  def IsoTimestampParam(v:Option[String])(implicit validator:Validator):Option[DateTime]=v.flatMap(x=>
    try {
      Some(isoTSFormatter.parseDateTime(x))
    } catch {
      case err =>None
    }
  ).orElse{
    validator.addError("Invalid timestamp parameter, expected input format yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
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
  def CellKeyParam(value:Option[Int],aggLevel:AggregationLevel)(implicit validator:Validator):Option[Int]={
    value.flatMap{x=>
      if (aggLevel.validColumnKey(x))
        Some(x)
      else {
        validator.addError("Bad column: "+value.get)
        None
      }
    }
  }
}


