package com.projectronin.clinical.trial.server.clinicalone

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.projectronin.clinical.trial.server.clinicalone.auth.ClinicalOneAuthenticationBroker
import com.projectronin.clinical.trial.server.clinicalone.model.ClinicalOneAddSubjectPayload
import com.projectronin.clinical.trial.server.clinicalone.model.ClinicalOneAddSubjectResponse
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
import org.junit.jupiter.api.Test

class ClinicalOneClientTest {
    private val subjectUrl = "http://localhost:9999/"

    @Test
    fun `retrieve id test`() {
        val siteId = "daffeda1f0084b358a355d4bdc7ae98b"
        val studyId = "dca7919d25b64416bbf58631aff66882"
        val subjectId = "47e71e98170d492fb54c2f93d8084860"
        val responseBody = ClinicalOneAddSubjectResponse(
            status = "success",
            result = ClinicalOneAddSubjectResponse.ClinicalOneAddSubjectResponseResult(
                state = "new",
                studyId = studyId,
                siteId = siteId,
                id = subjectId
            )
        )
        val response = JacksonManager.objectMapper.writeValueAsString(responseBody)
        val body = ClinicalOneAddSubjectPayload(
            ClinicalOneAddSubjectPayload.ClinicalOneAddSubjectInnerPayload(
                siteId = siteId,
                studyId = studyId
            )
        )
        val client = createClient(
            expectedUrl = "$subjectUrl/studies/$studyId/test/subjects",
            expectedBody = JacksonManager.objectMapper.writeValueAsString(body),
            responseBody = response
        )

        val actual = client.getSubjectId(siteId, studyId)
        assertEquals(subjectId, actual)
    }

    private fun createClient(
        expectedBody: String = "",
        expectedUrl: String,
        baseUrl: String = subjectUrl,
        responseStatus: HttpStatusCode = HttpStatusCode.OK,
        responseBody: String = ""
    ): ClinicalOneClient {
        val authenticationBroker = mockk<ClinicalOneAuthenticationBroker> {
            every { getAuthentication() } returns mockk {
                every { tokenType } returns "Bearer"
                every { accessToken } returns "Auth-String"
            }
        }

        val mockEngine = MockEngine { request ->
            assertEquals(expectedUrl, request.url.toString())
            if (expectedBody != "") {
                assertEquals(expectedBody, String(request.body.toByteArray()))
            }
            assertEquals("Bearer Auth-String", request.headers["Authorization"])
            respond(
                content = responseBody,
                status = responseStatus,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                jackson {
                    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                }
            }
        }

        return ClinicalOneClient(httpClient, baseUrl, authenticationBroker)
    }
}
