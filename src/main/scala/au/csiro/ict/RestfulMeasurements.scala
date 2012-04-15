package au.csiro.ict

import au.csiro.ict.Cache.Measurements
import org.scalatra.ScalatraServlet
import com.mongodb.casbah.Imports._
import au.csiro.ict.JsonGenerator.generate

trait RestfulMeasurements {
  self:ScalatraServlet with RestfulHelpers=>

  get("/measurements"){
    generate(Measurements.find())
  }
}
