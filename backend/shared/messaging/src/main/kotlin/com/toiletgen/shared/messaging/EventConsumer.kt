package com.toiletgen.shared.messaging

import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.Properties

class EventConsumer(
    bootstrapServers: String,
    private val groupId: String,
    private val topics: List<String>,
) {
    private val logger = LoggerFactory.getLogger(EventConsumer::class.java)

    private val consumer: KafkaConsumer<String, String> by lazy {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }
        KafkaConsumer(props)
    }

    fun start(scope: CoroutineScope, handler: suspend (topic: String, key: String?, value: String) -> Unit) {
        consumer.subscribe(topics)
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val records = consumer.poll(Duration.ofMillis(500))
                    for (record in records) {
                        handler(record.topic(), record.key(), record.value())
                    }
                } catch (e: Exception) {
                    logger.error("Error consuming events", e)
                }
            }
        }
    }

    fun close() {
        consumer.close()
    }
}
