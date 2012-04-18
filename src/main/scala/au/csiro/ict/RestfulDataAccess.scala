package au.csiro.ict

import org.scalatra.ScalatraServlet
import com.mongodb.casbah.Imports._
import com.codahale.jerkson.Json._
import au.csiro.ict.JsonGenerator.generate

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

}
