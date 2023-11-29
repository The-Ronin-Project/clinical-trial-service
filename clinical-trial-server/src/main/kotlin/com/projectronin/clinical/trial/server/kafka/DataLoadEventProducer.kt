package com.projectronin.clinical.trial.server.kafka

import com.projectronin.event.interop.resource.request.v1.InteropResourceRequestV1
import com.projectronin.kafka.data.RoninEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class DataLoadEventProducer(private val template: KafkaTemplate<String, RoninEvent<*>>) {

    fun producePatientResourceRequest(patientFhirID: String, tenantID: String) {
        val data = InteropResourceRequestV1(
            tenantId = tenantID,
            resourceFHIRId = patientFhirID,
            resourceType = "Patient",
            requestingService = "clinical-trial-service"
        )
        val subject = "ronin.interop-mirth.resource/$patientFhirID"
        val event = RoninEvent(
            specVersion = "1.0",
            dataSchema = "https://github.com/projectronin/contract-event-interop-resource-request/blob/main/v1/interop-resource-request-v1.schema.json",
            dataContentType = "application/json",
            source = "clinical-trial-service",
            type = "ronin.interop-mirth.resource.request",
            data = data,
            subject = subject
        )
        template.send("oci.us-phoenix-1.interop-mirth.resource-request.v1", subject, event)
    }
}
