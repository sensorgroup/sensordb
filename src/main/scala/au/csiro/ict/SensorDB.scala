package au.csiro.ict

import org.scalatra._
import scalate.ScalateSupport

import org.scalatra.fileupload.FileUploadSupport
import io.Source
import java.util.Properties
import java.io.FileReader
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import org.apache.commons.fileupload.servlet.ServletFileUpload
import scala.{None, Option}
import org.mindrot.jbcrypt.BCrypt
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.Imports._
import au.csiro.ict.Validators._
import org.bson.types.ObjectId
import javax.servlet.http.HttpSession
import Cache._

class Controller extends ScalatraServlet  with FileUploadSupport with FlashMapSupport
  with RestfulUsers
  with RestfulExperiments
  /* with ScalateSupport */
  with RestfulNodes
  with RestfulStreams
  with RestfulMeasurements
  with RestfulHelpers{

  notFound {
    //    findTemplate(requestPath) map {
    //      path =>
    //        contentType = "text/html"
    //        layoutTemplate(path)
    //    } orElse
    serveStaticResource() getOrElse resourceNotFound()
  }
}
