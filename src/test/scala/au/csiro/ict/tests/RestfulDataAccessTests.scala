package au.csiro.ict.tests

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, FunSuite}
import au.csiro.ict.Cache._
import org.bson.types.ObjectId
import au.csiro.ict._
import akka.actor.{Actor, Props, ActorSystem}
import org.scalatra.test.ScalatraTests._
import scala.None
import com.codahale.jerkson.Json._


class RestfulDataAccessTests extends ScalatraSuite with FunSuite with BeforeAndAfterAll{
  val DATA_RAW_URI = "/data/raw"
  val DATA_SUMMARY_DAILY_URI = "/data/summary/daily"
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
  val date1UKFormat = "30-01-2010"
  val date1 = Utils.ukDateTimeToInt(date1UKFormat+"T07:15:20")
  val date2 = Utils.ukDateTimeToInt(date1UKFormat+"T07:15:21")
  val date3 = Utils.ukDateTimeToInt(date1UKFormat+"T07:15:22")
  val date4 = Utils.ukDateTimeToInt(date1UKFormat+"T20:15:00")

  test("Check the stream2  to be empty") {
    get(DATA_RAW_URI,Map("sid"->stream2Id.toString,"sd"->"30-1-2000","ed"->"28-12-2030","st"->"00:00:00","et"->"23:59:59")){
      body should include("{}")
      status should equal(200)
    }
  }

  test("Data insertion, missing data parameter") {
    post(DATA_RAW_URI){
      body should include("missing")
      status should equal(400)
    }
  }

  test("Data insertion, bad json format") {
    post(DATA_RAW_URI,Map("data"->"[xx]")){
      body should include("parse")
      status should equal(400)
    }
  }
  test("Data insertion, good json, bad format for parsing") {
    post(DATA_RAW_URI,Map("data"->"[\"xx\"]")){
      body should include("parse")
      status should equal(400)
    }
  }
  test("Data insertion, good format, zero entry") {
    post(DATA_RAW_URI,Map("data"->"{}")){
      body should include("0")
      status should equal(200)
    }
  }

  test("Data insertion, good format, 100 times, one entry") {
    StreamStatistics.getBitVectorFor(Utils.generateRowKey(stream1Id.toString,date2)).size must equal(0) // Stream Daily index is empty
    for (i<- 0 until 100){
      post(DATA_RAW_URI , Map("data"->("{\""+token1+"\":{\""+(date1+i)+"\":"+i+"}}"))){
        body should include("1")
        status should equal(200)
      }
    }
    Thread.sleep(1000)
   }
  test("Check that stream 1 is not empty") {
    get(DATA_RAW_URI,Map("sid"->stream1Id.toString,"sd"->"30-1-2000","ed"->"28-12-2030","st"->"00:00:00","et"->"23:59:59")){
      body should include(stream1Id.toString)
      body should include("99.0") // we inserted 0 to 99 (inclusive), so 99 should be one of the entries.
      body should include(date1.toString)
      status should equal(200)
    }
  }
  test("Raw data action detection"){
    StreamStatistics.findAction(Set[Int]())(10,Some(11)) should equal(InsertionType.INSERT)
    StreamStatistics.findAction(Set(1))(10,Some(11)) should equal(InsertionType.INSERT)
    StreamStatistics.findAction(Set(10))(10,Some(11)) should equal(InsertionType.UPDATE)
    StreamStatistics.findAction(Set(10))(10,None) should equal(InsertionType.DELETE)
    StreamStatistics.findAction(Set(10))(11,None) should equal(InsertionType.NOP)

//    val insertion1 = StreamStatistics.updateStatisticsInstructions(stream1Id.toString,date2,Some(1))
//    val insertion2 = StreamStatistics.updateStatisticsInstructions(stream1Id.toString,date3,Some(2))
//    val update1 = StreamStatistics.updateStatisticsInstructions(stream1Id.toString,date2,Some(1))
//    val update2 = StreamStatistics.updateStatisticsInstructions(stream1Id.toString,date3,Some(2))
//    val delete1 = StreamStatistics.updateStatisticsInstructions(stream1Id.toString,date3,None)
//    val delete2 = StreamStatistics.updateStatisticsInstructions(stream1Id.toString,date3,None)
    implicit val validator = Validators.Validator()
    Validators.EntityIdList(Some("[]")) should have size(0)
    validator.errors should have size(1)
    validator.reset()
    validator.errors should have size(0)

    Validators.EntityIdList(Some("[123456]")) should have size(0)
    validator.errors should have size(1)
    validator.reset()

    Validators.EntityIdList(None) should have size(0)
    validator.errors should have size(1)
    validator.reset()

    Validators.EntityIdList(Some(generate(Set(new ObjectId().toString)))) should have size(1)
    validator.errors should have size(0)

    val oid = new ObjectId().toString
    Validators.EntityIdList(Some(generate(List(oid,oid)))) should have size(1)
    validator.errors should have size(0)

    Validators.EntityIdList(Some(generate(List(new ObjectId().toString,new ObjectId().toString)))) should have size(2)
    validator.errors should have size(0)
  }

  test("Daily summary should include new item after 100 msec"){
    val streamDayKey = Utils.generateRowKey(stream1Id.toString,date2)
    InterdayStatCalculator.process should equal(None) // because it is an insert, no global process required.
    StreamStatistics.getBitVectorFor(streamDayKey) should have size(100) // Stream Daily index is updated
    StreamStatistics.getBitVectorFor(streamDayKey) should contain(Utils.getSecondOfDay(date1)) // Stream Daily index is updated from start
    StreamStatistics.getBitVectorFor(streamDayKey) should contain(Utils.getSecondOfDay((date1+10))) // Stream Daily index is updated in the middle
    StreamStatistics.getBitVectorFor(streamDayKey) should contain(Utils.getSecondOfDay((date1+99))) // Stream Daily index is updated to end
    StreamStatistics.getStatFor(streamDayKey) should not be (None)
    StreamStatistics.getStatFor(streamDayKey).get should have size(5)
    StreamStatistics.getStatFor(streamDayKey).get should equal(List[Double](99,0,100,4950,328350))
    val tempObjectId = new ObjectId().toString
    get(DATA_SUMMARY_DAILY_URI , Map("sid"->generate(Set(stream1Id.toString)),"sd"->date1UKFormat,"ed"->date1UKFormat)){
      body should include("[99.0,0.0,100.0,4950.0,328350.0]")
      status should equal(200)
    }
    get(DATA_SUMMARY_DAILY_URI , Map("sid"->generate(Set(stream1Id.toString,tempObjectId)),"sd"->date1UKFormat,"ed"->date1UKFormat)){ // non-existing stream id
      body should include("error")
      status should equal(400)
    }
    get(DATA_SUMMARY_DAILY_URI , Map("sid"->generate(Set(tempObjectId)),"sd"->date1UKFormat,"ed"->date1UKFormat)){ //non-existing stream id
      body should include("error")
      status should equal(400)
    }
    get(DATA_SUMMARY_DAILY_URI , Map("sid"->generate(Set(stream1Id.toString,stream2Id.toString)),"sd"->date1UKFormat,"ed"->date1UKFormat)){
      body should include("[99.0,0.0,100.0,4950.0,328350.0]")
      body should include(stream1Id.toString)
      body should include(stream2Id.toString)
      status should equal(200)
    }
  }

  //
  //  test("Data insertion, good format, two entries with different timestamps, one updating") {
  //    post(DATA_RAW_URI,Map("data"->("{\""+token1+"\":{\""+date1+"\":"+102+",\""+date2+"\":"+101+"}}"))){
  //      body should include("2")
  //      status should equal(200)
  //    }
  //  }
  //  test("Check that stream 1 should receive an update and and insert") {
  //    Thread.sleep(100)
  //    get(DATA_RAW_URI,Map("sid"->stream1Id.toString,"sd"->"30-1-2000","ed"->"28-12-2030","st"->"00:00:00","et"->"23:59:59")){
  //      body should include(stream1Id.toString)
  //      body should not include("100")
  //      body should include("101")
  //      body should include("102")
  //      body should include((date1)+",")
  //      body should include((date2)+",")
  //      status should equal(200)
  //    }
  //  }
  //
  //  test("Data insertion, good format, two entries with different timestamps, one removing") {
  //    post(DATA_RAW_URI,Map("data"->("{\""+token1+"\":{\""+date1+"\":"+103+",\""+date2+"\":null}}"))){
  //      body should include("2")
  //      status should equal(200)
  //    }
  //  }
  //  test("Check that stream 1 should receive a remove and a update") {
  //    Thread.sleep(100)
  //    get(DATA_RAW_URI,Map("sid"->stream1Id.toString,"sd"->"30-1-2000","ed"->"28-12-2030","st"->"00:00:00","et"->"23:59:59")){
  //      body should include(stream1Id.toString)
  //      body should not include("100")
  //      body should not include("101")
  //      body should not include("102")
  //      body should include("103")
  //      body should include((date1)+",")
  //      body should not include(date2.toString)
  //      status should equal(200)
  //    }
  //  }
  //
  //
  //


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
