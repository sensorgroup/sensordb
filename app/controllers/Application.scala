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

case class MeasurmentUnit(pk:Integer, name:String, link:String, shortDescription:String)
case class MetaData(from:Long,to:Long,shortDescription:String,longDescription:String)
case class Stream(name:String,description:String,key:String,unit:MeasurmentUnit,stream_id:String,metadata:List[MetaData])
case class Node(name:String,latitude:Double,altitude:Double,longitutde:Double,description:String,streams:List[Stream],metadata:List[MetaData])
case class Experiment(name:String,description:String,nodes:List[Node],metadata:List[MetaData])
case class Analysis(name:String,widgets:List[WidgetInstance])
case class WidgetInstance(id:String, title:String,widget_id:Int,config:String)
case class Widget(author_id:String, sample_config:String)
case class User(user_name:String,password:String, super_key:String,experiments:List[Experiment],analysis:List[Analysis])