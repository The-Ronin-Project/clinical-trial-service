package com.projectronin.clinical.trial.server

import com.projectronin.clinical.trial.models.Subject
import com.projectronin.clinical.trial.server.data.binding.SiteDOs
import com.projectronin.clinical.trial.server.data.binding.StudyDOs
import com.projectronin.clinical.trial.server.data.binding.StudySiteDOs
import com.projectronin.clinical.trial.server.data.binding.SubjectDOs
import com.projectronin.clinical.trial.server.data.binding.SubjectStatusDOs
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.clinical.trial.server.dataauthority.ObservationDAO
import com.projectronin.interop.fhir.generators.datatypes.DynamicValues
import com.projectronin.interop.fhir.generators.datatypes.codeableConcept
import com.projectronin.interop.fhir.generators.datatypes.coding
import com.projectronin.interop.fhir.generators.primitives.of
import com.projectronin.interop.fhir.generators.resources.patient
import com.projectronin.interop.fhir.r4.CodeSystem
import com.projectronin.interop.fhir.r4.CodeableConcepts
import com.projectronin.interop.fhir.r4.datatype.Identifier
import com.projectronin.interop.fhir.r4.datatype.primitive.Code
import com.projectronin.interop.fhir.r4.datatype.primitive.Date
import com.projectronin.interop.fhir.r4.datatype.primitive.DateTime
import com.projectronin.interop.fhir.r4.datatype.primitive.FHIRString
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.r4.datatype.primitive.asFHIR
import com.projectronin.interop.fhir.ronin.generators.resource.observation.rcdmObservationLaboratoryResult
import com.projectronin.interop.fhir.ronin.generators.util.rcdmReference
import com.projectronin.kafka.data.RoninEvent
import com.projectronin.kafka.serde.RoninEventSerializer
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktorm.dsl.deleteAll
import org.ktorm.dsl.insert
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Properties
import java.util.UUID

class EHRDAListenerIT : BaseIT() {
    private val studyId = "009C2CB46A6F458F9BE7082193A75128"
    private val siteId = "F1AEC0AE8C6F44A6B0A4E50015D4ABED"

    private val subjectId = "subjectId"
    private val studySiteID = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770")
    private val observationDAO = ObservationDAO(ctdaDatabase)

    private fun seedDB() {
        database.insert(StudyDOs) {
            set(it.studyId, studyId)
        }

        database.insert(SiteDOs) {
            set(it.siteId, siteId)
            set(it.roninTenantMnemonic, "ronincer")
        }

        database.insert(StudySiteDOs) {
            set(it.studySiteId, studySiteID)
            set(it.studyId, studyId)
            set(it.siteId, siteId)
        }

        database.insert(SubjectDOs) {
            set(it.subjectId, subjectId)
            set(it.roninPatientId, "ronincer-PatientId1")
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
            roninFhirId = "ronincer-PatientId2",
            siteId = siteId,
            studyId = studyId
        )

        runBlocking {
            client.createSubject(subject)
        }
        val patient = patient {
            id of Id("ronincer-PatientId2")
            birthDate of Date("01-01-1999")
            identifier of listOf(
                Identifier(
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "ronincer-PatientId2".asFHIR(),
                    type = CodeableConcepts.RONIN_FHIR_ID
                )
            )
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
        // wait for listener to process message
        do {
            Thread.sleep(5000)
        } while (observationDAO.search(valueSetIds = listOf("10f8c49a-635b-4928-aee6-f6e47c2e7c50")).isEmpty())
        val actual = observationDAO.search(valueSetIds = listOf("10f8c49a-635b-4928-aee6-f6e47c2e7c50")).first()
        assertEquals(DateTime("01-01-1999"), actual.value!!.value)
    }

    @Test
    fun `listens to observation topic`() {
        seedDB()
        val testSubject = Subject(
            roninFhirId = "ronincer-PatientId3",
            siteId = siteId,
            studyId = studyId
        )

        runBlocking {
            client.createSubject(testSubject)
        }

        val observation = rcdmObservationLaboratoryResult("ronincer") {
            subject of rcdmReference("Patient", "ronincer-PatientId3")
            code of codeableConcept {
                coding of listOf(
                    coding {
                        system of "http://loinc.org"
                        version of "2.74"
                        code of Code("8302-2")
                        display of "Body height"
                    }
                )
            }
            value of DynamicValues.string("1")
            identifier of listOf(
                Identifier(
                    system = CodeSystem.RONIN_FHIR_ID.uri,
                    value = "ronincer-PatientId3".asFHIR(),
                    type = CodeableConcepts.RONIN_FHIR_ID
                )
            )
            effective of DynamicValues.dateTime("2023-01-01")
        }

        val event = RoninEvent(
            specVersion = "1.0",
            dataSchema = "dataSchema",
            dataContentType = "dataContentType",
            source = "integrationTest",
            type = "type",
            data = observation,
            subject = "subject"
        )
        producer.send(ProducerRecord("oci.us-phoenix-1.ehr-data-authority.observation.v1", event)).get()
        // wait for listener to process message
        do {
            Thread.sleep(5000)
        } while (observationDAO.search(valueSetIds = listOf("370b5c79-71f1-4f00-ab3d-cd7d430f813b")).isEmpty())
        val actual = observationDAO.search(valueSetIds = listOf("370b5c79-71f1-4f00-ab3d-cd7d430f813b")).first()
        assertEquals(FHIRString("1"), actual.value!!.value)
    }
}
