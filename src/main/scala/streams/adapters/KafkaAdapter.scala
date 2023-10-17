package streams.adapters


//import java.util.Properties
//import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
//import generator.ports.MessagePublisher
//
//class KafkaAdapter(broker: String, topic: String) extends MessagePublisher {
//
//  private val props = new Properties()
//  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker)
//  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
//  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
//
//  private val producer = new KafkaProducer[String, String](props)
//
//  override def publish(key: String, message: String): Unit = {
//    val record = new ProducerRecord[String, String](topic, key, message)
//    producer.send(record)
//  }
//
//  sys.addShutdownHook(producer.close())
//}

