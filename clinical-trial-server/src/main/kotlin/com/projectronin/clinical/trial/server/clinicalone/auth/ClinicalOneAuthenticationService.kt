package com.projectronin.clinical.trial.server.clinicalone.auth

import com.projectronin.interop.common.auth.Authentication
import com.projectronin.interop.common.http.request
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
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
    private val clinicalOneCredentialsBody: ClinicalOneCredentialsBody
) {
    private val logger = KotlinLogging.logger { }

    fun getAuthentication(): Authentication {
        return runBlocking {
            val httpResponse: HttpResponse = httpClient.request("ClinicalOne", clinicalOneAuthUrl) { url ->
                post(url) {
                    headers {
                        append(
                            HttpHeaders.Authorization,
                            "Basic ${clinicalOneCredentialsHeader.clientId}:${clinicalOneCredentialsHeader.clientSecret}"
                        )
                    }
                    basicAuth(clinicalOneCredentialsHeader.clientId, clinicalOneCredentialsHeader.clientSecret)
                    contentType(ContentType.Application.FormUrlEncoded)
                    setBody(clinicalOneCredentialsBody)
                }
            }
            httpResponse.body<Authentication>()
        }
    }
}
