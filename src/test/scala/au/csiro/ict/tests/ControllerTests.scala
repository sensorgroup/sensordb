package au.csiro.ict.tests

import org.scalatest.FunSuite
import org.scalatra.test.scalatest.ScalatraSuite
import com.codahale.jerkson.Json._
import java.util.LinkedHashMap
import org.bson.types.ObjectId
import au.csiro.ict.{Cache, SensorDB}

class ControllerTests extends ScalatraSuite with FunSuite{
  addServlet(classOf[SensorDB], "/*")
  test("Validators") {
    import au.csiro.ict.Validators._
    implicit val validator=new Validator()
    EntityId(None) should equal(None)
    EntityId(Some("")) should equal(None)
    EntityId(Some("   ")) should equal(None)
    EntityId(Some("12345")) should equal(None)
    EntityId(Some(new ObjectId().toString)) should not equal(None)

    Email(None) should equal(None)
    Email(Some("--")) should equal(None)
    Email(Some("@")) should equal(None)
    Email(Some("2@")) should equal(None)
    Email(Some("2@l")) should equal(None)
    Email(Some("2@lll.com")) should not equal(None)

    Password(None) should equal(None)
    Password(Some("--")) should equal(None)
    Password(Some("1234")) should equal(None)
    Password(Some("123456")) should not equal(None)

  }

  test("Full CRUD Restful API") {
    var stream1:Map[String,String]=Map()
    var stream2:Map[String,String]=Map()
    var node1:Map[String,String]=Map()
    var node2:Map[String,String]=Map()
    var exp1:Map[String,String]=Map()
    var exp2:Map[String,String]=Map()
    var exp3:Map[String,String]=Map()
    var user1:Map[String,Any]=Map()
    var user2:Map[String,Any]=Map()
    var units:List[Map[String,String]]=List()

    get("/measurements"){
      units = parse[List[Map[String,String]]](body)
      status must equal(200)
    }

    get("/metadata/remove",Map("id"->new ObjectId().toString,"name"->"age")){
      body should include("error")  // fails because there is no valid user session
      status must equal(400)
    }
      get("/metadata/add",Map("id"->new ObjectId().toString,"name"->"age","value"->"30")){
      body should include("error") // fails because there is no valid user session
      status must equal(400)
    }
      get("/metadata/retrieve/"+new ObjectId().toString){
      body should include("{}")
      status must equal(200)
    }

    session{
      get("/session"){
        body should not include("_id")
        body should not include("token")
      }

      delete("/nodes"){
        // Delete without providing eid nor nid
        body should include ("error")
      }
      delete("/streams"){
        // Delete without providing eid nor nid
        body should include ("error")
      }
      get("/tokens"){
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
      post("/experiments",Map("name"->"exp1","timezone"->"Australia/Sydney")){
        // no valid session
        status should equal(400)
      }
      post("/register",Map("name"->"ali","email"->"test@example.com","password"->"secret1")) {
        body should include ("_id")
        user1 = parse[Map[String,Any]](body)
        status must equal(200)
      }
      get("/session"){
        body should include ("_id")
      }
      get("/tokens"){
        body should include ("[]")
      }
      post("/logout"){
        body should include ("{}")
        status should equal (200)
      }
      get("/tokens"){
        body should include ("error")
      }
      get("/session",Map("user"->"ali")){
        body should include ("_id")
        body should not include ("email")
        body should not include ("password")
        body should include ("created_at")
      }
      post("/register",Map("name"->"ali2","email"->"test@example.com","password"->"secret1")) {
        // fails because email is not unique
        body.toLowerCase should include ("email")
        body should include ("error")
        status must equal(400)
      }
      post("/register",Map("name"->"ali2","email"->"test2@example.com","password"->"secret2")) {
        body should not include("password")
        user2 = parse[Map[String,Any]](body)
        status must equal(200)
        user1("user").asInstanceOf[LinkedHashMap[String,Long]].get("created_at") should be < (user2("user").asInstanceOf[LinkedHashMap[String,Long]].get("created_at"))
      }
      get("/session"){
        body should include ("ali2")
      }

      post("/experiments",Map("name"->"exp3","timezone"->"Australia/Sydney")){
        //        successful creation of exp3 experiment for user2
        exp3 = parse[Map[String,String]](body)
        body should include ("_id")
        status should equal(200)
      }

      post("/logout"){
        body should include ("{}")
        status should equal (200)
      }
      post("/register",Map("name"->"ali","email"->"test@example.com","password"->"secret1")) {
        // user name is already used
        status must equal(400)
      }
      post("/login",Map("name"->"ali","password"->"secret1")) {
        status should equal (200)
        body should include ("_id")
      }
      delete("/experiments"){
        // Delete without providing eid
        body should include ("error")
      }
      post("/experiments",Map("name"->"exp1","timezone"->"Australi")){
        // invalid timezone
        body should include ("error")
      }
      post("/experiments",Map("name"->"exp1","timezone"->"    ")){
        // missing timezone
        body should include ("error")
      }
      get("/session"){
        body should not include ("exp2")
        body should not include ("exp1")
      }
      post("/experiments",Map("name"->"exp1","timezone"->"Australia/Sydney")){
        //        successful creation of exp1 experiment
        exp1 = parse[Map[String,String]](body)
        body should include ("_id")
        status should equal(200)
      }

      get("/metadata/add",Map("id"->exp1.apply("_id"),"name"->"age","value"->"30<script>31</script>")){
        status must equal(200)
      }
      get("/metadata/retrieve/"+exp1.apply("_id")){
        body should include("age")
        body should not include("<script>") // script tag is removed from value automatically.
        body should not include("31\"") // script tag is removed from value automatically., the double quote added to the end to emphesis that 31 should appear at the end of the text (otherwise it may appear in other places such as middle of timestamp)
        body should include("30")
        status must equal(200)
      }

      get("/session"){
        body should include ("exp1")
        body should include ("30")
        body should include ("age")
        body should include ("metadata")
      }

      get("/metadata/remove",Map("id"->exp1.apply("_id"),"name"->"age")){
        status must equal(200)
      }
      get("/metadata/retrieve/"+exp1.apply("_id")){
        body should not include("age")
        body should not include("\"30\"")
        status must equal(200)
      }

      post("/experiments",Map("name"->"exp1","timezone"->"Australia/Sydney")){
        //failed, name reused creation of experiment
        status should equal(400)
      }

      get("/session"){
        body should not include ("exp2")
        body should include ("exp1")
        body should not include ("\"30\"")
        body should not include ("age")
        body should include ("metadata")
      }
      post("/experiments",Map("name"->"exp2","timezone"->"Australia/Sydney","public_access"->Cache.EXPERIMENT_ACCESS_PRIVATE.toString)){
        // success, exp2 created
        exp2 = parse[Map[String,String]](body)
        exp2.get("created_at") should  be > exp1.get("created_at")
        status should equal(200)
      }
      put("/experiments",Map("field"->"name","value"->"exp2","eid"->exp1("_id"))){
        // rename fails, exp1 can't be renamed to exp2, name exists
        body should include("error")
        status should equal(400)
      }

      put("/experiments",Map("field"->"name","value"->"exp 123","eid"->exp1("_id"))){
        // renaming exp1 to exp 123 successful
        status should equal(200)
      }
      put("/experiments",Map("field"->"description","value"->"desc-123","eid"->exp1("_id"))){
        // adding description to exp1
        body should include("desc-123")
        status should equal(200)
      }

      get("/session"){
        body should not include ("exp3")
        body should include ("exp2")
        body should include ("exp 123")
        body should include ("desc-123")
      }
      post("/nodes",Map("name"->"node1","eid"->exp1("_id"))){
        // successful creation of node1 within experiment 1
        node1 = parse[Map[String,String]](body)
        body should include ("_id")
        status should equal(200)
      }
      post("/nodes",Map("name"->"node1","eid"->exp1("_id"))){
        // failed, name is already used
        body should include ("error")
        status should equal(400)
      }
      get("/session"){
        body should include ("node1")
        body should not include ("node2")
      }
      post("/nodes",Map("name"->"node2","eid"->exp1("_id"))){
        // successful creation of node2 within exp1
        body should include ("_id")
        node2 = parse[Map[String,String]](body)
        status should equal(200)
      }
      post("/streams",Map("name"->"stream1","nid"->node1("_id"),"mid"->units.head.apply("_id"))){
        // successful creation of stream1 within exp1
        body should include ("_id")
        body should include ("token")
        stream1 = parse[Map[String,String]](body)
        status should equal(200)
      }
      get("/tokens"){
        body should include ("_id")
        body should include (stream1("_id"))
        body should include (stream1("token"))
      }
      post("/streams",Map("name"->"stream1","nid"->node1("_id"),"mid"->units.head.apply("_id"))){
        // failed name not available
        body should include ("error")
        status should equal(400)
      }
      post("/streams",Map("name"->"stream1","nid"->"dfkjgflkdjgdfljgd","mid"->units.head.apply("_id"))){
        // failed bad nodeId not valid
        body should include ("error")
        status should equal(400)
      }

      get("/session"){
        body should  include ("node1")
        body should  include ("node2")
        body should  include ("stream1")
        body should not include ("node1 renamed")
      }

      put("/nodes",Map("field"->"name","value"->"node1 renamed","nid"->node1("_id"))){
        // successful renaming of node1 to node1 renamed experiment
        node1 = parse[Map[String,String]](body)
        body should include ("_id")
        body should include ("node1 renamed")
        status should equal(200)
      }
      get("/session"){
        // confirm renamed work
        body should include ("node1 renamed")
      }
      put("/nodes",Map("field"->"lon","value"->"-123.321","nid"->node1("_id"))){
        // successfully adding longitude to node1
        body should include ("-123.321")
        node1 = parse[Map[String,String]](body)
        node1("name") should equal("node1 renamed")
        node1("lon") should equal("-123.321")
        status should equal(200)
      }
      put("/nodes",Map("field"->"lon","value"->"","nid"->node1("_id"))){
        // successfully resetting longitude to node1
        node1 = parse[Map[String,String]](body)
        body should not include ("-123.321")
        body should include (exp1("_id"))
        status should equal(200)
      }

      put("/nodes",Map("field"->"eid","value"->exp2("_id"),"nid"->node1("_id"))){
        // successfully moving node1 from exp1 to exp2
        node1 = parse[Map[String,String]](body)
        body should not include (exp1("_id"))
        body should include (exp2("_id"))
        status should equal(200)
      }
      put("/nodes",Map("field"->"eid","value"->exp3("_id"),"nid"->node1("_id"))){
        // invalid move; permission denied
        body should include ("error")
        status should equal(400)
      }
      put("/nodes",Map("field"->"eid","value"->"   ","nid"->node1("_id"))){
        // invalid move, should return 400
        body should include ("error")
        status should equal(400)
      }
      put("/nodes",Map("field"->"eid","value"->"44444444444","nid"->node1("_id"))){
        // invalid move, should return 400
        body should include ("error")
        status should equal(400)
      }

      put("/nodes",Map("field"->"eid","value"->node2("_id"),"nid"->node1("_id"))){
        // invalid move, should return 400 successfully moving node1 from exp1 to exp2
        body should include ("error")
        status should equal(400)
      }

      get("/session"){
        body should include ("token")
        // User Ali has Experiment2 which is private, at this stage it has one node, named "node1 renamed" and "node1 renamed" has one stream, named stream1
        body should include ("node1 renamed")
        body should include ("exp2")
        body should include ("exp 123")
        body should include ("stream1")
        body should not include ("machine")
      }

      post("/logout"){
        body should include ("{}")
        status should equal (200)
      }

      get("/session",Map("user"->"ali")){
        // Permission check, after logout, all the private experiments and its children should be invisible.
        body should not include ("token")
        // User Ali has Experiment2 which is private, at this stage it has one node, named "node1 renamed" and "node1 renamed" has one stream, named stream1
        body should not include ("node1 renamed")
        body should not include ("exp2")
        body should include ("exp 123")
        body should not include ("stream1")
        body should not include ("machine")
        body should  include ("ali")
      }

      post("/login",Map("name"->"ali2","password"->"secret2")) {
        status should equal (200)
        body should include ("_id")
      }


      get("/session",Map("user"->"ali")){
        // Permission check, another user which is logged in still can't see private private experiments of others and their children.
        body should not include ("token")
        // User Ali has Experiment2 which is private, at this stage it has one node, named "node1 renamed" and "node1 renamed" has one stream, named stream1
        body should not include ("node1 renamed")
        body should not include ("exp2")
        body should include ("exp 123")
        body should not include ("stream1")
        body should not include ("machine")
        body should  include ("ali")
      }

      post("/logout"){
        body should include ("{}")
        status should equal (200)
      }

      post("/login",Map("name"->"ali","password"->"secret1")) {
        status should equal (200)
        body should include ("_id")
      }

      delete("/nodes",Map("nid"->node1("_id"))){
        status should equal(200)
      }

      get("/session"){
        body should not include ("node1 renamed")
        status should equal(200)
      }

      delete("/experiments",Map("eid"->exp1("_id"))){
        status should equal(200)
      }
      get("/session"){
        body should not include ("exp 123")
        body should include ("exp2")
      }

      delete("/experiments",Map("eid"->exp2("_id"))){
        status should equal(200)
      }

      post("/remove",Map("name"->"ali","password"->"secret2")) {
        status should equal (200)
      }
      get("/session"){
        body should not include ("token")
      }

      post("/remove",Map("name"->"ali","password"->"secret1")) {
        status should equal (200)
      }
      post("/remove",Map("name"->"ali2","password"->"secret2")) {
        status should equal (200)
      }
      get("/session"){
        body should include ("{}")
      }
      post("/remove",Map("name"->"nonexist","password"->"secret1")) {
        status must equal(200)
      }
    }

  }
}
