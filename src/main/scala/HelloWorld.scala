/**
  * Created by cjemison on 4/8/17.
  */

import akka.pattern.ask
import akka.actor._
import akka.util.Timeout

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

class HelloMessage(val name: String)

class GoodByeMessage(val name: String)

class MessageActor extends Actor {
  def receive = {
    case message: HelloMessage => {
      sender ! "Hello %s!".format(message.name)
    }
    case message: GoodByeMessage => {
      sender ! "Good Bye %s!".format(message.name)
    }
  }
}

class HelloActor(messageActor:ActorRef) extends Actor {
  def receive = {
    // (2) changed these println statements
    case message: HelloMessage => {
      implicit val timeout = Timeout(5 seconds)
      val future = messageActor ? message
      val result = Await.result(future, timeout.duration).asInstanceOf[String]
      println(result)
    }
    case message: GoodByeMessage => {
      messageActor ! message
    }
  }
}

object Main extends App {
  val system = ActorSystem("HelloSystem")


  val messageActor = system.actorOf(Props[MessageActor], name = "messageActor")
  val helloActor = system.actorOf(Props(new HelloActor(messageActor)), name = "helloActor")

  helloActor ! new HelloMessage("Cornelius")
 // helloActor ! new GoodByeMessage("Cornelius")
  //helloActor ! PoisonPill
  system.terminate()
}