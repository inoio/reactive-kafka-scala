package sample.reactivekafka

import akka.actor.{ ActorSystem, Props }
import scalaz._
import Scalaz._

object Application extends App {

  val result = (sys.env.get("INOIO_KAFKA_IP") |@|
    sys.env.get("INOIO_ZK_IP")) {
      case (kafkaIp, zkIp) =>
        implicit val system = ActorSystem("CurrencyWatcher")
        println(s"""
Starting Kafka with:
INOIO_KAFKA_IP : $kafkaIp
INOIO_ZK_IP    : $zkIp
""")
        val coordinator = system.actorOf(Props(new Coordinator(kafkaIp, zkIp)))
        coordinator ! "Start"
    } getOrElse {
      println("You need to set INOIO_KAFKA_IP and INOIO_ZK_IP to their respective values")
    }

}
