package com.projectronin.clinical.trial.server.controller

import com.projectronin.clinical.trial.server.BaseIT
import com.projectronin.clinical.trial.server.data.binding.SiteDOs
import com.projectronin.clinical.trial.server.data.binding.StudyDOs
import com.projectronin.clinical.trial.server.data.binding.StudySiteDOs
import com.projectronin.clinical.trial.server.data.binding.SubjectDOs
import com.projectronin.clinical.trial.server.data.binding.SubjectStatusDOs
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.clinical.trial.server.dataauthority.ObservationDAO
import com.projectronin.interop.fhir.generators.resources.observation
import com.projectronin.interop.fhir.r4.datatype.Coding
import com.projectronin.interop.fhir.r4.datatype.DynamicValue
import com.projectronin.interop.fhir.r4.datatype.DynamicValueType
import com.projectronin.interop.fhir.r4.datatype.Meta
import com.projectronin.interop.fhir.r4.datatype.Reference
import com.projectronin.interop.fhir.r4.datatype.primitive.DateTime
import com.projectronin.interop.fhir.r4.datatype.primitive.FHIRString
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ktorm.dsl.deleteAll
import org.ktorm.dsl.insert
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class ObservationControllerIT : BaseIT() {

    private val observationDAO = ObservationDAO(ctdaDatabase)
    private val studyId = "studyId"
    private val siteId = "siteId"
    private val subjectId = "subjectId"
    private val roninFhirId = "fhirId"
    private val studySiteID = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770")
    private val authentication = getAuth()
    private val testObservations1 = (1..8).toList().map {
        observation {
            id of Id("observationID-$it")
            subject of Reference(reference = FHIRString("Patient/$roninFhirId"))
            meta of Meta(tag = listOf(Coding(system = Uri("10f8c49a-635b-4928-aee6-f6e47c2e7c50"), display = FHIRString("Birth Date"))))
            effective of DynamicValue(DynamicValueType.DATE_TIME, DateTime("2023-12-01T00:00:00"))
        }
    }
    private val testObservations2 = (9..12).toList().map {
        observation {
            id of Id("observationID-$it")
            subject of Reference(reference = FHIRString("Patient/$roninFhirId"))
            meta of Meta(tag = listOf(Coding(system = Uri("daf6a5fc-5705-400b-abd0-852e060c9325"), display = FHIRString("Sex"))))
            effective of DynamicValue(DynamicValueType.DATE_TIME, DateTime("2023-12-01T00:00:00"))
        }
    }
    private val allObservations = listOf(testObservations1, testObservations2).flatten()

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
            set(it.roninPatientId, roninFhirId)
        }

        database.insert(SubjectStatusDOs) {
            set(it.studySiteId, studySiteID)
            set(it.subjectId, subjectId)
            set(it.status, SubjectStatus.ACTIVE)
            set(it.createdDateTime, OffsetDateTime.now(ZoneOffset.UTC))
            set(it.updatedDateTime, OffsetDateTime.now(ZoneOffset.UTC))
        }
        allObservations.forEach {
            observationDAO.insert(it)
        }
    }
    private fun clearDB() {
        database.deleteAll(SubjectStatusDOs)
        database.deleteAll(SubjectDOs)
        database.deleteAll(StudySiteDOs)
        database.deleteAll(SiteDOs)
        database.deleteAll(StudyDOs)
        allObservations.forEach { observationDAO.delete(it.findFhirId()!!) }
    }

    @BeforeEach
    fun resetDB() {
        clearDB()
        seedDB()
    }

    @Test
    fun `get observations - 200`() {
        val expectedResponse = GetObservationsResponse(
            subjectId,
            testObservations1,
            Pagination(1, 10, false, 8)
        )

        runBlocking {
            val response = httpClient.post("$serverUrl/studies/$studyId/sites/$siteId/subject/$subjectId/observations") {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${authentication.accessToken}")
                }
                setBody(
                    mapOf(
                        "observation_name" to listOf("10f8c49a-635b-4928-aee6-f6e47c2e7c50"),
                        "date_range" to mapOf("start_date" to "2023-11-11T00:00:00", "end_date" to "2023-12-12"),
                        "offset" to 1,
                        "limit" to 10,
                        "test_mode" to false
                    )
                )
            }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(expectedResponse, response.body<GetObservationsResponse>())
        }
    }

    @Test
    fun `get observations - 200 multiple pages and observation types`() {
        val expectedResponse1 = GetObservationsResponse(
            subjectId,
            testObservations1 + testObservations2.slice(0..1),
            Pagination(1, 10, true, 12)
        )
        val expectedResponse2 = GetObservationsResponse(
            subjectId,
            testObservations2.slice(2..3),
            Pagination(11, 10, false, 12)
        )

        runBlocking {
            val response = httpClient.post("$serverUrl/studies/$studyId/sites/$siteId/subject/$subjectId/observations") {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${authentication.accessToken}")
                }
                setBody(
                    mapOf(
                        "observation_name" to listOf("10f8c49a-635b-4928-aee6-f6e47c2e7c50", "daf6a5fc-5705-400b-abd0-852e060c9325"),
                        "date_range" to mapOf("start_date" to "2023-11-11T00:00:00", "end_date" to "2023-12-12"),
                        "offset" to 1,
                        "limit" to 10,
                        "test_mode" to false
                    )
                )
            }
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(expectedResponse1, response.body<GetObservationsResponse>())

            val response2 = httpClient.post("$serverUrl/studies/$studyId/sites/$siteId/subject/$subjectId/observations") {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${authentication.accessToken}")
                }
                setBody(
                    mapOf(
                        "observation_name" to listOf("10f8c49a-635b-4928-aee6-f6e47c2e7c50", "daf6a5fc-5705-400b-abd0-852e060c9325"),
                        "date_range" to mapOf("start_date" to "2023-11-11T00:00:00", "end_date" to "2023-12-12"),
                        "offset" to 11,
                        "limit" to 10,
                        "test_mode" to false
                    )
                )
            }
            assertEquals(HttpStatusCode.OK, response2.status)
            assertEquals(expectedResponse2, response2.body<GetObservationsResponse>())
        }
    }

    @Test
    fun `get observations - 404 patient not found`() {
        runBlocking {
            val response = httpClient.post("$serverUrl/studies/$studyId/sites/$siteId/subject/anotherSubject/observations") {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${authentication.accessToken}")
                }
                setBody(
                    mapOf(
                        "observation_name" to listOf("10f8c49a-635b-4928-aee6-f6e47c2e7c50"),
                        "date_range" to mapOf("start_date" to "2023-11-11T00:00:00", "end_date" to "2023-12-12"),
                        "offset" to 1,
                        "limit" to 100,
                        "test_mode" to false
                    )
                )
            }
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("{\"message\":\"Subject ID not found: anotherSubject\"}", response.bodyAsText())
        }
    }

    @Test
    fun `get observations - 400 invalid date range`() {
        runBlocking {
            val response = httpClient.post("$serverUrl/studies/$studyId/sites/$siteId/subject/$subjectId/observations") {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${authentication.accessToken}")
                }
                setBody(
                    mapOf(
                        "observation_name" to listOf("10f8c49a-635b-4928-aee6-f6e47c2e7c50"),
                        "date_range" to mapOf("start_date" to "2023-11-1", "end_date" to "2023-12-"),
                        "offset" to 1,
                        "limit" to 100,
                        "test_mode" to false
                    )
                )
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("{\"message\":\"Invalid start_date: 2023-11-1\"}", response.bodyAsText())
        }
    }

    @Test
    fun `get observations - 400 invalid offset`() {
        runBlocking {
            val response = httpClient.post("$serverUrl/studies/$studyId/sites/$siteId/subject/$subjectId/observations") {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${authentication.accessToken}")
                }
                setBody(
                    mapOf(
                        "observation_name" to listOf("10f8c49a-635b-4928-aee6-f6e47c2e7c50"),
                        "date_range" to mapOf("start_date" to "2023-11-11", "end_date" to "2023-12-12"),
                        "offset" to 0,
                        "limit" to 100,
                        "test_mode" to false
                    )
                )
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("{\"message\":\"offset must be at least 1.\"}", response.bodyAsText())
        }
    }
}
