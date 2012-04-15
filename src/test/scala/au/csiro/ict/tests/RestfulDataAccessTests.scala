package au.csiro.ict.tests

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, FunSuite}
import au.csiro.ict.Cache._
import org.bson.types.ObjectId

class RestfulDataAccessTests extends ScalatraSuite with FunSuite with BeforeAndAfter with BeforeAndAfterAll{
  val stream_token1 =  "STREAM01-b494-497b-a95e-b27824098057"
  val stream_token2 =  "STREAM02-b494-497b-a95e-b27824098059"

  val measurementId = new ObjectId()

  val user1Id = addUser("u1","pass1","000","u1@example.com","","","").get
  val user2Id = addUser("u2","pass2","000","u1@example.com","","","").get
  val exp1Id = addExperiment("exp1",user1Id,"000","","","","").get
  val exp2Id = addExperiment("exp2",user2Id,"000","","","","").get
  val node1Id =addNode("node1",user1Id,exp1Id,"-1","-1","-1","","","").get
  val node2Id = addNode("node2",user2Id,exp2Id,"-1","-1","-1","","","").get
  val stream1Id = addStream("stream1",user1Id,node1Id,measurementId,"","","",Some(stream_token1)).get
  val stream2Id = addStream("stream2",user2Id,node2Id,measurementId,"","","",Some(stream_token2)).get

  override def beforeAll(configMap: Map[String, Any]) {


  }
  override def afterAll(configMap: Map[String, Any]) {
    delUser(user1Id)
    delUser(user2Id)
  }
  test("Validators") {

    1 should equal(1)
  }
}
