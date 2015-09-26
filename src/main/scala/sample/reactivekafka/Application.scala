package sample.reactivekafka

import akka.actor.{ ActorSystem, Props }
import scalaz._
import Scalaz._

object Application extends App with CommandLineParser {

  commandLineParser.parse(args, Config()) map { config =>
    implicit val system = ActorSystem("CurrencyWatcher")
    println(s"""
                  |Starting Kafka with:
                  |INOIO_KAFKA_IP : ${config.kafkaIp}
                  |INOIO_ZK_IP    : ${config.zkIp}""".stripMargin)
    val coordinator = system.actorOf(Props(new Coordinator(config)))
    coordinator ! "Start"
  }

}
