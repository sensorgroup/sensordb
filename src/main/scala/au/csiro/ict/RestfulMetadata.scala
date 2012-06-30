package au.csiro.ict

import org.scalatra.ScalatraServlet
import au.csiro.ict.JsonGenerator._
import au.csiro.ict.Cache._
import au.csiro.ict.Validators._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons
import com.mongodb

//import com.mongodb.casbah.query.Imports._

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
        val toInsert = Map("name"->name,
          "value"->value.slice(0,30),
          "updated_at"->System.currentTimeMillis(),
          "updated_by"->userName) ++ startTs.map("start-ts"->_) ++ endTs.map("end-ts"->_) ++ description.map("description"-> _ )

        val search = Map("uid"->uid,"_id"->oid)
        val remove = Map("$pull"->Map("metadata"->Map("name"->name)))
        val insert = Map("$push"->Map("metadata"->Map(toInsert.toSeq :_*)))
        List(Experiments,Nodes,Streams).foreach{x=>
          x.update(search,remove)
          x.update(search,insert)
          x.update(search,Map("$set"->Map("changed"->"yes")))
        }
      case missingParams=> haltMsg("Invalid user session")
    }
  }

  get("/metadata/remove"){
    // Session,  Id, name
    (UserSession(session),
      EntityId(params.get("id")),
      PatternMatch(params.get("name"),METADATA_NAME_REGEX)) match {
      case (Some((uid,userName)),Some(oid),Some(name)) =>
        val remove = Map("$pull"->Map("metadata"->Map("name"->name)))
        val search = Map("uid"->uid,"_id"->oid)
        List(Experiments,Nodes,Streams).foreach{x=>x.update(search,remove)}
      case missingParams=> haltMsg("Missing arguments, id and name are required. This can be only called by authenticated users")
    }
  }

  get("/metadata/retrieve/:id"){
    val fields = Map("metadata"->1)
    EntityId(params.get("id")) match {
      case Some(oid)=>
        Experiments.findOneByID(oid,fields).orElse(Nodes.findOneByID(oid,fields)).orElse(Streams.findOneByID(oid,fields)).map(_.getAs[BasicDBList]("metadata").toList) match {
          case Some(result) if !result.isEmpty=>  generate(result.head)
          case notFound => halt(200,"{}")
        }
      case invalidId=>halt(200,"{}")
    }
  }

  def generateMetadataKeys(filter:Map[String,_]):String={
    generate(List(Experiments,Nodes,Streams).flatMap(_.find((filter),Map("metadata"->1))
      .flatMap(_.getAs[MongoDBList]("metadata").getOrElse(MongoDBList()).map(x=>x.asInstanceOf[BasicDBObject].getString("name")))).toSet)
  }

  def generateMetadataKeyValues(uid:Option[ObjectId]):String=
    generate(List(Experiments,Nodes,Streams).flatMap(_.find(Map()++uid.map("uid"->_),Map("metadata"->1))
      .flatMap(x=>x.getAs[MongoDBList]("metadata").getOrElse(MongoDBList()).map{x=>
      val item = x.asInstanceOf[mongodb.BasicDBObject]
      item.get("name") ->item.get("value")
    })).groupBy(_._1).mapValues(_.map(_._2)).toMap)

  def generateMetadataValues(uid:Option[ObjectId]):String=
    generate(List(Experiments,Nodes,Streams).flatMap(_.find(Map("metadata.name"->params("key"))++uid.map("uid"->_),Map("metadata"->1))
      .flatMap(x=>x.getAs[MongoDBList]("metadata").getOrElse(MongoDBList()).map{x=>
      val item = x.asInstanceOf[mongodb.BasicDBObject]
      if (item.get("name") == params("key"))
        Some(item.get("value"))
      else
        None
    }.filter(_ != None))).toSet)

  get("/metadata/keys/:userid"){
    generateMetadataKeys(Map("uid"->new ObjectId(params("userid"))))
  }
  get("/metadata/keys"){
    generateMetadataKeys(Map())
  }
  get("/metadata/keyvalues"){
    generateMetadataKeyValues(None)
  }
  get("/metadata/values/:key"){
    generateMetadataValues(None)
  }
  get("/metadata/values/:key/:userid"){
    generateMetadataValues(params.get("userid").map(uid=>new ObjectId(uid)))
  }
}
