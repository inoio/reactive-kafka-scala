package sample.reactivekafka

import java.util

import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.{ Deserializer, Serializer }

/**
 * Created by magro on 10/2/15.
 */
object Serializers {

  class LongSerializer extends Serializer[Long] {
    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

    override def serialize(topic: String, data: Long): Array[Byte] =
      BigInt(data).toByteArray

    override def close(): Unit = ()
  }

  class LongDeserializer extends Deserializer[Long] {
    override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

    override def deserialize(topic: String, data: Array[Byte]): Long = BigInt(data).toLong

    override def close(): Unit = ()
  }

}
