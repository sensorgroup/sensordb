package au.csiro.ict

import au.csiro.ict.Cache.Measurements
import org.scalatra.ScalatraServlet
import au.csiro.ict.JsonGenerator.generate

trait RestfulMeasurements {
  self:ScalatraServlet with RestfulHelpers=>

  get("/measurements"){
    generate(Measurements.find())
  }
}
