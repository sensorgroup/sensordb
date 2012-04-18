package au.csiro.ict

import org.scalatra.ScalatraServlet
import com.mongodb.casbah.Imports._
import com.codahale.jerkson.Json._
import au.csiro.ict.JsonGenerator.generate
import au.csiro.ict.Cache.store
import au.csiro.ict.Validators._

trait RestfulDataAccess {

  self:ScalatraServlet with RestfulHelpers=>

  val ips = InputProcessingBackend.ips
  //  val ips = new InputProcessingSystemProxy // activate this line for distributed message processing
  post("/data"){
    // to insert new time series data items
    // looking for data parameter with a string value with the following format of [[token,ts,value],[token,ts,value]]
    params.get("data") match {
      case Some(s:String) if !s.trim.isEmpty =>
        try{
          val data = parse[List[List[String]]](s)
          val packed:Map[String,Map[Long,Double]]=data.foldLeft(Map[String,Map[Long,Double]]()){(sum,item)=>
            val key =item(0)
            sum + (key->(sum.get(key).getOrElse(Map[Long,Double]()) + (item(1).toLong->item(2).toDouble)))
          }
          if(!packed.keys.forall(Utils.keyPatternMatcher))
            haltMsg("Bad request, invalid tokens")

          val allKeysMapped = Cache.Streams.find(MongoDBObject("token"->MongoDBObject("$in"->packed.keys.toArray)),MongoDBObject("_id"->1,"token"->1,"nid"->1)).map{x=>
            val token = x("token").toString
            val sid=x("_id").toString
            val nid=x("nid").toString
            Utils.inputQueueIdFor(nid,sid) -> packed(token)
          }.toMap

          if(allKeysMapped.size != packed.size)
            haltMsg("Bad request, invalid security token(s)")

          allKeysMapped.foreach{item=>
            Cache.queue.lpush(item._1,generate(item._2))
            ips.process(Task(item._1))
          }

          generate(Map("length"->data.size))
        } catch {
          case e:java.lang.Exception =>
            haltMsg("Bad input, can't parse the data parameter, please verify the body of your request")
        }
      case missing => haltMsg("Bad input, missing parameter data from our request")
    }
  }

  get("/data"){
    // querying data from a stream
    // parameters
    // sid => sensor id as String convertable to ObjectId on MongoDB
    // sd => start date => UK format => 30/01/2012
    // ed => end date => uk format
    // st => start time => 22:03
    // et => end time => 23:01
    (EntityId(params.get("sid")),DateParam(params.get("sd")),TimeParam(params.get("st")),DateParam(params.get("ed")),TimeParam(params.get("et"))) match {
      case (Some(sid),Some(sd),Some(st),Some(ed),Some(et)) => Cache.Streams.findOne(MongoDBObject("_id"->sid),MongoDBObject("nid"->1,"uid"->1)) match {
        case Some(s:DBObject) => Cache.Nodes.findOne(MongoDBObject("_id"->s.getAs[ObjectId]("nid").get),MongoDBObject("eid"->1)) match {
          case Some(n:DBObject)=> Cache.Experiments.findOne(MongoDBObject("_id"->n.getAs[ObjectId]("eid").get),MongoDBObject(Cache.ACCESS_RESTRICTION_FIELD->1)) match {
            case Some(e:DBObject)=>
              if (e.getAs[Int](Cache.ACCESS_RESTRICTION_FIELD) == Cache.EXPERIMENT_ACCESS_PUBLIC || {
                val current:Option[ObjectId] = UserSession(session).map(_._1)
                current.isDefined && current.get == s.getAs[ObjectId]("uid")
              }) {
                store.queryNode(s.getAs[ObjectId]("nid").toString,new KeyListIterator(List(sid.toString),sd,ed),Some(st->et),new DefaultChunkFormatter(new JSONWriter(response.getWriter)))
              }
            case other => haltMsg("Bad input, access denied, please verify the body of your request")
          }
          case other => haltMsg("Bad input, missing experiment, please verify the body of your request")
        }
        case other => haltMsg("Bad input, missing node, please verify the body of your request")
      }
      case other => haltMsg("Bad input, missing stream, please verify the body of your request")
    }
  }

}
