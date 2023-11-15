package com.projectronin.clinical.trial.client

import com.projectronin.clinical.trial.client.auth.ClinicalTrialAuthenticationService
import com.projectronin.clinical.trial.models.Subject
import com.projectronin.interop.common.http.exceptions.ClientFailureException
import com.projectronin.interop.common.http.request
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ClinicalTrialClient(
    @Value("\${clinicaltrial.url}")
    private val hostUrl: String,
    private val client: HttpClient,
    private val authenticationService: ClinicalTrialAuthenticationService
) {
    private val serverName = "Clinical Trial Service"

    suspend fun getSubjects(activeIdsOnly: Boolean = false): List<Subject> {
        return runCatching<List<Subject>> {
            val subjectUrl = "$hostUrl/subjects"
            val authentication = authenticationService.getAuthentication()

            val response: HttpResponse = client.request(serverName, subjectUrl) { url ->
                get(url) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer ${authentication.accessToken}")
                    }
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    url { parameters.append("activeIdsOnly", activeIdsOnly.toString()) }
                }
            }

            response.body()
        }.fold(
            onSuccess = { it },
            onFailure = {
                if (it is ClientFailureException && it.status == HttpStatusCode.NotFound) {
                    emptyList()
                } else {
                    throw it
                }
            }
        )
    }

    suspend fun createSubject(subject: Subject): Subject {
        return runCatching<Subject> {
            val subjectUrl = "$hostUrl/subjects"
            val authentication = authenticationService.getAuthentication()

            val response: HttpResponse = client.request(serverName, subjectUrl) { url ->
                post(url) {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer ${authentication.accessToken}")
                    }
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    setBody(subject)
                }
            }
            response.body()
        }.fold(
            onSuccess = { it },
            onFailure = {
                throw it
            }
        )
    }
}
