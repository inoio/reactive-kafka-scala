package sample.reactivekafka

import java.util.{ Properties, UUID }

import akka.actor._
import akka.stream.ActorMaterializer

import scala.language.postfixOps
import scala.concurrent.duration._

class Coordinator(kafkaIp: String, zkIp: String) extends Actor with ActorLogging {

  val topicName = UUID.randomUUID().toString
  var writer: Option[ActorRef] = None
  var reader: Option[ActorRef] = None
  val materializer = ActorMaterializer()(context)

  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case "Start" =>
      log.debug("Starting the coordinator")
      writer = Some(context.actorOf(Props(new KafkaWriterCoordinator(materializer, topicName, kafkaIp))))
      reader = Some(context.actorOf(Props(new KafkaReaderCoordinator(materializer, topicName, kafkaIp, zkIp))))
    case "Reader initialized" =>
      log.debug("Reader initialized")
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

