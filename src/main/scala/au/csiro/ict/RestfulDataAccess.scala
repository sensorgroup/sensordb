package au.csiro.ict

import org.scalatra.ScalatraServlet
import com.mongodb.casbah.Imports._
import com.codahale.jerkson.Json._
import au.csiro.ict.JsonGenerator.generate
import au.csiro.ict.Validators._
import au.csiro.ict.Cache._
import javax.servlet.http.{HttpServletResponse}
import scala.Some
import java.io.BufferedWriter
import org.joda.time.DateTime

trait RestfulDataAccess {

  self:ScalatraServlet with RestfulHelpers=>

  val ips = InputProcessingBackend.ips
  //  val ips = new InputProcessingSystemProxy // activate this line for distributed message processing
  post("/data/raw"){
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
            val token = x("token").toString
            val sid=x("_id").toString
            val nid=x("nid").toString
            Utils.inputQueueIdFor(nid,sid) -> packed(token)
          }.toMap

          if(allKeysMapped.size != packed.size)
            haltMsg("Bad request, invalid security token(s)")

          allKeysMapped.foreach{item=>
            Cache.queue.call(_.rpush(item._1,generate(item._2)))
            ips.process(Task(item._1))
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
   * @return NodeId only if user has enough permissions to access this stream
   */
  def permissionCheck(sid:ObjectId):Option[ObjectId]={
    NidUidFromSid(sid) match {
      case Some(s:DBObject) =>
        val nid:ObjectId = s.getAs[ObjectId]("nid").get
        Nodes.findOne(MongoDBObject("_id"->nid),MongoDBObject("eid"->1)) match {
          case Some(n:DBObject)=> Experiments.findOne(MongoDBObject("_id"->n.getAs[ObjectId]("eid").get),MongoDBObject(ACCESS_RESTRICTION_FIELD->1)) match {
            case Some(e:DBObject)=> e.getAs[String](ACCESS_RESTRICTION_FIELD) match {
              case Some(EXPERIMENT_ACCESS_PUBLIC) => Some(nid)
              case Some(EXPERIMENT_ACCESS_PRIVATE) if UserSession(session).filter(_._1 ==  s.getAs[ObjectId]("uid")).isDefined => Some(nid)
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

  get("/data/raw"){
    /* querying raw data from a stream by sending GET requests to /data url.
    * Required parameters are
    * sid => sensor id as String convertable to ObjectId on MongoDB
    * sd => start date => UK format => 30-01-2012
    * ed => end date => uk format
    * st => start time => 22:03:00
    * et => end time => 23:01:59
    */
    (EntityId(params.get("sid")),DateParam(params.get("sd")),TimeParam(params.get("st")),DateParam(params.get("ed")),TimeParam(params.get("et"))) match {
      case (Some(sid),Some(start_date),Some(st),Some(end_date),Some(et)) => permissionCheck(sid) match {
        case Some(nid)=> store.get(sid.toString, start_date ,end_date,st,et).foldLeft(new DefaultChunkFormatter(new JSONWriter(response.getWriter))){(sum,item)=>
          sum.insert(sid.toString,item._1,item._2)
          sum
        }.done()
        case other => haltMsg("Bad input, access denied, please verify the body of your request")
      }
      case other => haltMsg("Bad input, missing stream, please verify the body of your request")
    }
  }

  get("/data/summary/daily"){
    /**
     * TODO: Better output, needs to be streaming, can overload it by providing large range
     * Querying summary information (a.k.a statistical information) for a set of stream ids
     * Parameter:
     * sid=[List of stream ids]
     * sd= start date, UK format , 30-01-2012
     * ed= end date, UK format
     */
    (EntityIdList(params.get("sid")),DateParam(params.get("sd")),DateParam(params.get("ed"))) match {
      case (sids,Some(sd),Some(ed)) =>
        if(sids.map(permissionCheck).forall(_.isDefined)){
          val keys:List[String] = new StorageStreamDayIdGenerator(sids.map(_.toString).toSeq,Utils.yyyyDDDFormat.print(sd*1000L),Utils.yyyyDDDFormat.print(ed*1000L)).toList
          generate(keys.zip(Cache.stat.mget(keys.head,keys.tail: _*).get).toMap)
        }else
          halt()
      case others => haltMsg()
    }
  }
}
