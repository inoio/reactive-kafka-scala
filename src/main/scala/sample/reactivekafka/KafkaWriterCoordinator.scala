package sample.reactivekafka

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import akka.stream.Materializer
import akka.stream.actor.ActorSubscriberMessage.OnComplete
import akka.stream.actor.{ ActorPublisher, ActorSubscriber }
import akka.stream.scaladsl.{ Sink, Source }
import com.softwaremill.react.kafka.{ ProducerProperties, ReactiveKafka }
import org.reactivestreams.Publisher
import akka.event.LoggingReceive

/**
 * Responsible for starting the writing stream.
 */
class KafkaWriterCoordinator(mat: Materializer, config: Config) extends Actor with ActorLogging {
  import CurrencyRateUpdated._
  implicit lazy val materializer = mat

  var maybeKafkaProducer: Option[ActorRef] = None

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e: Exception =>
      // here you can handle your failing Kafka writes
      log.error(s"Write failed! ${e}")
      Resume
  }

  override def preStart(): Unit = {
    super.preStart()
    initWriter()
  }

  override def receive: Receive = LoggingReceive {
    case "Stop" =>
      log.debug("Stopping the writer coordinator")
      maybeKafkaProducer.foreach(actor => actor ! OnComplete)
  }

  def initWriter(): Unit = {
    log.debug(s"Config : $config")
    val producerProperties = ProducerProperties(
      brokerList = config.kafkaIp,
      topic = config.topic,
      encoder = Encoder.encoder[CurrencyRateUpdated]
    ).setProperties(config.props.toList: _*)

    val kafkaProducerProps = new ReactiveKafka().producerActorProps(producerProperties)
    log.info("ProducerProperties")
    log.info(producerProperties.dump)
    val kafkaProducer = context.actorOf(kafkaProducerProps)
    maybeKafkaProducer = Some(kafkaProducer)
    val generatorActor = context.actorOf(Props(new CurrencyRatePublisher))
    context.parent ! "Writer initialized"

    // Start the stream
    val publisher: Publisher[CurrencyRateUpdated] = ActorPublisher[CurrencyRateUpdated](generatorActor)
    Source(publisher).runWith(Sink(ActorSubscriber[CurrencyRateUpdated](kafkaProducer)))
  }

}
