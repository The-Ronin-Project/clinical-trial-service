package com.projectronin.clinical.trial.server.clinicalone.auth

import com.projectronin.interop.common.auth.Authentication
import com.projectronin.interop.common.http.request
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.basicAuth
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ClinicalOneAuthenticationService(
    private val httpClient: HttpClient,
    @Value("\${clinicalone.auth.url}")
    private val clinicalOneAuthUrl: String,
    private val clinicalOneCredentialsHeader: ClinicalOneCredentialsHeader,
    private val clinicalOneCredentialsFormParameters: ClinicalOneCredentialsFormParameters,
) {
    private val logger = KotlinLogging.logger { }

    fun getAuthentication(): Authentication {
        return runBlocking {
            logger.debug { "Retrieving authorization from $clinicalOneAuthUrl" }
            val httpResponse: HttpResponse =
                httpClient.request("ClinicalOne", clinicalOneAuthUrl) { url ->
                    post(url) {
                        basicAuth(clinicalOneCredentialsHeader.clientId, clinicalOneCredentialsHeader.clientSecret)
                        setBody(
                            FormDataContent(
                                Parameters.build {
                                    append("grant_type", clinicalOneCredentialsFormParameters.grantType)
                                    append("scope", clinicalOneCredentialsFormParameters.scope)
                                },
                            ),
                        )
                        accept(ContentType.Application.Json)
                    }
                }
            logger.debug("Response body: ${httpResponse.bodyAsText()}")
            httpResponse.body<ClinicalOneAuthentication>()
        }
    }
}
