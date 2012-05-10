//package au.csiro.ict.tests
//
//import au.csiro.ict.SDBMsg
//import akka.actor.Actor
//
//class MockActor extends Actor{
//  var messages = List[SDBMsg]()
//  def receive ={
//    case msg:SDBMsg => messages::=msg
//    case others => throw new Exception(others.toString)
//  }
//  def reset() = messages = List[SDBMsg]()
//}