package au.csiro.ict

import org.scalatra.ScalatraServlet
import com.mongodb.casbah.Imports._
import com.codahale.jerkson.Json._
import au.csiro.ict.JsonGenerator.generate
import au.csiro.ict.Validators._
import au.csiro.ict.Cache._
import scala.Some
import org.joda.time.DateTimeZone

trait RestfulDataAccess {

  self:ScalatraServlet with RestfulHelpers=>

  val workersProxy = new UpdateBrokerProxy(Cache.store)

  //  val ips = new InputProcessingSystemProxy // activate this line for distributed message processing
  post("/data"){
    /* Inserting time series data into a group of streams by sending POST requests.
    * Required parameters:
    * data: a string value with in the following format of {token:{ts,value},token:{ts,value}} where,
    * token is a security token, ts is timestamp in form of seconds from epoch (int) and value is double number or null
    */
    params.get("data") match {
      case Some(s:String) if !s.trim.isEmpty =>
        try{
          val packed = parse[Map[String,Map[Int,Option[Double]]]](s)
          if(!packed.keys.forall(Utils.keyPatternMatcher))
            haltMsg("Bad request, invalid tokens")

          val allKeysMapped = Streams.find(MongoDBObject("token"->MongoDBObject("$in"->packed.keys.toArray)),MongoDBObject("_id"->1,"token"->1,"nid"->1)).map{x=>
            val sid=x.getAs[ObjectId]("_id").get
            val timeZone = Cache.experimentTimeZoneFromNid(x.getAs[ObjectId]("nid").get)
            val token = x("token").toString
            sid.toString ->(packed(token),timeZone)
          }.toMap

          if(allKeysMapped.size != packed.size)
            haltMsg("Bad request, invalid security token(s)")
          allKeysMapped.foreach { item =>
            workersProxy.process(RawData(item._1,item._2._1,item._2._2))
          }

          generate(Map("length"->packed.values.map(_.size).sum))
        } catch {
          case e:java.lang.Exception =>
            haltMsg("Bad input, can't parse the data parameter, please verify the body of your request")
        }
      case missing => haltMsg("Bad input, missing parameter data from our request")
    }
  }

  /**
   * if the current user doesn't have enough rights to access sid, the method returns None otherwise returns ObjectId of a parent Node ID
   * @param sid to check user permission on
   * @return SID, NodeId and Experiment's TZ only if user has enough permissions to access this stream
   */
  def permissionCheck(sid:ObjectId):Option[(ObjectId,ObjectId,DateTimeZone)]={
    NidUidFromSid(sid) match {
      case Some(s:DBObject) =>
        val nid:ObjectId = s.getAs[ObjectId]("nid").get
        Nodes.findOne(MongoDBObject("_id"->nid),MongoDBObject("eid"->1)) match {
          case Some(n:DBObject)=> Experiments.findOne(MongoDBObject("_id"->n.getAs[ObjectId]("eid").get),MongoDBObject(ACCESS_RESTRICTION_FIELD->1,"timezone"->1)) match {
            case Some(e:DBObject)=> e.getAs[String](ACCESS_RESTRICTION_FIELD) match {
              case Some(EXPERIMENT_ACCESS_PUBLIC) => Some((sid,nid,DateTimeZone.forID(e.getAs[String]("timezone").get)))
              case Some(EXPERIMENT_ACCESS_PRIVATE) if UserSession(session).filter(_._1 ==  s.getAs[ObjectId]("uid")).isDefined => Some((sid,nid,DateTimeZone.forID(e.getAs[String]("timezone").get)))
              case others =>
                haltMsg("Access Denied")
            }
            case other => haltMsg("Bad input, access denied, please verify the body of your request")
          }
          case other => haltMsg("Bad input, missing experiment, please verify the body of your request")
        }
      case other => haltMsg("Bad input, missing node, please verify the body of your request")
    }
  }

  get("/data"){
    /* querying raw data from a stream by sending GET requests to /data url.
    * Required parameters are
    * sid => sensor id as String convertable to ObjectId on MongoDB
    * sd => start date => UK format => 30-01-2012
    * ed => end date => uk format
    * st (optional) => start time index => For example for raw, you use seconds, 0 until 86399
    * et (optional) => end time => similar format to ST
    * level (optional, default is raw)=> Level is text and can be raw, 1-minute, 5-minute, 15-minute, 1-hour, 3-hour, 6-hour, 1-day, 1-month, 1-year
    */
    (AggregationLevelParam(params.get("level")),EntityIdList(params.get("sid")),DateParam(params.get("sd")),DateParam(params.get("ed"))) match {
      case (levelOption,sids,Some(start_date),Some(end_date)) if !sids.isEmpty=>
        val level = levelOption.getOrElse(RawLevel)
        val cols = (CellKeyParam(IntParam(params.get("st")),level),CellKeyParam(IntParam(params.get("et")),level)) match {
          case (Some(fromColIdx:Int),Some(toColIdx:Int)) => Some((fromColIdx,toColIdx))
          case others => None
        }
        var timezones: Set[Option[(ObjectId,ObjectId, DateTimeZone)]] = sids.map(permissionCheck)
        if(timezones.forall(_.isDefined)){
          val chunker = new DefaultChunkFormatter(new JSONWriter(response.getWriter))
          timezones.groupBy(_.get._3).foreach{(reqs)=>
            store.get(reqs._2.map(_.get._1.toString),start_date,end_date,cols,reqs._1,level,chunker)
          }
          chunker.done()
        }else
          haltMsg("Permission denied")

      case others => haltMsg("Bad input, missing stream, please verify the body of your request")
    }
  }
}

