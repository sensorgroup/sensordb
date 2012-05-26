package au.csiro.ict

import org.bson.types.ObjectId
import org.scalatra.ScalatraServlet
import au.csiro.ict.JsonGenerator.generate
import au.csiro.ict.Validators._
import au.csiro.ict.Cache._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._

trait RestfulHelpers {
  self:ScalatraServlet =>

  implicit def errors:Validator = request.getAttribute("__errors") match {
    case v:Validator =>v
    case others =>
      val validator = new Validator()
      request.setAttribute("__errors",validator)
      validator
  }

  def haltMsg(newErrorMessage:String=null) = {
    if(newErrorMessage!=null)
      errors.addError(newErrorMessage)
    if (!errors.errors.isEmpty)
      halt(400,generate(Map("errors"->errors.errors.reverse)))
    None
  }

  def sendSession()={
    val (user_session:Option[ObjectId],user:Option[ObjectId])=(UserSession(session).map(_._1),Name(params.get("user")).flatMap(uname=>Users.findOne(MongoDBObject("name"->uname,"active"->true),MongoDBObject("_id"->1)).map(_._id.get)))

    val requested_user = user.orElse(user_session)
    val ownerRequest = requested_user.filter(x=>user_session.isDefined && user_session.get == x).isDefined

    val fields = if (ownerRequest)
      Map("password"->0)
    else
      Map("token"->0,"password"->0,"email"->0)

    requested_user.flatMap((u:ObjectId)=>Users.findOne(MongoDBObject("_id"->u),fields)).map{user=>
      val uid = user._id.get
      val experiments = (if (ownerRequest)
        Experiments.find(MongoDBObject("uid"->uid),fields)
      else
        Experiments.find(MongoDBObject("uid"->uid,Cache.ACCESS_RESTRICTION_FIELD->Cache.EXPERIMENT_ACCESS_PUBLIC),fields)).toList


      val nodes = Nodes.find("eid" $in experiments.map(_._id.get).toList,fields).toList
      val streams = Streams.find("nid" $in nodes.map(_._id.get).toList,fields)

      generate(Map("user"->user,
        "experiments"->experiments,
        "nodes"->nodes,
        "streams"->streams))
    }.getOrElse("{}")
  }

}
