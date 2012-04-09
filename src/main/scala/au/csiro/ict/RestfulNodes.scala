package au.csiro.ict

import au.csiro.ict.Cache._
import com.mongodb.casbah.Imports._
import au.csiro.ict.Validators._
import org.scalatra.ScalatraServlet
import scala.collection.JavaConversions._

trait RestfulNodes {
  self:ScalatraServlet with RestfulHelpers=>

  import SDBSerializer.generate

  get("/nodes"){
    // List the nodes

  }
  delete("/nodes"){
    (UserSession(session),EntityId(params.get("nid"))) match {
      case (Some((uid,userName)),Some(nid))=>
        Nodes.remove(Map("uid"->uid,"_id"->nid))
        if(Nodes.findOne(Map("uid"->uid,"_id"->nid)).isDefined)
          haltMsg("Delete Failed")
        else
          halt(200,"Delete succeeded")
      case errors => haltMsg()
    }
  }


  put("/nodes"){
    // update/replace an nodes information
    val validators:Map[String,()=>Option[_]] = Map(
      "name"-> (()=> Name(params.get("value"))),
      "lat"-> (()=> LatLonAlt(params.get("value"))),
      "alt"-> (()=> LatLonAlt(params.get("value"))),
      "lon"-> (()=> LatLonAlt(params.get("value"))),
      "description"-> (()=> Description(params.get("value"))),
      "picture"-> (()=> PictureUrl(params.get("value"))),
      "website"-> (()=>WebUrl(params.get("value"))),
      "eid"-> (()=> EntityId(params.get("value"))))

    (UserSession(session),EntityId(params.get("nid")),ExperimentIdFromNodeId(EntityId(params.get("nid"))),params.get("field").filter(validators.keys.contains),params.get("value")) match {
      case (Some((uid,userName)),Some(nid),Some(eid),Some(field),Some(value)) if validators(field).apply().isDefined && ((field!= "name") || UniqueName(Nodes,"name"->value,"eid"->eid)) && (field!="eid" || OwnedBy(Experiments,uid,validators(field).apply().get.asInstanceOf[ObjectId])) =>
        val toSet = if(field == "eid") validators(field).apply().get.asInstanceOf[ObjectId] else value
        Nodes.findAndModify(Map("uid"->uid,"_id"->nid),$set(field->toSet,"updated_at"->System.currentTimeMillis()))
        generate(Nodes.findOne(Map("uid"->uid,"_id"->nid)))
      case errors => haltMsg()
    }
  }

  post("/nodes"){
    // Create a new node
    (UserSession(session),Name(params.get("name")),EntityId(params.get("eid")),LatLonAlt(params.get("lat")),LatLonAlt(params.get("lon")),LatLonAlt(params.get("alt")),Description(params.get("description")),WebUrl(params.get("website")),PictureUrl(params.get("picture"))) match{
      case (Some((uid,user_name)),Some(name),Some(eid),Some(lat),Some(lon),Some(alt),Some(description),Some(website),Some(picture)) if UniqueName(Nodes,"name"->name,"eid"->eid)=>
        Nodes.insert(Map("name"->name,"uid"->uid,"eid"->eid,"lat"->lat,"lon"->lon,"alt"->alt,
          "picture"->picture,"website"->website, "token"->Utils.uuid(),
          "updated_at"->System.currentTimeMillis(),
          "created_at"->System.currentTimeMillis(),
          "description"->description))
        generate(Nodes.findOne(Map("uid"->uid,"name"->name,"eid"->eid)))
      case error=>haltMsg()
    }

  }

}
