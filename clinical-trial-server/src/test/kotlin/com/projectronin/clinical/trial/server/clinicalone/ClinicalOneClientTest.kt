package com.projectronin.clinical.trial.server.clinicalone

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.projectronin.clinical.trial.models.Subject
import com.projectronin.clinical.trial.server.clinicalone.auth.ClinicalOneAuthenticationBroker
import com.projectronin.clinical.trial.server.clinicalone.model.ClinicalOneAddSubjectPayload
import com.projectronin.clinical.trial.server.clinicalone.model.ClinicalOneAddSubjectResponse
import com.projectronin.clinical.trial.server.clinicalone.model.ClinicalOneGetStudyNameResponse
import com.projectronin.clinical.trial.server.clinicalone.model.ClinicalOneSearchSubjectResponse
import com.projectronin.clinical.trial.server.clinicalone.model.ClinicalOneSubjectResponseResult
import com.projectronin.interop.common.jackson.JacksonManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.jackson
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ClinicalOneClientTest {
    private val subjectUrl = "http://localhost:9999"
    private val dcUrl = "dc/rest"
    private val designerUrl = "designer/rest"

    @Test
    fun `retrieve id test`() {
        val siteId = "daffeda1f0084b358a355d4bdc7ae98b"
        val studyId = "dca7919d25b64416bbf58631aff66882"
        val subjectId = "47e71e98170d492fb54c2f93d8084860"
        val subjectNumber = "001-001"
        val responseBody =
            ClinicalOneAddSubjectResponse(
                status = "success",
                result =
                    ClinicalOneSubjectResponseResult(
                        state = "new",
                        studyId = studyId,
                        siteId = siteId,
                        id = subjectId,
                        subjectNumber = subjectNumber,
                    ),
            )
        val response = JacksonManager.objectMapper.writeValueAsString(responseBody)
        val body =
            ClinicalOneAddSubjectPayload(
                ClinicalOneAddSubjectPayload.ClinicalOneAddSubjectInnerPayload(
                    siteId = siteId,
                    studyId = studyId,
                ),
            )
        val client =
            createClient(
                expectedUrl = "$subjectUrl/$dcUrl/v2.0/studies/$studyId/test/subjects",
                expectedBody = JacksonManager.objectMapper.writeValueAsString(body),
                responseBody = response,
            )

        val subject = client.getSubjectIdAndSubjectNumber(siteId, studyId)
        assertEquals(subjectId, subject.subjectId)
        assertEquals(subjectNumber, subject.subjectNumber)
    }

    @Test
    fun `retrieve study name test`() {
        val responseBody =
            ClinicalOneGetStudyNameResponse(
                status = "success",
                result =
                    listOf(
                        ClinicalOneGetStudyNameResponse.ClinicalOneGetStudyNameResponseResult(
                            identity = "studyId:studyVersion",
                            id = "studyId",
                            version = "studyVersion",
                            versionStart = "2023-10-19T20:12:44.429Z",
                            versionEnd = "2023-10-30T16:46:41.991Z",
                            studyTitle = "Ronin POC",
                            studyDescription = "Ronin POC",
                        ),
                        ClinicalOneGetStudyNameResponse.ClinicalOneGetStudyNameResponseResult(
                            identity = "studyId:studyVersion",
                            id = "studyId",
                            version = "studyVersion",
                            versionStart = "2024-01-01T20:12:44.429Z",
                            versionEnd = "2099-10-30T16:46:41.991Z",
                            studyTitle = "Ronin POC",
                            studyDescription = "Ronin POC",
                        ),
                    ),
            )
        val response = JacksonManager.objectMapper.writeValueAsString(responseBody)

        val client =
            createClient(
                expectedUrl = "$subjectUrl/$designerUrl/v6.0/studies/studyId/statuses",
                responseBody = response,
            )
        val study = client.getStudyName("studyId")
        assertEquals("studyId:studyVersion", study.studyIdentity)
        assertEquals("Ronin POC", study.studyName)
        assertEquals("studyId", study.studyId)
    }

    @Test
    fun `retrieve study status not OK`() {
        val client =
            createClient(
                expectedUrl = "$subjectUrl/$designerUrl/v6.0/studies/studyId/statuses",
                responseStatus = HttpStatusCode.Forbidden,
            )
        val exception = assertThrows<Exception> { client.getStudyName("studyId") }
        exception.message?.let { assertTrue(it.startsWith("Received 403 Forbidden when calling ClinicalOne")) }
    }

    @Test
    fun `retrieve subject by number test`() {
        val siteId = "daffeda1f0084b358a355d4bdc7ae98b"
        val studyId = "dca7919d25b64416bbf58631aff66882"
        val subjectId = "47e71e98170d492fb54c2f93d8084860"
        val subjectNumber = "001-001"
        val responseBody =
            ClinicalOneSearchSubjectResponse(
                status = "success",
                result =
                    listOf(
                        ClinicalOneSubjectResponseResult(
                            state = "new",
                            studyId = studyId,
                            siteId = siteId,
                            id = subjectId,
                            subjectNumber = subjectNumber,
                        ),
                    ),
            )
        val response = JacksonManager.objectMapper.writeValueAsString(responseBody)

        val client =
            createClient(
                expectedUrl =
                    "$subjectUrl/$dcUrl/v7.0/studies/$studyId" +
                        "/test/subjects/sitestudyversion?exactSearchKeyword=$subjectNumber",
                responseBody = response,
            )

        val res =
            client.validateSubjectNumber(
                subject =
                    Subject(
                        id = subjectId,
                        roninFhirId = "",
                        siteId = siteId,
                        studyId = studyId,
                        number = subjectNumber,
                    ),
            )
        assertEquals(subjectId, res?.id)
        assertEquals(subjectNumber, res?.number)
    }

    private fun createClient(
        expectedBody: String = "",
        expectedUrl: String,
        baseUrl: String = subjectUrl,
        responseStatus: HttpStatusCode = HttpStatusCode.OK,
        responseBody: String = "",
    ): ClinicalOneClient {
        val authenticationBroker =
            mockk<ClinicalOneAuthenticationBroker> {
                every { getAuthentication() } returns
                    mockk {
                        every { tokenType } returns "Bearer"
                        every { accessToken } returns "Auth-String"
                    }
            }

        val mockEngine =
            MockEngine { request ->
                assertEquals(expectedUrl, request.url.toString())
                if (expectedBody != "") {
                    assertEquals(expectedBody, String(request.body.toByteArray()))
                }
                assertEquals("Bearer Auth-String", request.headers["Authorization"])
                respond(
                    content = responseBody,
                    status = responseStatus,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }

        val httpClient =
            HttpClient(mockEngine) {
                install(ContentNegotiation) {
                    jackson {
                        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                    }
                }
            }

        return ClinicalOneClient(httpClient, baseUrl, dcUrl, designerUrl, authenticationBroker)
    }
}
