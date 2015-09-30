package sample.reactivekafka

import akka.actor.{ ActorSystem, Props }

object Application extends App with CommandLineParser {

  commandLineParser.parse(args, Config()) map { config =>
    val system = ActorSystem("CurrencyWatcher")
    println(s"""
                  |Starting Kafka with:
                  |INOIO_KAFKA_IP : ${config.kafkaIp}
                  |INOIO_ZK_IP    : ${config.zkIp}""".stripMargin)
    val coordinator = system.actorOf(Props(new Coordinator(config)))
    coordinator ! "Start"
  }
}
