package au.csiro.ict.tests

import org.scalatest.FunSuite
import org.scalatra.test.scalatest.ScalatraSuite
import com.codahale.jerkson.Json._
import au.csiro.ict.{Experiment, Controller}

class ControllerTests extends ScalatraSuite with FunSuite{
  addServlet(classOf[Controller], "/*")
  test("Full user Restful API") {
    session{
      post("/session"){
        body.indexOf("token")>0 equals(false)
      }

      delete("/experiments"){
        // Delete without providing experiment_id
        body should include ("error")
      }
      delete("/nodes"){
        // Delete without providing experiment_id nor node_id
        body should include ("error")
      }
      delete("/nodes",Map("experiment_id"->"123A")){
        // Delete without providing node_id
        body should include ("error")
      }
      delete("/nodes",Map("node_id"->"123A")){
        // Delete without providing node_id
        body should include ("error")
      }
      delete("/streams"){
        // Delete without providing experiment_id nor node_id
        body should include ("error")
      }
      delete("/streams",Map("experiment_id"->"123A")){
        // Delete without providing node_id nor stream_id
        body should include ("error")
      }
      delete("/streams",Map("node_id"->"123A")){
        // Delete without providing experiment_id nor stream_id
        body should include ("error")
      }
      delete("/streams",Map("experiment_id"->"123A","node_id"->"123A")){
        // Delete without providing stream_id
        body should include ("error")
      }
      post("/experiments",Map("name"->"exp1","timezone"->"1000")){
        // invalid user session
        body should include ("session")
        body should include ("error")
      }
      post("/register",Map("name"->"ali","email"->"test@example.com","password"->"secret1","timezone"->"0")) {
        println(body)
        status must equal(200)
        body should include ("token")
      }

      post("/session"){
        body should include ("token")
      }

      post("/experiments",Map("name"->"exp1","timezone"->"10000")){
        // invalid timezone
        body should include ("error")
      }
      post("/experiments",Map("name"->"exp1","timezone"->"    ")){
        // missing timezone
        body should include ("error")
      }
      var exp1:Map[String,String]=Map()
      var exp2:Map[String,String]=Map()

      post("/experiments",Map("name"->"exp1","timezone"->"1000")){
        //        successful creation of experiment
        println(body)
        exp1 = parse[Map[String,String]](body)
        status should equal(200)

      }
      post("/session"){
        body should include ("exp1")
      }
      post("/experiments",Map("name"->"exp1","timezone"->"1000")){
        //failed, name reused creation of experiment
        status should equal(400)

      }
      post("/experiments",Map("name"->"exp2","timezone"->"1000","public_access"->"false")){
        //failed, name reused creation of experiment
        status should equal(200)
      }
      post("/session"){
        body should include ("exp1")
        body should include ("exp2")
      }
      //      delete("/experiments",Map("experiment_id"->exp1("_id"))){
      //
      //      }
      post("/logout"){
        body should include ("{}")
        status should equal (200)
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
