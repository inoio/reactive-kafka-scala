package sample.reactivekafka

import akka.actor.{ ActorSystem, Props }

object Application extends App with CommandLineParser {

  commandLineParser.parse(args, Config()) map { config =>
    val system = ActorSystem("CurrencyWatcher")
    println(config)
    val coordinator = system.actorOf(Props(new Coordinator(config)))
    coordinator ! Coordinator.InitialMessage(config.msg, config.mode)
  }
}
