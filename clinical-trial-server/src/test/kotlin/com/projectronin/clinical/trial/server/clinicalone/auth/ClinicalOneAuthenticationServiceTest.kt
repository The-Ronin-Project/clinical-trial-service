package com.projectronin.clinical.trial.server.clinicalone.auth

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.projectronin.interop.common.http.exceptions.ServiceUnavailableException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.jackson
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ClinicalOneAuthenticationServiceTest {
    private val authUrl = "http://localhost:9999/auth/token"

    @Test
    fun `minimal authentication returned`() {
        val expectedBody = """grant_type=client_credentials&scope=scope"""
        val responseContent = """{
            |  "token_type" : "Bearer",
            |  "access_token": "abcd1234"
            |}
        """.trimMargin()

        val httpClient = makeClient(expectedBody, responseContent, HttpStatusCode.OK)
        val service = ClinicalOneAuthenticationService(
            httpClient,
            authUrl,
            ClinicalOneCredentialsHeader("client-id", "client-secret"),
            ClinicalOneCredentialsFormParameters("scope")
        )
        val authentication = service.getAuthentication()
        Assertions.assertEquals("Bearer", authentication.tokenType)
        Assertions.assertEquals("abcd1234", authentication.accessToken)
        Assertions.assertNull(authentication.expiresAt)
        Assertions.assertNull(authentication.scope)
        Assertions.assertNull(authentication.refreshToken)
    }

    @Test
    fun `full authentication returned`() {
        val expectedBody = """grant_type=client_credentials&scope=scope"""
        val responseContent = """{
            |  "token_type" : "Bearer",
            |  "access_token": "abcd1234",
            |  "expires_in": 3600,
            |  "scope": "local",
            |  "refresh_token": "efgh5678"
            |}
        """.trimMargin()

        val httpClient = makeClient(expectedBody, responseContent, HttpStatusCode.OK)
        val service = ClinicalOneAuthenticationService(
            httpClient,
            authUrl,
            ClinicalOneCredentialsHeader("client-id", "client-secret"),
            ClinicalOneCredentialsFormParameters("scope")
        )
        val authentication = service.getAuthentication()
        Assertions.assertEquals("Bearer", authentication.tokenType)
        Assertions.assertEquals("abcd1234", authentication.accessToken)
        Assertions.assertNotNull(authentication.expiresAt)
        Assertions.assertEquals("local", authentication.scope)
        Assertions.assertEquals("efgh5678", authentication.refreshToken)
    }

    @Test
    fun `exception while authenticating`() {
        val expectedBody = """grant_type=client_credentials&scope=scope"""

        val httpClient = makeClient(expectedBody, "", HttpStatusCode.ServiceUnavailable)
        val service = ClinicalOneAuthenticationService(
            httpClient,
            authUrl,
            ClinicalOneCredentialsHeader("client-id", "client-secret"),
            ClinicalOneCredentialsFormParameters("scope")
        )
        assertThrows<ServiceUnavailableException> {
            service.getAuthentication()
        }
    }

    private fun makeClient(expectedBody: String, responseContent: String, status: HttpStatusCode): HttpClient =
        HttpClient(
            MockEngine { request ->
                Assertions.assertEquals(authUrl, request.url.toString())
                Assertions.assertEquals(expectedBody, String(request.body.toByteArray()))
                respond(
                    content = responseContent,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        ) {
            install(ContentNegotiation) {
                jackson {
                    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                }
            }
        }
}
