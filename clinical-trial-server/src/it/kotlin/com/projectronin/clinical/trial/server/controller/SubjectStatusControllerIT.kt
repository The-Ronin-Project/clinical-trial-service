package com.projectronin.clinical.trial.server.controller

import com.projectronin.clinical.trial.server.BaseIT
import com.projectronin.clinical.trial.server.data.binding.SiteDOs
import com.projectronin.clinical.trial.server.data.binding.StudyDOs
import com.projectronin.clinical.trial.server.data.binding.StudySiteDOs
import com.projectronin.clinical.trial.server.data.binding.SubjectDOs
import com.projectronin.clinical.trial.server.data.binding.SubjectStatusDOs
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
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

class SubjectStatusControllerIT : BaseIT() {
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
    fun `get subject status (no auth) - 401`() {
        val response = runBlocking {
            httpClient
                .get(
                    "$serverUrl/studies/$studyId/sites/$siteId/subject/$subjectId/status"
                )
        }
        assertEquals(401, response.status.value)
    }

    @Test
    fun `get subject status (no data) - 404`() {
        val authentication = getAuth()
        val response = runBlocking {
            httpClient
                .get("$serverUrl/studies/$studyId/sites/$siteId/subject/$subjectId/status") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer ${authentication.accessToken}")
                    }
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                }
        }
        assertEquals(404, response.status.value)
    }

    @Test
    fun `get subject status - 200`() {
        seedDB()
        val authentication = getAuth()
        val response = runBlocking {
            httpClient
                .get("$serverUrl/studies/$studyId/sites/$siteId/subject/$subjectId/status") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer ${authentication.accessToken}")
                    }
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                }
        }
        assertEquals(200, response.status.value)
    }

    @Test
    fun `update subject status - 200`() {
        seedDB()
        val authentication = getAuth()
        runBlocking {
            val response = httpClient
                .post("$serverUrl/studies/$studyId/sites/$siteId/subject/$subjectId/status") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer ${authentication.accessToken}")
                    }
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    setBody(UpdateStatusRequest("WITHDRAWN"))
                }

            assertEquals(200, response.status.value)
            assertEquals("Enrollment status updated successfully.", response.body<StatusResponse>().message)
        }
    }
}
