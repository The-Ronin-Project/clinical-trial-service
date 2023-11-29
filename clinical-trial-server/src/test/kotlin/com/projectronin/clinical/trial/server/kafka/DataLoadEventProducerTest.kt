package com.projectronin.clinical.trial.server.kafka

import com.projectronin.kafka.data.RoninEvent
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.kafka.core.KafkaTemplate

internal class DataLoadEventProducerTest {

    @Test
    fun `creates event`() {
        val template = mockk<KafkaTemplate<String, RoninEvent<*>>> {
            every { send(any(), any(), any()) } returns mockk()
        }
        val producer = DataLoadEventProducer(template)
        assertDoesNotThrow { producer.producePatientResourceRequest("12345", "tenant") }
    }
}
