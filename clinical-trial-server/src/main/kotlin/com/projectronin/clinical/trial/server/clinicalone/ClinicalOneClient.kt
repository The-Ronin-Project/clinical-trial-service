package com.projectronin.clinical.trial.server.clinicalone

import com.projectronin.clinical.trial.server.clinicalone.auth.ClinicalOneAuthenticationBroker
import io.ktor.client.HttpClient
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ClinicalOneClient(
    private val httpClient: HttpClient,
    private val authenticationBroker: ClinicalOneAuthenticationBroker
) {
    private val logger = KotlinLogging.logger { }

    fun getSubjectId(siteId: String, studyId: String): String {
        logger.debug { "Retrieving subject id from ClinicalOne based on site: $siteId and study: $studyId" }
        return UUID.randomUUID().toString()
    }
}
