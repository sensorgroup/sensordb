package au.csiro.ict

import au.csiro.ict.Validators._
import org.scalatra.ScalatraServlet
import au.csiro.ict.Cache._
import com.mongodb.casbah.Imports._
import au.csiro.ict.Validators._
import scala.collection.JavaConversions._
import au.csiro.ict.JsonGenerator.generate

trait RestfulExperiments {
  self:ScalatraServlet with RestfulHelpers=>

  delete("/experiments"){
    //TODO: Test cascading deletes
    (UserSession(session),EntityId(params.get("eid"))) match{
      case (Some((uid,userName)),Some(expId))=>
        delExperiment(uid, expId)
        val failed=Experiments.findOne(Map("uid"->uid,"_id"->expId)).isDefined
        if(failed)
          haltMsg("Delete Failed")
        else
          halt(200,"Delete succeeded")
      case errors=>haltMsg()
    }
  }
  //
  //  //todo: test that experiment information with private flag is not available through session
  post("/experiments"){
    // Add a new experiments
    (UserSession(session),Name(params.get("name")),Description(params.get("description")),TimeZone(params.get("timezone")),WebUrl(params.get("website")),PictureUrl(params.get("picture")),Privacy(params.get("public_access"))) match{
      case (Some((uid,user_name)),Some(name),Some(description),Some(timezone),Some(website),Some(picture),Some(public_access)) if UniqueName(Experiments,"name"->name,"uid"->uid)=>
        addExperiment(name, uid, timezone, public_access, picture, website, description)
        generate(Experiments.findOne(Map("uid"->uid,"name"->name)))
      case error=>haltMsg("Creating a new experiment failed")
    }
  }

  put("/experiments"){
    // update/replace an experiment information
    val validators:Map[String,()=>Option[String]] = Map(
      "website"-> (()=>WebUrl(params.get("value"))),
      "name"-> (()=> Name(params.get("value"))),
      "timezone"-> (()=> TimeZone(params.get("value"))),
      "description"-> (()=> Description(params.get("value"))),
      "picture"-> (()=> PictureUrl(params.get("value"))),
      "access_restriction"-> (()=> Privacy(params.get("value"))))

    (UserSession(session),EntityId(params.get("eid")),params.get("field").filter(validators.keys.contains),params.get("value")) match {
      case (Some((uid,userName)),Some(eid),Some(field),Some(value)) if validators(field).apply().isDefined && (!(field.equals("name")) || UniqueName(Experiments,"name"->value,"uid"->uid)) =>
        Experiments.findAndModify(Map("uid"->uid,"_id"->eid),$set(field->value,"updated_at"->System.currentTimeMillis()))
        generate(Experiments.findOne(Map("uid"->uid,"_id"->eid)))
      case errors => haltMsg()
    }
  }
}
