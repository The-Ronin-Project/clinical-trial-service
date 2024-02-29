package com.projectronin.clinical.trial.client

import com.projectronin.clinical.trial.client.auth.ClinicalTrialAuthenticationService
import com.projectronin.clinical.trial.models.Subject
import com.projectronin.interop.common.http.exceptions.ClientFailureException
import com.projectronin.interop.common.http.ktor.ContentLengthSupplier
import com.projectronin.interop.common.jackson.JacksonManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ClinicalTrialClientTest {
    private val authenticationToken = "12345678"
    private val authenticationService =
        mockk<ClinicalTrialAuthenticationService> {
            every { getAuthentication() } returns
                mockk {
                    every { accessToken } returns authenticationToken
                }
        }
    private val client: HttpClient =
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                jackson {
                    JacksonManager.setUpMapper(this)
                }
            }
            install(ContentLengthSupplier)
        }

    @Test
    fun `createSubject works`() {
        val subject =
            Subject(
                roninFhirId = "tenant-fhirid",
                siteId = "siteid",
                studyId = "studyId",
            )

        val subjectReturned =
            Subject(
                id = "newid",
                roninFhirId = "tenant-fhirid",
                siteId = "siteid",
                status = "ACTIVE",
                studyId = "studyId",
                number = "subjectNumber",
            )

        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpStatusCode.OK.value)
                .setBody(JacksonManager.objectMapper.writeValueAsString(subjectReturned))
                .setHeader("Content-Type", "application/json"),
        )

        val url = mockWebServer.url("/test")
        val response =
            runBlocking {
                val subjectToReturn =
                    ClinicalTrialClient(url.toString(), client, authenticationService)
                        .createSubject(subject)
                subjectToReturn
            }

        assertEquals(subjectReturned.id, response.id)
        assertEquals(subjectReturned.roninFhirId, response.roninFhirId)
        assertEquals(subjectReturned.siteId, response.siteId)
        assertEquals(subjectReturned.status, response.status)
        assertEquals(subjectReturned.studyId, response.studyId)
        assertEquals(subjectReturned.number, response.number)

        val request = mockWebServer.takeRequest()
        assertEquals(true, request.path?.endsWith("/subjects"))
        assertEquals("Bearer $authenticationToken", request.getHeader("Authorization"))
    }

    @Test
    fun `createSubject fails`() {
        val subject =
            Subject(
                roninFhirId = "tenant-fhirid",
                siteId = "siteid",
                studyId = "studyId",
                number = "subjectNumber",
            )

        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpStatusCode.BadRequest.value)
                .setHeader("Content-Type", "application/json"),
        )

        val url = mockWebServer.url("/test")
        val exception =
            assertThrows<ClientFailureException> {
                runBlocking {
                    ClinicalTrialClient(url.toString(), client, authenticationService)
                        .createSubject(subject)
                }
            }

        assertNotNull(exception.message)
        exception.message?.let { Assertions.assertTrue(it.contains("400")) }

        val request = mockWebServer.takeRequest()
        assertEquals(true, request.path?.endsWith("/subjects"))
        assertEquals("Bearer $authenticationToken", request.getHeader("Authorization"))
    }

    @Test
    fun `createSubjectWithSubjectNumber works`() {
        val subject =
            Subject(
                roninFhirId = "tenant-fhirid",
                siteId = "siteid",
                studyId = "studyId",
                number = "subjectNumber",
            )

        val subjectReturned =
            Subject(
                id = "newid",
                roninFhirId = "tenant-fhirid",
                siteId = "siteid",
                status = "ACTIVE",
                studyId = "studyId",
                number = "subjectNumber",
            )

        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpStatusCode.OK.value)
                .setBody(JacksonManager.objectMapper.writeValueAsString(subjectReturned))
                .setHeader("Content-Type", "application/json"),
        )

        val url = mockWebServer.url("/test")
        val response =
            runBlocking {
                val subjectToReturn =
                    ClinicalTrialClient(url.toString(), client, authenticationService)
                        .createSubjectWithSubjectNumber(subject)
                subjectToReturn
            }

        assertEquals(subjectReturned.id, response.id)
        assertEquals(subjectReturned.roninFhirId, response.roninFhirId)
        assertEquals(subjectReturned.siteId, response.siteId)
        assertEquals(subjectReturned.status, response.status)
        assertEquals(subjectReturned.studyId, response.studyId)
        assertEquals(subjectReturned.number, response.number)

        val request = mockWebServer.takeRequest()
        assertEquals(true, request.path?.endsWith("/subjects"))
        assertEquals("Bearer $authenticationToken", request.getHeader("Authorization"))
    }

    @Test
    fun `createSubjectWithSubjectNumber fails`() {
        val subject =
            Subject(
                roninFhirId = "tenant-fhirid",
                siteId = "siteid",
                studyId = "studyId",
                number = "subjectNumber",
            )

        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpStatusCode.BadRequest.value)
                .setHeader("Content-Type", "application/json"),
        )

        val url = mockWebServer.url("/test")
        val exception =
            assertThrows<ClientFailureException> {
                runBlocking {
                    ClinicalTrialClient(url.toString(), client, authenticationService)
                        .createSubjectWithSubjectNumber(subject)
                }
            }

        assertNotNull(exception.message)
        exception.message?.let { Assertions.assertTrue(it.contains("400")) }

        val request = mockWebServer.takeRequest()
        assertEquals(true, request.path?.endsWith("/subjects"))
        assertEquals("Bearer $authenticationToken", request.getHeader("Authorization"))
    }

    @Test
    fun `getSubjects works with default or false activeIdsOnly`() {
        val subjectReturned =
            Subject(
                id = "newid",
                roninFhirId = "tenant-fhirid",
                siteId = "siteid",
                status = "ACTIVE",
                studyId = "studyId",
                number = "subjectNumber",
            )

        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpStatusCode.OK.value)
                .setBody(JacksonManager.objectMapper.writeValueAsString(listOf(subjectReturned)))
                .setHeader("Content-Type", "application/json"),
        )

        val url = mockWebServer.url("/test")
        val response =
            runBlocking {
                val subjectsToReturn =
                    ClinicalTrialClient(url.toString(), client, authenticationService)
                        .getSubjects()
                subjectsToReturn
            }
        val returnedSubjects = response as List<Subject>
        assertEquals(1, returnedSubjects.size)
        assertEquals(subjectReturned.id, returnedSubjects[0].id)
        assertEquals(subjectReturned.roninFhirId, returnedSubjects[0].roninFhirId)
        assertEquals(subjectReturned.siteId, returnedSubjects[0].siteId)
        assertEquals(subjectReturned.status, returnedSubjects[0].status)
        assertEquals(subjectReturned.studyId, returnedSubjects[0].studyId)
        assertEquals(subjectReturned.number, returnedSubjects[0].number)

        val request = mockWebServer.takeRequest()
        assertEquals(true, request.path?.endsWith("/subjects?activeIdsOnly=false"))
        assertEquals("Bearer $authenticationToken", request.getHeader("Authorization"))
    }

    @Test
    fun `getSubjects works with true activeIdsOnly`() {
        val subjectReturned =
            Subject(
                id = "newid",
                roninFhirId = "tenant-fhirid",
                siteId = "siteid",
                status = "ACTIVE",
                studyId = "studyId",
                number = "subjectNumber",
            )

        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpStatusCode.OK.value)
                .setBody(JacksonManager.objectMapper.writeValueAsString(listOf(subjectReturned)))
                .setHeader("Content-Type", "application/json"),
        )

        val url = mockWebServer.url("/test")
        val response =
            runBlocking {
                val subjectsToReturn =
                    ClinicalTrialClient(url.toString(), client, authenticationService)
                        .getSubjects(true)
                subjectsToReturn
            }
        val returnedSubjects = response as List<Subject>
        assertEquals(1, returnedSubjects.size)
        assertEquals(subjectReturned.id, returnedSubjects[0].id)
        assertEquals(subjectReturned.roninFhirId, returnedSubjects[0].roninFhirId)
        assertEquals(subjectReturned.siteId, returnedSubjects[0].siteId)
        assertEquals(subjectReturned.status, returnedSubjects[0].status)
        assertEquals(subjectReturned.studyId, returnedSubjects[0].studyId)
        assertEquals(subjectReturned.number, returnedSubjects[0].number)

        val request = mockWebServer.takeRequest()
        assertEquals(true, request.path?.endsWith("/subjects?activeIdsOnly=true"))
        assertEquals("Bearer $authenticationToken", request.getHeader("Authorization"))
    }

    @Test
    fun `getSubjects fails with default or false activeIdsOnly`() {
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpStatusCode.BadRequest.value)
                .setHeader("Content-Type", "application/json"),
        )

        val url = mockWebServer.url("/test")
        val exception =
            assertThrows<ClientFailureException> {
                runBlocking {
                    ClinicalTrialClient(url.toString(), client, authenticationService)
                        .getSubjects()
                }
            }

        assertNotNull(exception.message)
        exception.message?.let { Assertions.assertTrue(it.contains("400")) }

        val request = mockWebServer.takeRequest()
        assertEquals(true, request.path?.endsWith("/subjects?activeIdsOnly=false"))
        assertEquals("Bearer $authenticationToken", request.getHeader("Authorization"))
    }

    @Test
    fun `getSubjects fails with true activeIdsOnly`() {
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpStatusCode.BadRequest.value)
                .setHeader("Content-Type", "application/json"),
        )

        val url = mockWebServer.url("/test")
        val exception =
            assertThrows<ClientFailureException> {
                runBlocking {
                    ClinicalTrialClient(url.toString(), client, authenticationService)
                        .getSubjects(true)
                }
            }

        assertNotNull(exception.message)
        exception.message?.let { Assertions.assertTrue(it.contains("400")) }

        val request = mockWebServer.takeRequest()
        assertEquals(true, request.path?.endsWith("/subjects?activeIdsOnly=true"))
        assertEquals("Bearer $authenticationToken", request.getHeader("Authorization"))
    }

    @Test
    fun `getSubjectById works with roninFhirId`() {
        val subjectReturned =
            Subject(
                id = "newid",
                roninFhirId = "tenant-fhirid",
                siteId = "siteid",
                status = "ACTIVE",
                studyId = "studyId",
                number = "subjectNumber",
            )

        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpStatusCode.OK.value)
                .setBody(JacksonManager.objectMapper.writeValueAsString(subjectReturned))
                .setHeader("Content-Type", "application/json"),
        )

        val url = mockWebServer.url("/test")
        val response =
            runBlocking {
                val subjectToReturn =
                    ClinicalTrialClient(url.toString(), client, authenticationService)
                        .getSubjectById(subjectReturned.roninFhirId)
                subjectToReturn
            }
        val returnedSubject = response as Subject
        assertEquals(subjectReturned.id, returnedSubject.id)
        assertEquals(subjectReturned.roninFhirId, returnedSubject.roninFhirId)
        assertEquals(subjectReturned.siteId, returnedSubject.siteId)
        assertEquals(subjectReturned.status, returnedSubject.status)
        assertEquals(subjectReturned.studyId, returnedSubject.studyId)
        assertEquals(subjectReturned.number, returnedSubject.number)

        val request = mockWebServer.takeRequest()
        assertEquals(true, request.path?.endsWith("/subjects/${subjectReturned.roninFhirId}"))
        assertEquals("Bearer $authenticationToken", request.getHeader("Authorization"))
    }
}
