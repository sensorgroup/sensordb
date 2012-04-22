package au.csiro.ict.tests

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, FunSuite}
import au.csiro.ict.Cache._
import org.bson.types.ObjectId
import au.csiro.ict._
import akka.actor.{Actor, Props, ActorSystem}
import org.scalatra.test.ScalatraTests._
import scala.None

class RestfulDataAccessTests extends ScalatraSuite with FunSuite with BeforeAndAfterAll{

  addServlet(classOf[SensorDB], "/*")

  val token1 =  "STREAM01-b494-497b-a95e-b27824098057"
  val token2 =  "STREAM02-b494-497b-a95e-b27824098059"

  val measurementId = new ObjectId()

  val user1Id = addUser("u1","pass1","000","u1@example.com","","","").get
  val user2Id = addUser("u2","pass2","000","u1@example.com","","","").get
  val exp1Id = addExperiment("exp1",user1Id,"000",Cache.EXPERIMENT_ACCESS_PUBLIC,"","","").get
  val exp2Id = addExperiment("exp2",user2Id,"000",Cache.EXPERIMENT_ACCESS_PUBLIC,"","","").get
  val node1Id =addNode("node1",user1Id,exp1Id,"-1","-1","-1","","","").get
  val node2Id = addNode("node2",user2Id,exp2Id,"-1","-1","-1","","","").get
  val stream1Id = addStream("stream1",user1Id,node1Id,measurementId,"","","",Some(token1)).get
  val stream2Id = addStream("stream2",user2Id,node2Id,measurementId,"","","",Some(token2)).get
  val date1 = Utils.isoDateTimeFormat.parseDateTime("2010-01-30T07:15:20").getMillis/1000
  val date2 = Utils.isoDateTimeFormat.parseDateTime("2010-01-30T07:15:21").getMillis/1000
  val date3 = Utils.isoDateTimeFormat.parseDateTime("2010-01-30T07:15:22").getMillis/1000
  val date4 = Utils.isoDateTimeFormat.parseDateTime("2010-01-30T20:15:00").getMillis/1000

  test("Check the stream2  to be empty") {
    get("/data",Map("sid"->stream2Id.toString,"sd"->"30-1-2000","ed"->"28-12-2030","st"->"00:00:00","et"->"23:59:59")){
      println(body)
      body should include("{}")
      status should equal(200)
    }
  }

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
    post("/data",Map("data"->"{}")){
      body should include("0")
      status should equal(200)
    }
  }

  test("Data insertion, good format, one entry") {
    post("/data",Map("data"->("{\""+token1+"\":{\""+(date1)+"\":"+100+"}}"))){
      body should include("1")
      status should equal(200)
    }
  }
  test("Check that stream 1 is not empty") {
    Thread.sleep(100)
    get("/data",Map("sid"->stream1Id.toString,"sd"->"30-1-2000","ed"->"28-12-2030","st"->"00:00:00","et"->"23:59:59")){
      body should include(stream1Id.toString)
      body should include("100")
      body should include((date1)+",")
      status should equal(200)
    }
  }

  test("Data insertion, good format, two entries with different timestamps, one updating") {
    post("/data",Map("data"->("{\""+token1+"\":{\""+date1+"\":"+102+",\""+date2+"\":"+101+"}}"))){
      body should include("2")
      status should equal(200)
    }
  }
  test("Check that stream 1 should receive an update and and insert") {
    Thread.sleep(100)
    get("/data",Map("sid"->stream1Id.toString,"sd"->"30-1-2000","ed"->"28-12-2030","st"->"00:00:00","et"->"23:59:59")){
      body should include(stream1Id.toString)
      body should not include("100")
      body should include("101")
      body should include("102")
      body should include((date1)+",")
      body should include((date2)+",")
      status should equal(200)
    }
  }

  test("Data insertion, good format, two entries with different timestamps, one removing") {
    post("/data",Map("data"->("{\""+token1+"\":{\""+date1+"\":"+103+",\""+date2+"\":null}}"))){
      body should include("2")
      status should equal(200)
    }
  }
  test("Check that stream 1 should receive a remove and a update") {
    Thread.sleep(100)
    get("/data",Map("sid"->stream1Id.toString,"sd"->"30-1-2000","ed"->"28-12-2030","st"->"00:00:00","et"->"23:59:59")){
      body should include(stream1Id.toString)
      body should not include("100")
      body should not include("101")
      body should not include("102")
      body should include("103")
      body should include((date1)+",")
      body should not include(date2.toString)
      status should equal(200)
    }
  }



  test("Starting a sample actor"){
    val system = ActorSystem()
    val mockWorker = system.actorOf(Props[MockActor])
    val master = system.actorOf(Props(new InputProcessingMaster(mockWorker)))
    master ! "someQ"
    //    mockWorker. //todo investigate akka test package
    system.shutdown()
    1 should equal(1)

  }


  test("Dummy, cleaning ..."){
    delUser(user1Id)
    delUser(user2Id)
    1 should equal(1)
  }


  override protected def afterAll() {
    // todo: Don't know why this is not executed at the end rather close to begining
//    println("after all called ...............")
    //    val ips = new InputProcessingSystemProxy
    //    val ips = InputProcessingBackend.ips
    //    ips.process(new Task("my-queue1"))
    //    ips.shutdown()
  }

}
