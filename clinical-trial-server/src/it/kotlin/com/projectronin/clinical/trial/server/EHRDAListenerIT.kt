package com.projectronin.clinical.trial.server

import com.projectronin.clinical.trial.models.Subject
import com.projectronin.clinical.trial.server.data.binding.SiteDOs
import com.projectronin.clinical.trial.server.data.binding.StudyDOs
import com.projectronin.clinical.trial.server.data.binding.StudySiteDOs
import com.projectronin.clinical.trial.server.data.binding.SubjectDOs
import com.projectronin.clinical.trial.server.data.binding.SubjectStatusDOs
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.ronin.generators.resource.rcdmPatient
import com.projectronin.kafka.data.RoninEvent
import com.projectronin.kafka.serde.RoninEventSerializer
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktorm.dsl.deleteAll
import org.ktorm.dsl.insert
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Properties
import java.util.UUID

class EHRDAListenerIT : BaseIT() {
    private val studyId = "studyId"
    private val siteId = "siteId"

    private val subjectId = "subjectId"
    private val studySiteID = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770")

    private fun seedDB() {
        database.insert(StudyDOs) {
            set(it.studyId, studyId)
        }

        database.insert(SiteDOs) {
            set(it.siteId, siteId)
            set(it.roninTenantMnemonic, "ronin")
        }

        database.insert(StudySiteDOs) {
            set(it.studySiteId, studySiteID)
            set(it.studyId, studyId)
            set(it.siteId, siteId)
        }

        database.insert(SubjectDOs) {
            set(it.subjectId, subjectId)
            set(it.roninPatientId, "ronin-PatientId1")
        }

        database.insert(SubjectStatusDOs) {
            set(it.studySiteId, studySiteID)
            set(it.subjectId, subjectId)
            set(it.status, SubjectStatus.ACTIVE)
            set(it.createdDateTime, OffsetDateTime.now(ZoneOffset.UTC))
            set(it.updatedDateTime, OffsetDateTime.now(ZoneOffset.UTC))
        }
    }

    @BeforeEach
    fun clearDB() {
        database.deleteAll(SubjectStatusDOs)
        database.deleteAll(SubjectDOs)
        database.deleteAll(StudySiteDOs)
        database.deleteAll(SiteDOs)
        database.deleteAll(StudyDOs)
    }

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
        seedDB()
        val subject = Subject(
            roninFhirId = "ronin-PatientId2",
            siteId = siteId,
            studyId = studyId
        )

        runBlocking {
            client.createSubject(subject)
        }
        val patient = rcdmPatient("ronin") {
            id of Id("ronin-PatientId2")
        }
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
