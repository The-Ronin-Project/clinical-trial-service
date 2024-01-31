package com.projectronin.clinical.trial.server.controller

import com.projectronin.clinical.trial.models.Subject
import com.projectronin.clinical.trial.server.BaseIT
import com.projectronin.clinical.trial.server.data.binding.SiteDOs
import com.projectronin.clinical.trial.server.data.binding.StudyDOs
import com.projectronin.clinical.trial.server.data.binding.StudySiteDOs
import com.projectronin.clinical.trial.server.data.binding.SubjectDOs
import com.projectronin.clinical.trial.server.data.binding.SubjectStatusDOs
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.event.interop.resource.request.v1.InteropResourceRequestV1
import com.projectronin.kafka.data.RoninEvent
import com.projectronin.kafka.serde.RoninEventDeserializer
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktorm.dsl.deleteAll
import org.ktorm.dsl.insert
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Properties
import java.util.UUID

class SubjectControllerIT : BaseIT() {
    private val studyId = "009C2CB46A6F458F9BE7082193A75128"
    private val siteId = "F1AEC0AE8C6F44A6B0A4E50015D4ABED"

    private val subjectId = "subjectId"
    private val subjectNumber = "001-001"
    private val studySiteID = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770")

    private val consumer: KafkaConsumer<String, RoninEvent<*>> by lazy {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092") // Kafka broker address
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.springframework.kafka.support.serializer.ErrorHandlingDeserializer")
            put("spring.deserializer.value.delegate.class", RoninEventDeserializer::class.java.name)
            put("ronin.json.deserializer.topics", "oci.us-phoenix-1.interop-mirth.resource-request.v1:com.projectronin.event.interop.resource.request.v1.InteropResourceRequestV1")
            put(ConsumerConfig.GROUP_ID_CONFIG, "clinical-trial-service-it") // Consumer group ID
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest") // or "latest", based on your requirement
        }
        KafkaConsumer<String, RoninEvent<*>>(props)
    }

    private fun seedDB() {
        database.insert(StudyDOs) {
            set(it.studyId, studyId)
        }

        database.insert(SiteDOs) {
            set(it.siteId, siteId)
            set(it.roninTenantMnemonic, "test")
        }

        database.insert(StudySiteDOs) {
            set(it.studySiteId, studySiteID)
            set(it.studyId, studyId)
            set(it.siteId, siteId)
        }

        database.insert(SubjectDOs) {
            set(it.subjectId, subjectId)
            set(it.roninPatientId, "roninPatientId")
            set(it.subjectNumber, subjectNumber)
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

    @Test
    fun `get fails auth`() {
        val response = runBlocking {
            httpClient.get("$serverUrl/subject&activeIdsOnly=true") {
                contentType(ContentType.Application.Json)
            }
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `create fails auth`() {
        val subject = Subject(
            roninFhirId = "tenant-fhirId",
            siteId = "siteId",
            studyId = "studyId",
            number = "subjectNumber"
        )
        val response = runBlocking {
            httpClient.post("$serverUrl/subjects") {
                setBody(subject)
                contentType(ContentType.Application.Json)
            }
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `get active subjects with subjects`() {
        seedDB()

        val subject = Subject(
            roninFhirId = "tenant-fhirId",
            siteId = siteId,
            studyId = studyId,
            number = subjectNumber
        )

        runBlocking {
            client.createSubject(subject)
        }

        val response = runBlocking {
            client.getSubjects(true)
        }

        assertTrue(response.isNotEmpty())
    }

    @Test
    fun `get active subject by roninFhirId`() {
        seedDB()

        val subject = Subject(
            roninFhirId = "tenant-fhirId",
            siteId = siteId,
            studyId = studyId
        )

        runBlocking {
            client.createSubject(subject)
        }

        val response = runBlocking {
            client.getSubjectById(roninFhirId = "tenant-fhirId")
        }

        assertEquals(response?.roninFhirId, subject.roninFhirId)
    }

    @Test
    fun `create a subject`() {
        seedDB()
        consumer.subscribe(listOf("oci.us-phoenix-1.interop-mirth.resource-request.v1"))
        val subject = Subject(
            roninFhirId = "tenant-fhirId",
            siteId = siteId,
            studyId = studyId
        )

        val expectedSubjectId = "0CEE019CE0A9460BB9291A29EB67719B"

        val response = runBlocking {
            client.createSubject(subject)
        }

        assertTrue(response.id.isNotEmpty())
        assertTrue(response.number.isNotEmpty())
        assertEquals(SubjectStatus.ACTIVE.toString(), response.status)
        assertEquals(subject.roninFhirId, response.roninFhirId)
        assertEquals(subject.siteId, response.siteId)
        assertEquals(subject.studyId, response.studyId)
        assertEquals(expectedSubjectId, response.id)
        val records = consumer.poll(Duration.ofSeconds(5)).map { it.value().data as InteropResourceRequestV1 }
        assertTrue(records.any { it.resourceFHIRId == "tenant-fhirId" })
    }
}
