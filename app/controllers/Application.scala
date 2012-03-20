package controllers

import play.api._
import data.Form
import libs.Crypto
import play.api.mvc._
import org.mindrot.jbcrypt.BCrypt
import play.api.data._
import play.api.data.Forms._
import views._
import models._
import com.codahale.jerkson.Json._
import redis.clients.jedis.Jedis
import sensordb.Utils


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

//object JsonActions extends Controller{

//  val loginForm = Form(mapping(
//    "username" -> nonEmptyText,
//    "password" -> nonEmptyText
//  )(LoginUserRequest.apply)(LoginUserRequest.unapply)
//    verifying ("Invalid username or password", result => result match {
//    case input:LoginUserRequest => false
//  }))
//
//  def authenticate = Action{ implicit request=>
//
//      Ok("")
//    else {
//      loginForm.bindFromRequest.fold({formWithErrors =>
//        Ok("Error !"+Utils.uuid)
//        // if there is a session active, don't proceed
//      },{value:LoginUserRequest =>
//        val token = Utils.uuid()
//        session("sensordb",token)
//        Utils.jCache.hset(token,"uid",1234)
//
//        Ok("Hello !")
//      })
//    }
//  }

//  def logout = Action{implicit request => Ok("").withNewSession }
//
//  def isAuthenticated(sessionId:String) = true
//}

