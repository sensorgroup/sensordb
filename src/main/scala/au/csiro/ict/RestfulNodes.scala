package au.csiro.ict

import au.csiro.ict.Cache._
import com.mongodb.casbah.Imports._
import au.csiro.ict.Validators._
import org.scalatra.ScalatraServlet
import scala.collection.JavaConversions._
import au.csiro.ict.JsonGenerator.generate

trait RestfulNodes {
  self:ScalatraServlet with RestfulHelpers=>

  delete("/nodes"){
    (UserSession(session),EntityId(params.get("nid"))) match {
      case (Some((uid,userName)),Some(nid))=>
        delNode(uid, nid)
        halt(200,"Delete succeeded")
      case errors => haltMsg()
    }
  }


  put("/nodes"){
    // update/replace an nodes information
    val validators:Map[String,()=>Option[_]] = Map(
      "name"-> (()=> Required(Name(params.get("value")),"Name parameter is missing")),
      "lat"-> (()=> LatLonAlt(params.get("value"))),
      "alt"-> (()=> LatLonAlt(params.get("value"))),
      "lon"-> (()=> LatLonAlt(params.get("value"))),
      "description"-> (()=> Description(params.get("value"))),
      "picture"-> (()=> PictureUrl(params.get("value"))),
      "website"-> (()=>WebUrl(params.get("value"))),
      "eid"-> (()=> EntityId(params.get("value"))))

    val user_session = UserSession(session)
    (user_session,ObjectOwnershipCheck(EntityId(params.get("nid")),user_session),ExperimentIdFromNodeId(EntityId(params.get("nid"))),params.get("field").filter(validators.keys.contains),params.get("value")) match {
      case (Some((uid,userName)),Some(nid),Some(eid),Some(field),Some(value)) if validators(field).apply().isDefined && ((field!= "name") || UniqueName(Nodes,"name"->value,"eid"->eid)) && (field!="eid" || OwnedBy(Experiments,uid,validators(field).apply().get.asInstanceOf[ObjectId])) =>
        val toSet = if(field == "eid") validators(field).apply().get.asInstanceOf[ObjectId] else value
        Nodes.findAndModify(Map("uid"->uid,"_id"->nid),$set(field->toSet,"updated_at"->System.currentTimeMillis()))
        generate(Nodes.findOne(Map("uid"->uid,"_id"->nid)))
      case errors => haltMsg()
    }
  }


  post("/nodes"){
    // Create a new node
    (UserSession(session),Required(Name(params.get("name")),"name parameter is missing"),EntityId(params.get("eid")),LatLonAlt(params.get("lat")),LatLonAlt(params.get("lon")),LatLonAlt(params.get("alt")),Description(params.get("description")),WebUrl(params.get("website")),PictureUrl(params.get("picture"))) match{
      case (Some((uid,user_name)),Some(name),Some(eid),Some(lat),Some(lon),Some(alt),Some(description),Some(website),Some(picture)) if UniqueName(Nodes,"name"->name,"eid"->eid)=>
        addNode(name, uid, eid, lat, lon, alt, picture, website, description)
        generate(Nodes.findOne(Map("uid"->uid,"name"->name,"eid"->eid)))
      case error=>haltMsg()
    }

  }

}
