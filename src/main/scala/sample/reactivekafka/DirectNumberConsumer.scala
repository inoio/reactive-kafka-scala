package sample.reactivekafka

import akka.actor.SupervisorStrategy.Resume
import akka.actor._
import akka.event.LoggingReceive
import kafka.api.TopicMetadataRequest
import kafka.consumer.SimpleConsumer
import org.apache.kafka.clients.consumer.{ ConsumerConfig, KafkaConsumer }
import org.apache.kafka.clients.producer._

import scala.collection.JavaConversions._

/*

class DirectNumberConsumer(config: Config) extends Actor with ActorLogging {

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
    case e: Exception =>
      // here you can handle your failing Kafka writes
      log.error(s"Read failed! ${e}")
      Resume
  }

  private var consumer: KafkaConsumer[String, Long] = _

  override def preStart(): Unit = {
    super.preStart()
    consumer = initConsumer()
    context.parent ! "Reader initialized"
    self ! "Consumer"
  }

  override def receive: Receive = LoggingReceive {
    case "Consumer" => produce(consumer, numMessages)
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    consumer.close()
  }

  private def produce(producer: KafkaProducer[String, Long], numMessages: Int): Unit = {
    (1 to numMessages).foreach { messageNum =>
      val message = new ProducerRecord[String, Long](config.topic, null, messageNum)
      val future = producer.send(message, new Callback {
        override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
          if (metadata != null) {
            log.info(s"$messageNum onCompletion offset ${metadata.offset()}, partition ${metadata.partition()}")
          }
          if (exception != null) {
            log.error(exception, s"$messageNum onCompletion received error")
          }
        }
      })
    }
  }

  private def initConsumer(): KafkaConsumer[String, Long] = {
    log.debug(s"Config : $config")

    val props = Map(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> config.kafkaIp,
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[Serializers.LongDeserializer].getName,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> "org.apache.kafka.common.serialization.StringDeserializer"
    )

    new KafkaConsumer[String, Long](props)
  }

  private def findLeader(seedBroker: String, a_port: Int, a_topic: String, a_partition: Int): PartitionMetadata = {
    // var returnMetaData: PartitionMetadata = null
    // loop:

    val consumer: SimpleConsumer = new SimpleConsumer(seedBroker, a_port, soTimeout = 100000, bufferSize = 64 * 1024, clientId = "leaderLookup")
    val req = new TopicMetadataRequest(List(a_topic), correlationId = 0)
    val resp = consumer.send(req)
    /*
    val returnMetaData = resp.topicsMetadata.collectFirst {
      case topicMeta if topicMeta.partitionsMetadata.exists(_.partitionId == a_partition) =>
        topicMeta.partitionsMetadata.find(_.partitionId == a_partition).head
    }*/
    val returnMetaData = resp.topicsMetadata.collectFirst {
      case topicMeta if topicMeta.partitionsMetadata.exists(_.partitionId == a_partition) =>
        topicMeta.partitionsMetadata.find(_.partitionId == a_partition).head
    }

    returnMetaData.map(_.partitionsMetadata.)
    /*
    } catch (Exception e) {
      System.out.println("Error communicating with Broker [" + seed + "] to find Leader for [" + a_topic
        + ", " + a_partition + "] Reason: " + e);
    }
    */
    if (returnMetaData != null) {
      m_replicaBrokers.clear();
      for (kafka.cluster.Broker replica : returnMetaData.replicas()) {
        m_replicaBrokers.add(replica.host());
      }
    }
    return returnMetaData;
  }

}

object DirectNumberConsumer {
  def props(config: Config): Props = Props(new DirectNumberConsumer(config))
}

*/ 