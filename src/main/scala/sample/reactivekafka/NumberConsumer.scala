package sample.reactivekafka

import java.util.Properties

import akka.actor.SupervisorStrategy.{ Escalate, Resume }
import akka.actor._
import akka.event.LoggingReceive

import kafka.consumer.{ ConsumerConnector, ConsumerConfig, KafkaStream }
import kafka.message.MessageAndMetadata
import org.apache.kafka.common.serialization.StringDeserializer
import sample.reactivekafka.NumberConsumer.Consume
import sample.reactivekafka.Serializers.LongDeserializer

import scala.collection.JavaConversions._
import scala.util.{ Failure, Success, Try }

class NumberConsumer(config: Config) extends Actor with ActorLogging {

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e: Exception =>
      // here you can handle your failing Kafka writes
      log.error(s"Read failed! $e")
      //Resume
      Escalate
  }

  private var consumer: Try[ConsumerConnector] = _
  // private val partitions = config.props("partitions").split(";").map(_.toInt)

  override def preStart(): Unit = {
    super.preStart()
    consumer = Try(kafka.consumer.Consumer.create(consumerConfig))
    consumer match {
      case Success(c) =>
        context.parent ! "Reader initialized"
        self ! "Consume"
      case Failure(e) =>
        log.error(e, "Could not create KafkaConsumer")
        context.parent ! "Shutdown"
    }
  }

  override def receive: Receive = LoggingReceive {
    case "Consume" => consumer.foreach { (consumer: ConsumerConnector) =>
      // val topicCountMap = Map[String, Int](config.topic, 1)
      // Map<String, List<KafkaStream<byte[], byte[]>>>
      val consumerMap = consumer.createMessageStreams(Map(config.topic -> 1), Decoder(config.topic, new StringDeserializer), Decoder(config.topic, new LongDeserializer))
      val streams = consumerMap(config.topic)
      log.info(s"Got streams ${streams.length} $streams")
      streams.foreach { kafkaStream =>
        self ! Consume(kafkaStream)
      }
    }
    case Consume(kafkaStream) =>
      log.info(s"Handling KafkaStream ${kafkaStream.clientId}")
      kafkaStream.iterator().foreach {
        case (msg: MessageAndMetadata[String, Long]) =>
          log.info(s"KafkaStream ${kafkaStream.clientId} received offset ${msg.offset}, partition ${msg.partition}, value: ${msg.message()}")
      }
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    consumer.foreach(_.shutdown())
  }

  private def consumerConfig: ConsumerConfig = {
    val props = new Properties() {
      put("zookeeper.connect", config.zkIp)
      put("group.id", config.group)
      put("zookeeper.session.timeout.ms", "400")
      put("zookeeper.sync.time.ms", "200")
      put("auto.commit.interval.ms", "1000")
    }
    log.debug(s"Config: $config, props: $props")
    new ConsumerConfig(props)
  }

}

object NumberConsumer {
  def props(config: Config): Props = Props(new NumberConsumer(config))

  private case class Consume(kafkaStream: KafkaStream[String, Long])
}