package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
	
	def index = Action {
		Ok(views.html.index("Your new application is ready."))
	}
	def sample = Action {
		Ok(views.html.sample())
	}

	def add_sensor_data(key:String) = Action { req =>
		Ok("Hello " + key + "!")
	}
	
}

case class MeasurementUnit(id:Int, name:String, link:String, description:String)
case class MetaData(id:Int, from:Long,to:Long,shortDescription:String,longDescription:String)

case class Analysis(id:Int, name:String,widgets:List[WidgetInstance])
case class WidgetInstance(id:Int, title:String,widget_id:Int,config:String)
case class Widget(author_id:String, sample_config:String)
case class Stream(id:Int, name:String,description:String,key:String,unit:MeasurementUnit,metadata:List[MetaData])
case class Node(id:Int,name:String,latitude:Double,altitude:Double,longitude:Double,description:String,streams:List[Stream],metadata:List[MetaData])
case class Experiment(id:Int, name:String,description:String,nodes:List[Node],metadata:List[MetaData])
case class User(id:Int, user_name:String,password:String, super_key:String,experiments:List[Experiment],analysis:List[Analysis])