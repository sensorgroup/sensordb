package au.csiro.ict

import org.scalatra.ScalatraServlet
import au.csiro.ict.JsonGenerator._
import au.csiro.ict.Cache._
import au.csiro.ict.Validators._
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject

/**
 * Metadata key is limited to 30 characters, minimum 1, no space in the middle
 * Metadata value is 0 to 30 characters, can have any text
 */
trait RestfulMetadata {
  self:ScalatraServlet with RestfulHelpers=>

  get("/metadata/add"){
    // Session,  Id, start ts, end ts,name ,value, description

    (UserSession(session),
      EntityId(params.get("id")),
      IntParam(params.get("start-ts")),
      IntParam(params.get("end-ts")),
      PatternMatch(params.get("name"),METADATA_NAME_REGEX),
      Description(params.get("value")),
      Description(params.get("description"))) match {
      case (Some((uid,userName)),Some(oid),startTs,endTs,Some(name),Some(value),description) =>
        val toInsert = Map("value"->value.slice(0,30),"updated_at"->System.currentTimeMillis(),"updated_by"->userName) ++ startTs.map("start-ts"->_) ++ endTs.map("end-ts"->_) ++ description.map("description"-> _ )

        val modify = MongoDBObject("$set"->MongoDBObject(("metadata."+name)->MongoDBObject(toInsert.toSeq :_*)))
        val search = MongoDBObject("uid"->uid,"_id"->oid)
        val result = Experiments.findAndModify(search,modify).orElse(Nodes.findAndModify(search,modify)).orElse(Streams.findAndModify(search,modify))
        if (result.isDefined)
          halt(200)
        else
          haltMsg("Adding meta data failed")
      case missingParams=> haltMsg("Invalid user session")
    }
  }

  get("/metadata/remove"){
    // Session,  Id, name
    (UserSession(session),
      EntityId(params.get("id")),
      PatternMatch(params.get("name"),METADATA_NAME_REGEX)) match {
      case (Some((uid,userName)),Some(oid),Some(name)) =>
        val modify = MongoDBObject("$unset"->MongoDBObject(("metadata."+name)->1))
        val search = MongoDBObject("uid"->uid,"_id"->oid)
        val result = Experiments.findAndModify(search,modify).orElse(Nodes.findAndModify(search,modify)).orElse(Streams.findAndModify(search,modify))
        if (result.isDefined)
          halt(200)
        else
          haltMsg("Removing a meta data item failed")
      case missingParams=> haltMsg("Missing arguments, id and name are required. This can be only called by authenticated users")
    }
  }

  get("/metadata/retrieve/:id"){
    val fields = MongoDBObject("metadata"->1)
    EntityId(params.get("id")) match {
      case Some(oid)=>
        Experiments.findOneByID(oid,fields).orElse(Nodes.findOneByID(oid,fields)).orElse(Streams.findOneByID(oid,fields)).map(_.get("metadata")).filter(_!=null).map(_.asInstanceOf[DBObject].toMap) match {
          case Some(result) if !result.isEmpty=>  generate(result)
          case notFound => halt(200,"{}")
        }
      case invalidId=>halt(200,"{}")
    }
  }
}
