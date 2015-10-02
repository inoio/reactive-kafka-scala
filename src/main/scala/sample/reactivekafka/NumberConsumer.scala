package sample.reactivekafka

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import akka.event.LoggingReceive
import org.apache.kafka.clients.consumer.{ ConsumerConfig, KafkaConsumer }
import org.apache.kafka.clients.producer.RecordMetadata

import scala.collection.JavaConversions._
import scala.util.{ Failure, Success, Try }

class NumberConsumer(config: Config) extends Actor with ActorLogging {

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e: Exception =>
      // here you can handle your failing Kafka writes
      log.error(s"Read failed! ${e}")
      Resume
  }

  private var consumer: Try[KafkaConsumer[String, Long]] = _
  private val partitions = config.props("partitions").split(";").map(_.toInt)

  override def preStart(): Unit = {
    super.preStart()
    consumer = initConsumer()
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
    case "Consume" => consumer.foreach(consume(_, partitions))
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    consumer.foreach(_.close())
  }

  private def consume(consumer: KafkaConsumer[String, Long], partitions: Seq[Int]): Unit = {
    consumer.subscribe(config.topic)
    val commitInterval = 200
    val recordsByTopic = consumer.poll(100)
    recordsByTopic.foreach {
      case (topic, records) =>
        // Insert into db or anything else
        log.info(s"Received records ${records.records(partitions: _*).map(_.value()).mkString(",")}")
        consumer.commit(true)
        log.info("Done committing")
    }
    self ! "Consume"
  }

  private def initConsumer(): Try[KafkaConsumer[String, Long]] = {
    log.debug(s"Config : $config")

    val props = Map(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> config.kafkaIp,
      // The configuration partition.assignment.strategy = range was supplied but isn't a known config
      ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY -> "range", // Possible values: range, roundrobin.
      ConsumerConfig.GROUP_ID_CONFIG -> config.group,
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> "false", // we manage offsets ourselves
      // The configuration session.timeout.ms = 30000 was supplied but isn't a known config
      // ConsumerConfig.SESSION_TIMEOUT_MS -> "30000",
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[Serializers.LongDeserializer].getName,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer"
    )
    Try(new KafkaConsumer[String, Long](props))
  }

}

object NumberConsumer {
  def props(config: Config): Props = Props(new NumberConsumer(config))
}