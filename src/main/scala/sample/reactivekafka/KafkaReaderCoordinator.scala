package sample.reactivekafka

import akka.actor.{ Actor, ActorLogging }
import akka.stream.Supervision.Resume
import akka.stream.{ Supervision, ActorAttributes, Materializer }
import akka.stream.scaladsl.Source
import com.softwaremill.react.kafka.{ ConsumerProperties, PublisherWithCommitSink, ReactiveKafka }
import kafka.message.MessageAndMetadata
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.math.BigDecimal.RoundingMode
import akka.event.LoggingReceive

class KafkaReaderCoordinator(mat: Materializer, config: Config) extends Actor with ActorLogging {

  implicit val materializer = mat
  var consumerWithOffsetSink: PublisherWithCommitSink[CurrencyRateUpdated] = _

  override def preStart(): Unit = {
    super.preStart()
    initReader()
  }

  val processingDecider: Supervision.Decider = {
    case e: Exception =>
      log.error(e, "Error when processing exchange rates")
      Resume
  }

  override def receive: Receive = LoggingReceive {
    case msg => unhandled(msg)
  }

  def initReader(): Unit = {
    log.debug("initReader")
    implicit val actorSystem = context.system
    val consumerProperties = (ConsumerProperties(
      brokerList = config.kafkaIp,
      zooKeeperHost = config.zkIp,
      topic = config.topic,
      groupId = config.group,
      decoder = Decoder.decoder[CurrencyRateUpdated]
    ).kafkaOffsetsStorage()
      .commitInterval(100.milliseconds)
      .readFromEndOfStream()
      .setProperties(config.props.toList: _*))
    log.info("ConsumerProperies:")
    log.info(consumerProperties.dump)
    consumerWithOffsetSink = new ReactiveKafka().consumeWithOffsetSink(consumerProperties)
    log.debug("Starting the reader")
    Source(consumerWithOffsetSink.publisher)
      .map(processMessage)
      .withAttributes(ActorAttributes.supervisionStrategy(processingDecider))
      .to(consumerWithOffsetSink.offsetCommitSink).run()
    context.parent ! "Reader initialized"
  }

  def processMessage(msg: MessageAndMetadata[Array[Byte], CurrencyRateUpdated]) = {
    val pairAndRate = msg.message()
    log.info(s"Partition: ${msg.partition}")
    log.info(s"Offset: ${msg.offset}")
    log.info(s"Msg   : ${pairAndRate}")
    msg
  }

  override def postStop(): Unit = {
    consumerWithOffsetSink.cancel()
    super.postStop()
  }
}
