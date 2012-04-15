package au.csiro.ict

import org.scalatra.ScalatraServlet
import com.mongodb.casbah.Imports._
import au.csiro.ict.JsonGenerator.generate

trait RestfulDataAccess {
  self:ScalatraServlet with RestfulHelpers=>

  post("/data"){

  }

}
