package sample.reactivekafka

import akka.actor.ActorLogging
import akka.stream.actor.{ ActorPublisher, ActorPublisherMessage }

class CurrencyRatePublisher extends ActorPublisher[CurrencyRateUpdated] with ActorLogging {

  override def receive: Receive = {
    case ActorPublisherMessage.Request(_) => sendRates()
    case ActorPublisherMessage.Cancel     => context.stop(self)
    case _                                =>
  }

  def sendRates(): Unit = {
    while (isActive && totalDemand > 0) {
      val randomUpdate = RandomCurrencyRateChangeGenerator.randomPair()
      onNext(randomUpdate)
      log.info(s"write ${randomUpdate}")
      Thread.sleep(50)
    }
  }
}
