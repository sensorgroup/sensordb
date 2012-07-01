package au.csiro.ict

import org.scalatra._
import org.scalatra.fileupload.FileUploadSupport
import java.util

class SensorDB extends ScalatraServlet
with FileUploadSupport
with FlashMapSupport
with RestfulDataAccess
with RestfulUsers
with RestfulUserRegistration
with RestfulExperiments
/* with ScalateSupport */
with RestfulNodes
with RestfulStreams
with RestfulMeasurements
with RestfulMetadata
with RestfulHelpers
with Logger{

  util.TimeZone.setDefault(util.TimeZone.getTimeZone("UTC"))

  notFound {
    //    findTemplate(requestPath) map {
    //      path =>
    //        contentType = "text/html"
    //        layoutTemplate(path)
    //    } orElse
    serveStaticResource() getOrElse resourceNotFound()
  }
}
