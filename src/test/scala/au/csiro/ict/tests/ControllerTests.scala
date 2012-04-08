package au.csiro.ict.tests

import org.scalatest.FunSuite
import org.scalatra.test.scalatest.ScalatraSuite
import com.codahale.jerkson.Json._
import au.csiro.ict.{Experiment, Controller}

class ControllerTests extends ScalatraSuite with FunSuite{
  addServlet(classOf[Controller], "/*")
  test("Full CRUD Restful API") {
    session{
      post("/session"){
        body should not include("token")
      }

      delete("/nodes"){
        // Delete without providing eid nor nid
        body should include ("error")
      }
      delete("/nodes"){
        // Delete without providing eid nor nid
        body should include ("error")
      }
      delete("/nodes",Map("eid"->"123A")){
        // Delete without providing nid
        body should include ("error")
      }
      delete("/nodes",Map("nid"->"123A")){
        // Delete without providing nid
        body should include ("error")
      }
      delete("/streams"){
        // Delete without providing eid nor nid
        body should include ("error")
      }
      delete("/streams",Map("eid"->"123A")){
        // Delete without providing nid nor sid
        body should include ("error")
      }
      delete("/streams",Map("nid"->"123A")){
        // Delete without providing eid nor sid
        body should include ("error")
      }
      delete("/streams",Map("eid"->"123A","nid"->"123A")){
        // Delete without providing sid
        body should include ("error")
      }
      delete("/experiments"){
        // Delete without providing eid and no valid session id
        body should include ("error")
        status should equal(400)
      }
      post("/experiments",Map("name"->"exp1","timezone"->"1000")){
        // no valid session
        body should include ("error")
        status should equal(400)
      }
      post("/register",Map("name"->"ali","email"->"test@example.com","password"->"secret1","timezone"->"000")) {
        body should include ("token")
        status must equal(200)
      }

      post("/session"){
        body should include ("token")
      }

      post("/logout"){
        body should include ("{}")
        status should equal (200)
      }
      post("/login",Map("name"->"ali","password"->"secret1")) {
        status should equal (200)
        body should include ("token")
      }
      post("/register",Map("name"->"ali","email"->"test@example.com","password"->"secret1","timezone"->"0")) {
        // user name is already used
        status must equal(400)
      }
      delete("/experiments"){
        // Delete without providing eid
        body should include ("error")
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
      post("/session"){
        body should not include ("exp2")
        body should not include ("exp1")
      }
      post("/experiments",Map("name"->"exp1","timezone"->"1000")){
        //        successful creation of exp1 experiment
        exp1 = parse[Map[String,String]](body)
        body should include ("token")
        status should equal(200)
      }

      post("/experiments",Map("name"->"exp1","timezone"->"1000")){
        //failed, name reused creation of experiment
        status should equal(400)
      }

      post("/session"){
        body should not include ("exp2")
        body should include ("exp1")
      }
      post("/experiments",Map("name"->"exp2","timezone"->"1000","public_access"->"false")){
        // success, exp2 created
        status should equal(200)
      }
      put("/experiments",Map("field"->"name","value"->"exp2","eid"->exp1("_id"))){
        // rename fails, exp1 can't be renamed to exp2, name exists
        status should equal(400)
      }
      put("/experiments",Map("field"->"name","value"->"exp1","eid"->exp1("_id"))){
        // rename to the same thing (exp1 to exp1) is ok
        println(exp1("_id"))
        status should equal(200)
      }

      put("/experiments",Map("field"->"name","value"->"exp 123","eid"->exp1("_id"))){
        // renaming exp1 to exp 123 successful
        println(body)
        status should equal(200)
      }
      post("/session"){
        body should include ("exp2")
        body should include ("exp 123")
      }

      delete("/experiments",Map("id"->exp1("_id"))){
        status should equal(200)
      }
      post("/session"){
        body should include ("exp2")
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
