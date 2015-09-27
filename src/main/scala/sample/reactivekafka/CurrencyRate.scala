package sample.reactivekafka

import play.api.libs.json._
import kafka.serializer._

case class CurrencyRateUpdated(base: String, counter: String, percentUpdate: BigDecimal) {
  def asKeyValue = (base, counter) -> percentUpdate
}

case object CurrencyRateUpdated {
  implicit val currencyRateUpdateFormat: Format[CurrencyRateUpdated] = Json.format[CurrencyRateUpdated]
}

trait EncoderInstances {
  implicit def encoder[A](implicit W: Writes[A]): Encoder[A] = new Encoder[A] {
    override def toBytes(a: A): Array[Byte] = Json.toJson(a).toString.getBytes("UTF-8")
  }

  implicit def decoder[A](implicit R: Reads[A]): Decoder[A] = new Decoder[A] {
    override def fromBytes(bytes: Array[Byte]): A = Json.parse(bytes).as[A]
  }

}

object Encoder extends EncoderInstances {
  def apply[A](implicit E: Encoder[A]): Encoder[A] = E
}

object Decoder extends EncoderInstances {
  def apply[A](implicit D: Decoder[A]): Decoder[A] = D
}