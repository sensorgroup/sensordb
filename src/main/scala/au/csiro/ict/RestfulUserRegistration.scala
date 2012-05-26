package au.csiro.ict

import au.csiro.ict.Validators._
import au.csiro.ict.Cache._
import org.scalatra.ScalatraServlet

trait RestfulUserRegistration {
  self:ScalatraServlet with RestfulHelpers with Logger with RestfulUsers=>

  post("/register") {
    logger.info("User registering with username:"+params.get("name")+" and email:"+params.get("email"))
    (UniqueUsername(Username(params.get("name"))),
      Password(params.get("password")),
      UniqueEmail(Email(params.get("email"))),
      Description(params.get("description")),
      PictureUrl(params.get("picture")),
      WebUrl(params.get("website"))) match {
      case (Some(name),Some(password),Some(email),Some(description),Some(pic),Some(website))=>
        addUser(name, password, email, pic, website, description)
        login(name,password)
        sendSession()
      case errors => haltMsg()
    }
  }
}
