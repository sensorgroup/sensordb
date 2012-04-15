package au.csiro.ict.tests

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, FunSuite}
import au.csiro.ict.Cache._
import org.bson.types.ObjectId
import au.csiro.ict.{InputProcessingSystemProxy, InputProcessingBackend, Task, SensorDB}

class RestfulDataAccessTests extends ScalatraSuite with FunSuite with BeforeAndAfterAll{

  addServlet(classOf[SensorDB], "/*")

  val token1 =  "STREAM01-b494-497b-a95e-b27824098057"
  val token2 =  "STREAM02-b494-497b-a95e-b27824098059"

  val measurementId = new ObjectId()

  val user1Id = addUser("u1","pass1","000","u1@example.com","","","").get
  val user2Id = addUser("u2","pass2","000","u1@example.com","","","").get
  val exp1Id = addExperiment("exp1",user1Id,"000","","","","").get
  val exp2Id = addExperiment("exp2",user2Id,"000","","","","").get
  val node1Id =addNode("node1",user1Id,exp1Id,"-1","-1","-1","","","").get
  val node2Id = addNode("node2",user2Id,exp2Id,"-1","-1","-1","","","").get
  val stream1Id = addStream("stream1",user1Id,node1Id,measurementId,"","","",Some(token1)).get
  val stream2Id = addStream("stream2",user2Id,node2Id,measurementId,"","","",Some(token2)).get

  test("Data insertion, missing data parameter") {
    post("/data"){
      body should include("missing")
      status should equal(400)
    }
  }
  test("Data insertion, bad json format") {
    post("/data",Map("data"->"[xx]")){
      body should include("parse")
      status should equal(400)
    }
  }
  test("Data insertion, good json, bad format for parsing") {
    post("/data",Map("data"->"[\"xx\"]")){
      body should include("parse")
      status should equal(400)
    }
  }
  test("Data insertion, good format, zero entry") {
    post("/data",Map("data"->"[]")){
      body should include("0")
      status should equal(200)
    }
  }

  test("Data insertion, good format, one entry") {
    post("/data",Map("data"->("[[\""+token1+"\","+10+","+100+"]]"))){
      body should include("1")
      status should equal(200)
    }
  }

  test("Data insertion, good format, two entries with same timestamp entry") {
    post("/data",Map("data"->("[[\""+token1+"\","+10+","+100+"],[\""+token1+"\","+10+","+100+"]]"))){
      body should include("2")
      status should equal(200)
    }
  }

  override protected def afterAll() {
    val ips = new InputProcessingSystemProxy
    ips.process(new Task("my-queue1"))
    delUser(user1Id)
    delUser(user2Id)
  }

}
