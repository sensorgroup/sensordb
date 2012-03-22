package au.csiro.ict

import org.scalatra._
import scalate.ScalateSupport
import grizzled.slf4j.Logger
import com.codahale.jerkson.Json._
import io.Source
import java.util.Properties
import java.io.FileReader

object Configuration{
  private val config = new Properties()
  config.load(getClass.getResourceAsStream("/config.properties"))
  
  def apply(name:String):Option[String]= {
    config.getProperty(name) match {
      case v:String=>Some(v)
      case null=>None
    }
    
  }
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
