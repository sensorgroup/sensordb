package au.csiro.ict

import org.scalatra._
import scalate.ScalateSupport
import grizzled.slf4j.Logger
import com.codahale.jerkson.Json._
import io.Source
import java.util.Properties
import java.io.FileReader
import scala.collection.JavaConversions._

object Configuration{
  private val config = new Properties()
  config.load(getClass.getResourceAsStream("/config.properties"))
  val config_map = config.entrySet().map(x=>(x.getKey.toString.trim().toLowerCase,x.getValue.toString.trim())).toMap[String, String]
  def apply(name:String):Option[String]= config_map.get(name.toLowerCase.trim())
}

class Controller extends ScalatraServlet with ScalateSupport {
  private val logger: Logger = Logger[this.type]
  get("/") {
    Configuration("redis.queue.port")
//    Json.parse(Source.fromFile("config.json"))
//    generate(Configuration("1",1,"2",2,"3",3,"4",4))
      
  }

  notFound {
    findTemplate(requestPath) map {
      path =>
        contentType = "text/html"
        layoutTemplate(path)
    } orElse serveStaticResource() getOrElse resourceNotFound()
  }
}
