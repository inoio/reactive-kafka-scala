package sample
package reactivekafka

import scalaz.std.option._
import scalaz.syntax.std.option._

trait CommandLineParser {
  val commandLineParser = new scopt.OptionParser[Config]("reactive-kafka-scala") {
    head("reactive-kafka-scala", "0.0.1")
    opt[String]("kafka") text ("Kafka IP") action { (ip, config) => config.copy(kafkaIp = ip) }
    opt[String]("zk") text ("Zookeper IP") action { (ip, config) => config.copy(zkIp = ip) }
    opt[String]("topic") text ("topic") action { (t, config) => config.copy(topic = t.some) }
    opt[String]("group") text ("group") action { (g, config) => config.copy(group = g.some) }
    opt[Mode]("mode") text ("mode") action { (m, config) => config.copy(mode = m) }

    note(
      """
        |Read or write sample data to a Kafka instance
      """.stripMargin
    )
  }
}

sealed trait Mode

object Mode {

  implicit val modeRead: scopt.Read[Mode] =
    scopt.Read.reads(s => parse(s).getOrElse(throw new IllegalArgumentException(s"""Mode needs to be one of ${values.mkString(", ")}""")))

  private val values = List(read, write, readwrite)

  def parse(str: String): Option[Mode] = values.find(v => v.toString == str)

  case object read extends Mode
  case object write extends Mode
  case object readwrite extends Mode
}

case class Config(
  kafkaIp: String = sys.env.get("INOIO_KAFKA_IP").getOrElse(""),
  zkIp: String = sys.env.get("INOIO_ZK_IP").getOrElse(""),
  topic: Option[String] = java.util.UUID.randomUUID().toString.some,
  group: Option[String] = none,
  mode: Mode = Mode.readwrite
)
