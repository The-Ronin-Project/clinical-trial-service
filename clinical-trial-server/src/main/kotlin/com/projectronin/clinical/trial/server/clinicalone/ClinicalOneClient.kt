package com.projectronin.clinical.trial.server.clinicalone

import com.projectronin.clinical.trial.server.clinicalone.auth.ClinicalOneAuthenticationBroker
import com.projectronin.clinical.trial.server.clinicalone.model.ClinicalOneAddSubjectPayload
import com.projectronin.clinical.trial.server.clinicalone.model.ClinicalOneAddSubjectResponse
import com.projectronin.interop.common.http.request
import com.projectronin.interop.common.jackson.JacksonManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ClinicalOneClient(
    private val httpClient: HttpClient,
    @Value("\${clinicalone.base.url}")
    private val clinicalOneBaseUrl: String,
    private val authenticationBroker: ClinicalOneAuthenticationBroker
) {
    private val logger = KotlinLogging.logger { }

    fun getSubjectId(siteId: String, studyId: String): String {
        logger.debug { "Retrieving subject id from ClinicalOne based on site: $siteId and study: $studyId" }

        val authentication = authenticationBroker.getAuthentication()
        val clinicalOneSubjectUrl = "$clinicalOneBaseUrl/studies/$studyId/test/subjects"
        val requestBody = ClinicalOneAddSubjectPayload(
            ClinicalOneAddSubjectPayload.ClinicalOneAddSubjectInnerPayload(
                siteId = siteId,
                studyId = studyId
            )
        )

        return runBlocking {
            val response: HttpResponse =
                httpClient.request("ClinicalOne", clinicalOneSubjectUrl) { url ->
                    post(url) {
                        headers {
                            append(HttpHeaders.Authorization, "${authentication.tokenType} ${authentication.accessToken}")
                        }
                        contentType(ContentType.Application.Json)
                        accept(ContentType.Application.Json)
                        setBody(JacksonManager.objectMapper.writeValueAsString(requestBody))
                    }
                }

            response.let { res ->
                if (res.status == HttpStatusCode.OK) {
                    val responseBody = res.body<ClinicalOneAddSubjectResponse>()
                    responseBody.result?.id ?: ""
                } else {
                    logger.debug { "Failed to create subject with Clinical One API. Status Code: ${res.status}. " }
                    ""
                }
            }
        }
    }
}
