package au.csiro.ict

import org.bson.types.ObjectId
import org.scalatra.ScalatraServlet
import au.csiro.ict.JsonGenerator.generate
import au.csiro.ict.Validators._
import au.csiro.ict.Cache._
import com.mongodb.casbah.query.BSONType.DBObject
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

  val protectedFields = Map("token"->0,"password"->0,"email"->0)

  def sendSession()={
    val current:Option[ObjectId] = UserSession(session).map(_._1)
    val user:Option[ObjectId] = params.get("user").filterNot(_.trim.isEmpty).flatMap{uname=>
      Users.findOne(MongoDBObject("name"->uname),MongoDBObject("_id"->1))
    }.flatMap(x=>x.getAs[ObjectId]("_id"))
    val fields = if (current.exists(x=>user.isEmpty || x.equals(user.get)))
      Map("password"->0)
    else
      protectedFields
    user.orElse(current).flatMap((u:ObjectId)=>Users.findOne(MongoDBObject("_id"->u),fields)).map{user=>
      val uid = user._id.get
      user.put("_id",uid.toString)
      generate(Map("user"->user,
        "experiments"->Experiments.find(MongoDBObject("uid"->uid),fields), //.map((o:DBObject)=> o.toMap+("_id"->o("_id").toString)),
        "nodes"->Nodes.find(MongoDBObject("uid"->uid),fields),
        "streams"->Streams.find(MongoDBObject("uid"->uid),fields)))
    }.getOrElse("{}")
  }

}
