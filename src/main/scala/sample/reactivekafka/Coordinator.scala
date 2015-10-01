package sample.reactivekafka

import akka.actor._
import akka.stream.ActorMaterializer

import scala.language.postfixOps
import scala.concurrent.duration._
import scalaz.std.option._
import scalaz.syntax.std.option._

class Coordinator(config: Config) extends Actor with ActorLogging {

  import Coordinator._

  val topicName = config.topic
  var writer: Option[ActorRef] = none
  var reader: Option[ActorRef] = none
  val materializer = ActorMaterializer()(context)

  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case msg @ InitialMessage("Start", mode) =>
      log.debug(s"Starting the coordinator with $msg")
      mode match {
        case Mode.write | Mode.readwrite =>
          log.debug("Creating writer actor")
          writer = Some(context.actorOf(Props(new KafkaWriterCoordinator(materializer, config))))
        case _ => writer = none
      }
      mode match {
        case Mode.read | Mode.readwrite =>
          log.debug("Creating reader actor")
          reader = Some(context.actorOf(Props(new KafkaReaderCoordinator(materializer, config))))
        case _ => writer = none
      }

    case msg: InitialMessage =>
      log.error(s"Did not understand $msg")
      log.error("Shutting down")
      context.system.shutdown()

    case "Reader initialized" =>
      log.debug("Reader initialized")
      context.system.scheduler.scheduleOnce(5 seconds, self, "Stop")
    case "Writer initialized" =>
      log.debug("Writer initialized")
      context.system.scheduler.scheduleOnce(5 seconds, self, "Stop")
    case "Stop" =>
      log.debug("Stopping the coordinator")
      writer.foreach(actor => actor ! "Stop")
      reader.foreach(actor => context.stop(actor))
      context.system.scheduler.scheduleOnce(5 seconds, self, "Shutdown")
    case "Shutdown" =>
      log.debug("Shutting down the app")
      context.system.shutdown()
  }
}

case object Coordinator {
  case class InitialMessage(name: String, mode: Mode)
}
