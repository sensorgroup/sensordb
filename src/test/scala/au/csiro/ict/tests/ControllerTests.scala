package au.csiro.ict.tests

import org.scalatest.FunSuite
import org.scalatra.test.scalatest.ScalatraSuite
import au.csiro.ict.Controller

class ControllerTests extends ScalatraSuite with FunSuite{
  addServlet(classOf[Controller], "/*")
  test("Full user Restful API") {
    session{
      post("/session"){
        body.indexOf("token")>0 equals(false)
      }
      post("/register",Map("name"->"ali","email"->"test@example.com","password"->"secret1","timezone"->"0")) {
        status must equal(200)
        body should include ("token")
      }

      post("/session"){
        body should include ("token")
      }

      post("/logout"){
        status should equal (200)
        body should include ("{}")
      }
      post("/login",Map("name"->"ali","password"->"secret1")) {
        status should equal (200)
        body should include ("token")
      }
      post("/session"){
        body should include ("token")
      }
      post("/remove",Map("name"->"ali","password"->"secret2")) {
        status should equal (200)

      }
      post("/session"){
        body should include ("token")
      }
      post("/remove",Map("name"->"ali","password"->"secret1")) {
        status should equal (200)
      }
      post("/session"){
        body should include ("{}")
      }
      post("/remove",Map("name"->"nonexist","password"->"secret1")) {
        status must equal(200)
      }
    }
  }
}
