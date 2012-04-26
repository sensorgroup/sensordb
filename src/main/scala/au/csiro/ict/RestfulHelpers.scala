package au.csiro.ict

import org.bson.types.ObjectId
import au.csiro.ict.Validators.Validator
import org.scalatra.ScalatraServlet
import au.csiro.ict.JsonGenerator.generate

trait RestfulHelpers {
  self:ScalatraServlet =>

  implicit def errors:Validator = request.getAttribute("__errors") match {
    case v:Validator =>v
    case others =>
      val validator = new Validator()
      request.setAttribute("__errors",validator)
      validator
  }

  def haltMsg(newErrorMessage:String=null) = {
    if(newErrorMessage!=null)
      errors.addError(newErrorMessage)
    if (!errors.errors.isEmpty)
      halt(400,generate(Map("errors"->errors.errors)))
    None
  }


  def forward(path:String="/session")=servletContext.getRequestDispatcher(path).forward(request, response)


}
