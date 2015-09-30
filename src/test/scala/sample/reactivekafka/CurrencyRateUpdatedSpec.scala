package sample.reactivekafka

import org.scalatest.{ FlatSpec, Matchers }

class CurrencyRateUpdatedSpec extends FlatSpec with Matchers {
  import CurrencyRateUpdated._

  behavior of "currency rate en/decoder"

  it should "correctly transform object" in {
    // given
    val initialRate = CurrencyRateUpdated("EUR", "USD", BigDecimal.valueOf(3))

    // when
    val bytes = Encoder.encoder[CurrencyRateUpdated].toBytes(initialRate)
    val resultRate = Encoder.decoder[CurrencyRateUpdated].fromBytes(bytes)

    // then
    resultRate should equal(initialRate)
  }

}
