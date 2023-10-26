package com.projectronin.clinical.trial.server

import com.projectronin.interop.fhir.generators.primitives.of
import com.projectronin.interop.fhir.generators.resources.patient
import com.projectronin.kafka.data.RoninEvent
import com.projectronin.kafka.serde.RoninEventSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Properties

class EHRDAListenerIT : BaseIT() {

    private val producer: KafkaProducer<String, RoninEvent<*>> by lazy {
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092") // Update with your Kafka broker address
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                RoninEventSerializer::class.java.name
            )
        }
        KafkaProducer<String, RoninEvent<*>>(props)
    }

    @Test
    fun `listens to patient topic`() {
        val patient = patient { id of "InitTestPatient" }
        val event = RoninEvent(
            specVersion = "1.0",
            dataSchema = "dataSchema",
            dataContentType = "dataContentType",
            source = "integrationTest",
            type = "type",
            data = patient,
            subject = "subject"
        )
        producer.send(ProducerRecord("oci.us-phoenix-1.ehr-data-authority.patient.v1", event)).get()
        assertTrue(true)
    }
}
