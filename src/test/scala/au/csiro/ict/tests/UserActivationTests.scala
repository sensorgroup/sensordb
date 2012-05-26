package au.csiro.ict.tests

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import au.csiro.ict.Cache._
import au.csiro.ict.SensorDB
import au.csiro.ict.Cache._

class UserActivationTests extends ScalatraSuite with FunSuite with BeforeAndAfterAll {

  addServlet(classOf[SensorDB], "/*")

  test("Check to see if an inactive user can login") {
    val user1Id = addUser("u1","pass1","u1@example.com","","","",false).get
    get("/session"){
      body should not include("_id")
      body should not include("token")
    }
    post("/login",Map("name"->"u1","password"->"pass1")) {
      status should equal (400)
      body should include ("error")
    }
    get("/session"){
      body should not include("_id")
      body should not include("token")
    }
    delUser(user1Id)
  }
}
