package sample.reactivekafka

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import akka.event.LoggingReceive
import org.apache.kafka.clients.producer._

import scala.collection.JavaConversions._

/**
  * Responsible for starting the writing stream.
  */
class NumberProducer(config: Config, numMessages: Int = 30) extends Actor with ActorLogging {

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e: Exception =>
      // here you can handle your failing Kafka writes
      log.error(s"Write failed! ${e}")
      Resume
  }

  private var producer: KafkaProducer[String, Long] = _

  override def preStart(): Unit = {
    super.preStart()
    producer = initProducer()
    context.parent ! "Writer initialized"
    self ! "Produce"
  }

  override def receive: Receive = LoggingReceive {
    case "Produce" => produce(producer, numMessages)
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    producer.close()
  }

  private def produce(producer: KafkaProducer[String, Long], numMessages: Int): Unit = {
    (1 to numMessages).foreach { messageNum =>
      val message = new ProducerRecord[String, Long](config.topic, null, messageNum)
      val future = producer.send(message, new Callback {
        override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
          val maybeMetadata = Option(metadata)
          val maybeException = Option(exception)
          if (maybeMetadata.isDefined) {
            log.info(s"$messageNum onCompletion offset ${metadata.offset()}, partition ${metadata.partition()}")
          }
          if (maybeException.isDefined) {
            log.error(exception, s"$messageNum onCompletion received error")
          }
        }
      })
    }
  }

  private def initProducer(): KafkaProducer[String, Long] = {
    log.debug(s"Config : $config")

    val props = Map(
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> config.kafkaIp,
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> classOf[Serializers.LongSerializer].getName, // "org.apache.kafka.common.serialization.StringSerializer"
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringSerializer"
    )

    new KafkaProducer[String, Long](props)
  }

}

object NumberProducer {
  def props(config: Config, numMessages: Int = 30): Props = Props(new NumberProducer(config))
}