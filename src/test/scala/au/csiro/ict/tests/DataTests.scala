package au.csiro.ict.tests

import org.scalatra.test.scalatest.ScalatraSuite
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, FunSuite}

class RestfulDataAccessTests extends ScalatraSuite with FunSuite with BeforeAndAfter with BeforeAndAfterAll{
  override def beforeAll(configMap: Map[String, Any]) {

  }
  override def afterAll(configMap: Map[String, Any]) {

  }
  test("Validators") {
    1 should equal(1)
  }
}
