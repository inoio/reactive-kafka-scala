package sample.reactivekafka

import play.api.libs.json._

case class CurrencyRateUpdated(base: String, counter: String, percentUpdate: BigDecimal) {
  def asKeyValue = (base, counter) -> percentUpdate
}

case object CurrencyRateUpdated {
  implicit val currencyRateUpdateFormat: Format[CurrencyRateUpdated] = Json.format[CurrencyRateUpdated]
}
