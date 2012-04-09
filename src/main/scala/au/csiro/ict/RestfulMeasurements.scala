package au.csiro.ict

import au.csiro.ict.Cache.Measurements
import org.scalatra.ScalatraServlet
import com.mongodb.casbah.Imports._


trait RestfulMeasurements {
  self:ScalatraServlet with RestfulHelpers=>

  import SDBSerializer.generate

  get("/measurements"){
    generate(Measurements.find())
  }
}
