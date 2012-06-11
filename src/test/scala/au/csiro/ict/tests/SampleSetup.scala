//package au.csiro.ict.tests
//
//import org.scalatest.FunSuite
//import org.scalatra.test.scalatest.ScalatraSuite
//import com.codahale.jerkson.Json._
//import java.util.LinkedHashMap
//import org.bson.types.ObjectId
//import org.joda.time.DateTime
//import au.csiro.ict.{Utils, Cache, SensorDB}
//import scala.Predef._
//import au.csiro.ict.Validators.Validator
//import org.scalatra.test.ScalatraTests._
//
///**
//* sample mock backend data for javascript testing
//*/
//class SampleSetup extends ScalatraSuite with FunSuite {
//
//  addServlet(classOf[SensorDB], "/*")
//
//  val username1 = "sample1"
//  val password1 = "secret1"
//
//  val username2 = "sample2"
//  val password2 = "secret2"
//
//  var userId = ""
//  var experimentId = ""
//  var nodeId = ""
//  var streamId = ""
//  var units:List[Map[String,String]]=List()
//
//  def idOf(s:String)=parse[Map[String,String]](s).get("_id").get
//  test("Sample Setup") {
//    get("/measurements"){
//      units = parse[List[Map[String,String]]](body)
//      status must equal(200)
//    }
//
//    session{
//      post("/register",Map("website"->"http://www.csiro.au/","name"->username1,"email"->(username1+"@example.com"),"password"->password1,"picture"->"http://127.0.0.1:9001/i/ali-user.png","description"->"This is a test user designed to evaluate various functionalities of SensorDB.")) {
//        body should include ("_id")
//        userId=parse[Map[String,Any]](body).apply("user").asInstanceOf[LinkedHashMap[String,String]].get("_id")
//        status must equal(200)
//      }
//      post("/register",Map("website"->"http://www.csiro.au/","name"->username2,"email"->(username2+"@example.com"),"password"->password2,"picture"->"http://127.0.0.1:9001/i/phenomics-user.png","description"->"""<p style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0.4em; padding-right: 0px; padding-bottom: 0.4em; padding-left: 0px; line-height: 1.7em; font-family: Verdana, Geneva, sans-serif; font-size: 12px; text-align: left; word-spacing: 1px; background-color: rgb(255, 255, 255); ">The Australian Plant Phenomics Facility is an initiative of the Australian Government conducted as part of the National Collaborative Research Infrastructure Strategy (NCRIS).</p><p style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0.4em; padding-right: 0px; padding-bottom: 0.4em; padding-left: 0px; line-height: 1.7em; font-family: Verdana, Geneva, sans-serif; font-size: 12px; text-align: left; word-spacing: 1px; background-color: rgb(255, 255, 255); ">It is supported by the South Australian and Australian Capital Territory (ACT) governments, along with the partner organisations and the Australian Centre for Plant Functional Genomics. The project is a collaboration between CSIRO, ANU,&nbsp;University of Adelaide and industry groups.</p><p style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0.4em; padding-right: 0px; padding-bottom: 0.4em; padding-left: 0px; line-height: 1.7em; font-family: Verdana, Geneva, sans-serif; font-size: 12px; text-align: left; word-spacing: 1px; background-color: rgb(255, 255, 255); ">Total investment in the Facility across Adelaide and Canberra is over A$40 million.</p><p style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0.4em; padding-right: 0px; padding-bottom: 0.4em; padding-left: 0px; line-height: 1.7em; font-family: Verdana, Geneva, sans-serif; font-size: 12px; text-align: left; word-spacing: 1px; background-color: rgb(255, 255, 255); ">Investment in the High Resolution Plant Phenomics Centre in Canberra totals around A$18 million. This includes direct financial support of:</p><ul style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0.4em; padding-right: 0px; padding-bottom: 0.8em; padding-left: 10px; list-style-type: none; list-style-position: initial; list-style-image: initial; font-family: Verdana, Geneva, sans-serif; font-size: 12px; text-align: left; word-spacing: 1px; background-color: rgb(255, 255, 255); "><li style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0em; padding-right: 0px; padding-bottom: 0em; padding-left: 14px; line-height: 1.7em; background-image: url(http://www.csiro.au/themes/default/img/blue-square-bullet.gif); background-attachment: initial; background-origin: initial; background-clip: initial; background-color: transparent; background-position: 0px 0.6em; background-repeat: no-repeat no-repeat; "><div style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; ">A$5.24 million from NCRIS</div></li><li style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0em; padding-right: 0px; padding-bottom: 0em; padding-left: 14px; line-height: 1.7em; background-image: url(http://www.csiro.au/themes/default/img/blue-square-bullet.gif); background-attachment: initial; background-origin: initial; background-clip: initial; background-color: transparent; background-position: 0px 0.6em; background-repeat: no-repeat no-repeat; "><div style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; ">A$5.8 million from CSIRO</div></li><li style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0em; padding-right: 0px; padding-bottom: 0em; padding-left: 14px; line-height: 1.7em; background-image: url(http://www.csiro.au/themes/default/img/blue-square-bullet.gif); background-attachment: initial; background-origin: initial; background-clip: initial; background-color: transparent; background-position: 0px 0.6em; background-repeat: no-repeat no-repeat; "><div style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; ">A$3.5 million from the ANU</div></li><li style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0em; padding-right: 0px; padding-bottom: 0em; padding-left: 14px; line-height: 1.7em; background-image: url(http://www.csiro.au/themes/default/img/blue-square-bullet.gif); background-attachment: initial; background-origin: initial; background-clip: initial; background-color: transparent; background-position: 0px 0.6em; background-repeat: no-repeat no-repeat; "><div style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; ">A$1.1 million from the ACT Government.</div></li></ul><p style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0.4em; padding-right: 0px; padding-bottom: 0.4em; padding-left: 0px; line-height: 1.7em; font-family: Verdana, Geneva, sans-serif; font-size: 12px; text-align: left; word-spacing: 1px; background-color: rgb(255, 255, 255); ">CSIRO and ANU are also supporting the facility through running costs and staffing, making up the remainder of the investment.</p><p style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0.4em; padding-right: 0px; padding-bottom: 0.4em; padding-left: 0px; line-height: 1.7em; font-family: Verdana, Geneva, sans-serif; font-size: 12px; text-align: left; word-spacing: 1px; background-color: rgb(255, 255, 255); ">Funding from the ACT Government will go towards postdoctoral research training fellowships, travel bursaries for users to travel to the ACT and PhD top-ups to support students.</p><p style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0.4em; padding-right: 0px; padding-bottom: 0.4em; padding-left: 0px; line-height: 1.7em; font-family: Verdana, Geneva, sans-serif; font-size: 12px; text-align: left; word-spacing: 1px; background-color: rgb(255, 255, 255); ">Read more about the&nbsp;<a href="http://www.plantphenomics.org.au/" style="margin-top: 0px; margin-right: 0px; margin-bottom: 0px; margin-left: 0px; padding-top: 0px; padding-right: 0px; padding-bottom: 0px; padding-left: 0px; color: rgb(0, 153, 204); line-height: 1.7; ">Australian Plant Phenomics Facility</a>&nbsp;<em>[external link].</em></p>""")) {
//        body should include ("_id")
//        userId=parse[Map[String,Any]](body).apply("user").asInstanceOf[LinkedHashMap[String,String]].get("_id")
//        status must equal(200)
//      }
//      post("/experiments",Map("name"->"yanco new setup","timezone"->"Australia/Sydney")){
//        body should include ("_id")
//        experimentId=idOf(body)
//        status should equal(200)
//      }
//      get("/metadata/add",Map("id"->experimentId,"name"->"location","value"->"Australia")){
//        status must equal(200)
//      }
//      get("/metadata/add",Map("id"->experimentId,"name"->"sensor type","value"->"Arduino")){
//        status must equal(200)
//      }
//      get("/metadata/add",Map("id"->experimentId,"name"->"deployment status","value"->"Active","start-ts"->"2012-03-05T21:42:26.220+10:00","end-ts"->"2012-05-05T21:42:26.220+10:00")){
//        status must equal(200)
//      }
//
//      post("/experiments",Map("name"->"yanco","timezone"->"Australia/Sydney")){
//        body should include ("_id")
//        experimentId=idOf(body)
//        status should equal(200)
//      }
//      post("/nodes",Map("name"->"node1","eid"->experimentId)){
//        // successful creation of node1 within experiment 1
//        nodeId = idOf(body)
//        body should include ("_id")
//        status should equal(200)
//      }
//      get("/metadata/add",Map("id"->nodeId,"name"->"wheat type","value"->"Janz")){
//        status must equal(200)
//      }
//      get("/metadata/add",Map("id"->nodeId,"name"->"watering","value"->"Rainfed")){
//        status must equal(200)
//      }
//      get("/metadata/add",Map("id"->nodeId,"name"->"altitude","value"->"100m")){
//        status must equal(200)
//      }
//      get("/metadata/add",Map("id"->nodeId,"name"->"status","value"->"active","start-ts"->"2012-01-01T21:42:26.220+10:00")){
//        status must equal(200)
//      }
//      post("/streams",Map("name"->"stream1","nid"->nodeId,"mid"->units.head.apply("_id"),"description"->"This stream captures temperature measurements in my unit.")){
//        // successful creation of stream1 within exp1
//        body should include ("_id")
//        body should include ("token")
//        streamId = idOf(body)
//        status should equal(200)
//      }
//      post("/streams",Map("name"->"stream2","nid"->nodeId,"mid"->units.head.apply("_id"),"description"->"This stream captures temperature measurements in my unit.")){
//        // successful creation of stream1 within exp1
//        body should include ("_id")
//        body should include ("token")
//        streamId = idOf(body)
//        status should equal(200)
//      }
//      post("/nodes",Map("name"->"node 2","eid"->experimentId)){
//        // successful creation of node1 within experiment 1
//        nodeId = idOf(body)
//        body should include ("_id")
//        status should equal(200)
//      }
//      post("/streams",Map("name"->"stream1","nid"->nodeId,"mid"->units.head.apply("_id"),"description"->"This stream captures temperature measurements in my unit.")){
//        // successful creation of stream1 within exp1
//        body should include ("_id")
//        body should include ("token")
//        streamId = idOf(body)
//        status should equal(200)
//      }
//      post("/streams",Map("name"->"stream3","nid"->nodeId,"mid"->units.head.apply("_id"))){
//        // successful creation of stream1 within exp1
//        body should include ("_id")
//        body should include ("token")
//        streamId = idOf(body)
//        status should equal(200)
//      }
//      post("/experiments",Map("name"->"Ginninderra","timezone"->"Australia/Sydney")){
//        body should include ("_id")
//        experimentId=idOf(body)
//        status should equal(200)
//      }
//      post("/nodes",Map("name"->"node2","eid"->experimentId)){
//        // successful creation of node1 within experiment 1
//        nodeId = idOf(body)
//        body should include ("_id")
//        status should equal(200)
//      }
//      post("/streams",Map("name"->"stream1","nid"->nodeId,"mid"->units.head.apply("_id"))){
//        // successful creation of stream1 within exp1
//        body should include ("_id")
//        body should include ("token")
//        streamId = idOf(body)
//        status should equal(200)
//      }
//      post("/streams",Map("name"->"stream2","nid"->nodeId,"mid"->units.head.apply("_id"),"description"->"This stream captures temperature measurements in my unit.")){
//        // successful creation of stream1 within exp1
//        body should include ("_id")
//        body should include ("token")
//        streamId = idOf(body)
//        status should equal(200)
//      }
//      post("/nodes",Map("name"->"EGA Gregory Irrigated","eid"->experimentId)){
//        // successful creation of node1 within experiment 1
//        nodeId = idOf(body)
//        body should include ("_id")
//        status should equal(200)
//      }
//      post("/nodes",Map("name"->"node 3","eid"->experimentId,"description"->"This node captures temperature measurements in my unit.")){
//        // successful creation of node1 within experiment 1
//        status should equal(200)
//        nodeId = idOf(body)
//        body should include ("_id")
//      }
//      post("/streams",Map("name"->"stream1","nid"->nodeId,"mid"->units.head.apply("_id"))){
//        // successful creation of stream1 within exp1
//        body should include ("_id")
//        body should include ("token")
//        streamId = idOf(body)
//        status should equal(200)
//      }
//      post("/streams",Map("name"->"stream3","nid"->nodeId,"mid"->units.head.apply("_id"))){
//        // successful creation of stream1 within exp1
//        body should include ("_id")
//        body should include ("token")
//        streamId = idOf(body)
//        status should equal(200)
//      }
//    }
//
//  }
//
//}
