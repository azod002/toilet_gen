package com.toiletgen.shared.messaging

import com.toiletgen.shared.events.DomainEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

class EventPublisher(bootstrapServers: String) {

    private val json = Json { encodeDefaults = true }

    private val producer: KafkaProducer<String, String> by lazy {
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.ACKS_CONFIG, "all")
        }
        KafkaProducer(props)
    }

    suspend fun publish(topic: String, event: DomainEvent) = withContext(Dispatchers.IO) {
        val payload = json.encodeToString(event)
        val record = ProducerRecord(topic, event.aggregateId, payload)
        producer.send(record).get()
    }

    /** Publish a pre-serialized payload (used by OutboxPoller). */
    suspend fun publishRaw(topic: String, key: String, payload: String) = withContext(Dispatchers.IO) {
        val record = ProducerRecord(topic, key, payload)
        producer.send(record).get()
    }

    fun close() {
        producer.close()
    }
}
