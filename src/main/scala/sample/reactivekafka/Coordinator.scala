package sample.reactivekafka

import java.util.{ Properties, UUID }

import akka.actor._
import akka.stream.ActorMaterializer

import scala.language.postfixOps
import scala.concurrent.duration._
import scalaz.std.option._
import scalaz.syntax.std.option._

class Coordinator(config: Config) extends Actor with ActorLogging {

  val topicName = config.topic.getOrElse(UUID.randomUUID().toString)
  var writer: Option[ActorRef] = None
  var reader: Option[ActorRef] = None
  val materializer = ActorMaterializer()(context)

  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case "Start" =>
      log.debug(s"Starting the coordinator in mode ${config.mode}")
      config.mode match {
        case Mode.write | Mode.readwrite =>
          log.debug("Creating writer actor")
          writer = Some(context.actorOf(Props(new KafkaWriterCoordinator(materializer, config.copy(topic = topicName.some)))))
        case _ => writer = none
      }
      config.mode match {
        case Mode.read | Mode.readwrite =>
          log.debug("Creating reader actor")
          reader = Some(context.actorOf(Props(new KafkaReaderCoordinator(materializer, config.copy(topic = topicName.some)))))
        case _ => writer = none
      }
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

