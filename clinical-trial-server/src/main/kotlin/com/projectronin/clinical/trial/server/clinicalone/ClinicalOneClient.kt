package com.projectronin.clinical.trial.server.clinicalone

import com.projectronin.clinical.trial.models.Subject
import com.projectronin.clinical.trial.server.clinicalone.auth.ClinicalOneAuthenticationBroker
import com.projectronin.clinical.trial.server.clinicalone.model.ClinicalOneAddSubjectPayload
import com.projectronin.clinical.trial.server.clinicalone.model.ClinicalOneAddSubjectResponse
import com.projectronin.clinical.trial.server.clinicalone.model.ClinicalOneGetStudyNameResponse
import com.projectronin.clinical.trial.server.clinicalone.model.ClinicalOneSearchSubjectResponse
import com.projectronin.clinical.trial.server.clinicalone.model.StudyResult
import com.projectronin.clinical.trial.server.clinicalone.model.SubjectResult
import com.projectronin.interop.common.http.request
import com.projectronin.interop.common.jackson.JacksonManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
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
    @Value("\${clinicalone.datacapture.url}")
    private val clinicalOneDataCaptureUrl: String,
    @Value("\${clinicalone.designer.url}")
    private val clinicalOneDesignerUrl: String,
    private val authenticationBroker: ClinicalOneAuthenticationBroker,
) {
    private val logger = KotlinLogging.logger { }

    fun getSubjectIdAndSubjectNumber(
        siteId: String,
        studyId: String,
    ): SubjectResult {
        logger.info { "Retrieving subject id from ClinicalOne based on site: $siteId and study: $studyId" }

        val authentication = authenticationBroker.getAuthentication()
        val clinicalOneSubjectUrl = "$clinicalOneBaseUrl/$clinicalOneDataCaptureUrl/v2.0/studies/$studyId/test/subjects"
        val requestBody =
            ClinicalOneAddSubjectPayload(
                ClinicalOneAddSubjectPayload.ClinicalOneAddSubjectInnerPayload(
                    siteId = siteId,
                    studyId = studyId,
                ),
            )
        logger.debug { "Auth: ${authentication.tokenType} ${authentication.accessToken}" }
        return runBlocking {
            val response: HttpResponse =
                httpClient.request("ClinicalOne", clinicalOneSubjectUrl) { url ->
                    post(url) {
                        headers {
                            append(
                                HttpHeaders.Authorization,
                                "${authentication.tokenType} ${authentication.accessToken}",
                            )
                        }
                        contentType(ContentType.Application.Json)
                        accept(ContentType.Application.Json)
                        setBody(JacksonManager.objectMapper.writeValueAsString(requestBody))
                    }
                }
            logger.debug { response }
            response.let { res ->
                if (res.status == HttpStatusCode.OK) {
                    val responseBody = res.body<ClinicalOneAddSubjectResponse>()
                    SubjectResult(responseBody.result?.id ?: "", responseBody.result?.subjectNumber ?: "")
                } else {
                    throw Exception("Failed to create subject with Clinical One API. Status Code: ${res.status}. ${res.bodyAsText()}")
                }
            }
        }
    }

    fun validateSubjectNumber(subject: Subject): Subject? {
        logger.info {
            "Retrieving subject id from ClinicalOne based on " +
                "site: $subject.siteId and " +
                "study: ${subject.studyId} and " +
                "subject number: ${subject.number}"
        }

        val authentication = authenticationBroker.getAuthentication()
        val clinicalOneStudiesUrl = "$clinicalOneBaseUrl/$clinicalOneDataCaptureUrl/v7.0/studies/${subject.studyId}"
        val clinicalOneSubjectUrl = "$clinicalOneStudiesUrl/test/subjects/sitestudyversion?exactSearchKeyword=${subject.number}"
        logger.debug { "Auth: ${authentication.tokenType} ${authentication.accessToken}" }
        return runBlocking {
            val response: HttpResponse =
                httpClient.request("ClinicalOne", clinicalOneSubjectUrl) { url ->
                    get(url) {
                        headers {
                            append(
                                HttpHeaders.Authorization,
                                "${authentication.tokenType} ${authentication.accessToken}",
                            )
                        }
                        contentType(ContentType.Application.Json)
                        accept(ContentType.Application.Json)
                    }
                }
            logger.debug { response }
            response.let { res ->
                if (res.status == HttpStatusCode.OK) {
                    val responseBody = res.body<ClinicalOneSearchSubjectResponse>()
                    if (responseBody.result?.isEmpty() == true) {
                        return@runBlocking null
                    } else if (responseBody.result?.size == 1) {
                        Subject(
                            id = responseBody.result[0].id ?: "",
                            roninFhirId = subject.roninFhirId,
                            siteId = responseBody.result[0].siteId ?: "",
                            studyId = responseBody.result[0].studyId ?: "",
                            number = responseBody.result[0].subjectNumber ?: "",
                        )
                    } else {
                        // TODO: We may be able to handle this without erroring but I'd need to see real data to figure out how
                        throw Exception("Found multiple matching subjects with Clinical One API.")
                    }
                } else {
                    throw Exception("Failed to find subject with Clinical One API. Status Code: ${res.status}. ${res.bodyAsText()}")
                }
            }
        }
    }

    fun getStudyName(studyId: String): StudyResult {
        logger.info { "Retrieving study name from ClinicalOne based on studyId: $studyId" }
        val authentication = authenticationBroker.getAuthentication()
        val clinicalOneStudyUrl = "$clinicalOneBaseUrl/$clinicalOneDesignerUrl/v6.0/studies/$studyId/statuses"
        logger.debug { "Auth: ${authentication.tokenType} ${authentication.accessToken}" }
        return runBlocking {
            val response: HttpResponse =
                httpClient.request("ClinicalOne", clinicalOneStudyUrl) { url ->
                    post(url) {
                        headers {
                            append(
                                HttpHeaders.Authorization,
                                "${authentication.tokenType} ${authentication.accessToken}",
                            )
                        }
                        contentType(ContentType.Application.Json)
                        accept(ContentType.Application.Json)
                    }
                }
            logger.debug { response }
            response.let { res ->
                if (res.status == HttpStatusCode.OK) {
                    val responseBody = res.body<ClinicalOneGetStudyNameResponse>()
                    StudyResult(
                        responseBody.result?.last()?.identity ?: "",
                        responseBody.result?.last()?.id ?: "",
                        responseBody.result?.last()?.studyTitle ?: "",
                    )
                } else {
                    throw Exception("Failed to get study name with Clinical One API. Status Code: ${res.status}. ${res.bodyAsText()}")
                }
            }
        }
    }
}
