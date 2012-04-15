package au.csiro.ict

import org.scalatra.ScalatraServlet
import com.mongodb.casbah.Imports._
import com.codahale.jerkson.Json._
import au.csiro.ict.JsonGenerator.generate

trait RestfulDataAccess {

  self:ScalatraServlet with RestfulHelpers=>

//  val ips = InputProcessingBackend.ips
//  val ips = new InputProcessingSystemProxy

  post("/data"){
    // to insert new time series data items
    // [[token,ts,value],[token,ts,value]]
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
          val allKeysMapped = Cache.Streams.find(MongoDBObject("token"->MongoDBObject("$in"->packed.keys.toArray)),MongoDBObject("_id"->1,"token"->1)).map(x=>x("token").toString->x("_id").toString).map{x=>
            x._2 -> packed(x._1)
          }.toMap

          if(allKeysMapped.size != packed.size)
            haltMsg("Bad request, invalid security tokens")

          allKeysMapped.foreach{item=>
            val qName=Utils.inputQueueIdFor(item._1)
            Cache.queue.lpush(qName,generate(item._2))
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
