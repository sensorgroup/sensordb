package au.csiro.ict

import org.scalatra.ScalatraServlet
import au.csiro.ict.Cache._
import com.mongodb.casbah.Imports._
import au.csiro.ict.Validators._
import scala.collection.JavaConversions._
import au.csiro.ict.JsonGenerator.generate

trait RestfulExperiments {
  self:ScalatraServlet with RestfulHelpers=>

  delete("/experiments"){
    params.foreach(x=>println(x._1,x._2))
    println("---------------")
    (UserSession(session),EntityId(params.get("eid"))) match{
      case (Some((uid,userName)),Some(expId))=>
        delExperiment(uid, expId)
        val failed=Experiments.findOne(Map("uid"->uid,"_id"->expId)).isDefined
        halt(200,"Delete succeeded")
      case errors=>haltMsg()
    }
  }

  post("/experiments"){
    // Add a new experiments
    (UserSession(session),Required(Name(params.get("name")),"name parameter is missing"),Description(params.get("description")),TimeZone(params.get("timezone")),WebUrl(params.get("website")),PictureUrl(params.get("picture")),Privacy(params.get("public_access"))) match{
      case (Some((uid,user_name)),Some(name),Some(description),Some(timezone),Some(website),Some(picture),Some(public_access)) if UniqueName(Experiments,"name"->name,"uid"->uid)=>
        addExperiment(name, uid, timezone, public_access, picture, website, description)
        generate(Experiments.findOne(Map("uid"->uid,"name"->name)))
      case error=>haltMsg("Creating a new experiment failed")
    }
  }

  put("/experiments"){
    // update/replace an experiment information
    val validators:Map[String,()=>Option[_]] = Map(
      "name"-> (()=> Required(Name(params.get("value")),"name parameter is missing")),
      "website"-> (()=>WebUrl(params.get("value"))),
      "description"-> (()=> Description(params.get("value"))),
      "picture"-> (()=> PictureUrl(params.get("value"))),
      "access_restriction"-> (()=> Privacy(params.get("value"))))

    val user_session = UserSession(session)
    (user_session,ObjectOwnershipCheck(EntityId(params.get("eid")),user_session),params.get("field").filter(validators.keys.contains),params.get("value")) match {
      case (Some((uid,userName)),Some(eid),Some(field),Some(value)) if validators(field).apply().isDefined && (!(field.equals("name")) || UniqueName(Experiments,"name"->value,"uid"->uid)) =>
        Experiments.findAndModify(Map("uid"->uid,"_id"->eid),$set(field->value,"updated_at"->System.currentTimeMillis()))
        generate(Experiments.findOne(Map("uid"->uid,"_id"->eid)))
      case errors => haltMsg()
    }
  }
}
