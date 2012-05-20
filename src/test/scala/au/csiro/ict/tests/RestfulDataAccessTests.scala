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
import org.joda.time.DateTime


class RestfulDataAccessTests extends ScalatraSuite with FunSuite with BeforeAndAfterAll{
  val DATA_RAW_URI = "/data"
  addServlet(classOf[SensorDB], "/*")

  val token1 =  "STREAM01-b494-497b-a95e-b27824098057"
  val token2 =  "STREAM02-b494-497b-a95e-b27824098059"

  val measurementId = new ObjectId()

  val user1Id = addUser("u1","pass1","000","u1@example.com","","","").get
  val user2Id = addUser("u2","pass2","000","u1@example.com","","","").get
  val exp1Id = addExperiment("exp1",user1Id,"Australia/Sydney",Cache.EXPERIMENT_ACCESS_PUBLIC,"","","").get
  val exp2Id = addExperiment("exp2",user2Id,"Australia/Sydney",Cache.EXPERIMENT_ACCESS_PUBLIC,"","","").get
  val node1Id =addNode("node1",user1Id,exp1Id,"-1","-1","-1","","","").get
  val node2Id = addNode("node2",user2Id,exp2Id,"-1","-1","-1","","","").get
  val stream1Id = addStream("stream1",user1Id,node1Id,measurementId,"","","",Some(token1)).get
  val stream2Id = addStream("stream2",user2Id,node2Id,measurementId,"","","",Some(token2)).get
  val date1UKFormat = "30-01-2010"
  val date1 = SDBTestHelpers.ukDateTimeToInt(date1UKFormat+"T07:15:20",Utils.TZ_Sydney)
  val date2 = SDBTestHelpers.ukDateTimeToInt(date1UKFormat+"T07:15:21",Utils.TZ_Sydney)
  val date3 = SDBTestHelpers.ukDateTimeToInt(date1UKFormat+"T07:15:22",Utils.TZ_Sydney)
  val date4 = SDBTestHelpers.ukDateTimeToInt(date1UKFormat+"T20:15:00",Utils.TZ_Sydney)
  val date1YYSummary = (new DateTime(date1*1000L).withZone(Utils.TZ_Sydney).withDayOfYear(1).withMillisOfDay(0).getMillis/1000L).asInstanceOf[Int]
  val date1MonthSummary = (new DateTime(date1*1000L).withZone(Utils.TZ_Sydney).withDayOfMonth(1).withMillisOfDay(0).getMillis/1000L).asInstanceOf[Int]
  val date1HourSummary = (new DateTime(date1*1000L).withZone(Utils.TZ_Sydney).withMillisOfDay(0).getMillis/1000L).asInstanceOf[Int]
  val date1DaySummary = (new DateTime(date1*1000L).withZone(Utils.TZ_Sydney).withMillisOfDay(0).getMillis/1000L).asInstanceOf[Int]
  val date1MinSummaryA = (new DateTime(date1*1000L).withZone(Utils.TZ_Sydney).withSecondOfMinute(0).withMillisOfSecond(0).getMillis/1000L).asInstanceOf[Int]
  val date1MinSummaryB = (new DateTime((date1+60)*1000L).withZone(Utils.TZ_Sydney).withSecondOfMinute(0).withMillisOfSecond(0).getMillis/1000L).asInstanceOf[Int]

  test("Check the stream2 to be empty") {
    get(DATA_RAW_URI,Map("level"->"raw","sid"->stream2Id.toString,"sd"->"30-1-2000","ed"->"28-12-2030","st"->"0","et"->"86399")){
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
  //
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

  test("Data insertion, good format, 100 times, one insert each time") {
    for (i<- 0 until 100){
      post(DATA_RAW_URI , Map("data"->("{\""+token1+"\":{\""+(date1+i)+"\":"+i+"}}"))){
        body should include("1")
        status should equal(200)
      }
    }
    Thread.sleep(1000)
  }
  test("Check that stream 1 is not empty") {
    get(DATA_RAW_URI,Map("sid"->stream1Id.toString,"sd"->"30-1-2000","ed"->"28-12-2030")){
      body should include(stream1Id.toString)
      body should include("99.0") // we inserted 0 to 99 (inclusive), so 99 should be one of the entries.
      body should include(date1.toString)
      body should include(stream1Id.toString)
      status should equal(200)
    }
  }
  test("Testing EntityIDList Validator"){
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

    Validators.EntityIdList(Some(new ObjectId().toString)) should have size(1)
    validator.errors should have size(0)

    val oid = new ObjectId().toString
    Validators.EntityIdList(Some(generate(List(oid,oid)))) should have size(1)
    validator.errors should have size(0)

    Validators.EntityIdList(Some(generate(List(new ObjectId().toString,new ObjectId().toString)))) should have size(2)
    validator.errors should have size(0)
  }

  test("Testing summaries for 100 inserts"){
    get(DATA_RAW_URI , Map("sid"->stream1Id.toString,"level"->"1-year","sd"->date1UKFormat,"ed"->date1UKFormat)){
      body should include("["+stream1Id.toString+","+date1YYSummary+",0.0,99.0,100.0,4950.0,328350.0]")
      status should equal(200)
    }
    get(DATA_RAW_URI , Map("sid"->generate(Set(stream1Id.toString,new ObjectId().toString)),"sd"->date1UKFormat,"ed"->date1UKFormat)){ // non-existing stream id
      body should include("error")
      status should equal(400)
    }
    get(DATA_RAW_URI , Map("sid"->generate(Set(new ObjectId().toString)),"sd"->date1UKFormat,"ed"->date1UKFormat)){ //non-existing stream id
      body should include("error")
      status should equal(400)
    }

    get(DATA_RAW_URI , Map("sid"->generate(Set(stream1Id.toString,stream2Id.toString)),"level"->"1-year","sd"->date1UKFormat,"ed"->date1UKFormat)){
      body should include("["+stream1Id.toString+","+date1YYSummary+",0.0,99.0,100.0,4950.0,328350.0]")
      body should include(stream1Id.toString)
      body should not include(stream2Id.toString)
      status should equal(200)
    }
  }


  test("Data insertion, good format, one entry insert") {
    post(DATA_RAW_URI,Map("data"->generate(Map(token1->Map((date1+110).toString-> -333.0 ))))){
      body should include("1")
      status should equal(200)
    }
    get(DATA_RAW_URI , Map("sid"->generate(Set(stream1Id.toString)),"sd"->date1UKFormat,"ed"->date1UKFormat,"level"->"1-month")){
      body should include("["+stream1Id.toString+","+date1MonthSummary+",-333.0,99.0,101.0,4617.0,439239.0]")
      status should equal(200)
    }
  }

  test("Data insertion, good format, updating one of the values with a differnt timestamp") {
    post(DATA_RAW_URI,Map("data"->generate(Map(token1->Map((date1).toString-> 1.0 ))))){
      body should include("1")
      status should equal(200)
    }
    Thread.sleep(100)//waiting until updates are applied to redis/storage. Without this tests were failing. I guess redis flush was required.
    get(DATA_RAW_URI , Map("sid"->generate(Set(stream1Id.toString)),"sd"->date1UKFormat,"ed"->date1UKFormat,"level"->"1-month")){
      body should include("["+stream1Id.toString+","+date1MonthSummary+",-333.0,99.0,101.0,4618.0,439240.0]")
      status should equal(200)
    }
  }
  test("Data insertion, good format, two entries with different timestamps, one inserting and one updating") {
    /**
     * Updated timestamp date1 to 123.321
     * Updated timestamp date1+110 to -333
     */
    post(DATA_RAW_URI,Map("data"->generate(Map(token1->Map(date1->123.321,(date1+110).toString-> -333 ))))){
      body should include("2")
      status should equal(200)
    }
    Thread.sleep(200) // waiting until processing being done
    get(DATA_RAW_URI , Map("sid"->generate(Set(stream1Id.toString)),"sd"->date1UKFormat,"ed"->date1UKFormat,"level"->"1-month")){
      body should include("["+stream1Id.toString+","+date1MonthSummary+",-333.0,123.321,101.0,4740.321,454447.069041]")
      status should equal(200)
    }
  }
  test("Data insertion, good format, two entries with different timestamps, one deleting and one updating") {
    /**
     * Updated timestamp date1 to 123.321
     * Updated timestamp date1+110 to -333
     */
    post(DATA_RAW_URI,Map("data"->generate(Map(token1->Map(date1->None,(date1+110).toString-> 77.7 ))))){
      body should include("2")
      status should equal(200)
    }
    Thread.sleep(200) // waiting until processing being done

    get(DATA_RAW_URI , Map("sid"->generate(Set(stream1Id.toString)),"sd"->date1UKFormat,"ed"->date1UKFormat,"level"->"1-day")){
      body should include("["+stream1Id.toString+","+date1DaySummary+",1.0,99.0,100.0,5027.7,334387.29]")
      body should include(stream1Id.toString)
      status should equal(200)
    }
  }
  test("Data insertion, good format, 50 overrides,50 inserts") {
    for (i<- 0 until 100){
      post(DATA_RAW_URI , Map("data"->("{\""+token1+"\":{\""+(date1-50+i)+"\":"+i+"}}"))){
        body should include("1")
        status should equal(200)
      }
    }
    Thread.sleep(100)
    get(DATA_RAW_URI , Map("sid"->generate(Set(stream1Id.toString)),"sd"->date1UKFormat,"ed"->date1UKFormat,"level"->"1-day")){
      body should include("["+stream1Id.toString+","+date1HourSummary+",0.0,99.0,151.0,8752.7,622312.29]")
      body should include(stream1Id.toString)
      status should equal(200)
    }
  }

  test("Dummy, cleaning ..."){
    delUser(user1Id)
    delUser(user2Id)
    1 should equal(1)
  }

}
