package au.csiro.ict


import au.csiro.ict.Validators._
import org.bson.types.ObjectId
import org.scalatra.ScalatraServlet
import au.csiro.ict.Cache._
import com.mongodb.casbah.Imports._
import scala.collection.JavaConversions._
import au.csiro.ict.JsonGenerator.generate

trait RestfulStreams {
  self:ScalatraServlet with RestfulHelpers=>

  delete("/streams"){
    (UserSession(session),EntityId(params.get("sid")),EntityId(params.get("eid")),EntityId(params.get("nid"))) match {
      case (Some((uid,userName)),Some(sid),Some(eid),Some(nid))=>
        delStream(uid, sid)
        if(Streams.findOne(Map("uid"->uid,"_id"->sid)).isDefined)
          haltMsg("Delete Failed")
        else
          halt(200,"Delete succeeded")

      case errors => haltMsg()
    }
  }

  put("/streams"){
    // update/replace stream information
    val validators:Map[String,()=>Option[_]] = Map(
      "name"-> (()=> Name(params.get("value"))),
      "mid"-> (()=> MeasurementId(EntityId(params.get("value")))),
      "description"-> (()=> Description(params.get("value"))),
      "picture"-> (()=> PictureUrl(params.get("value"))),
      "website"-> (()=>WebUrl(params.get("value"))),
      "nid"-> (()=> EntityId(params.get("value"))))

    (UserSession(session),EntityId(params.get("sid")),NodeIdFromStreamId(EntityId(params.get("nid"))),params.get("field").filter(validators.keys.contains),params.get("value")) match {
      case (Some((uid,userName)),Some(sid),Some(nid),Some(field),Some(value)) if validators(field).apply().isDefined && ((field!= "name") || UniqueName(Streams,"name"->value,"nid"->nid)) && (field!="nid" || OwnedBy(Cache.Nodes,uid,validators(field).apply().get.asInstanceOf[ObjectId])) =>
        val toSet = if(field == "nid" || field=="mid") validators(field).apply().get.asInstanceOf[ObjectId] else value
        Streams.findAndModify(Map("uid"->uid,"_id"->nid),$set(field->toSet,"updated_at"->System.currentTimeMillis()))
        generate(Streams.findOne(Map("uid"->uid,"_id"->nid)))
      case errors => haltMsg()
    }
  }

  post("/streams"){
    // Add a new stream
    (UserSession(session),Name(params.get("name")),MeasurementId(EntityId(params.get("mid"))),EntityId(params.get("nid")),Description(params.get("description")),WebUrl(params.get("website")),PictureUrl(params.get("picture"))) match{
      case (Some((uid,user_name)),Some(name),Some(mid),Some(nid),Some(description),Some(website),Some(picture)) if UniqueName(Streams,"name"->name,"nid"->nid) && OwnedBy(Nodes,uid,nid) =>
        addStream(name, uid, nid, mid, picture, website, description)
        generate(Streams.findOne(Map("nid"->nid,"name"->name,"uid"->uid)))
      case error=>haltMsg()
    }

  }

}
