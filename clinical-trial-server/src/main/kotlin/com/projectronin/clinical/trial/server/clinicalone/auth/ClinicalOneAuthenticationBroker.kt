package com.projectronin.clinical.trial.server.clinicalone.auth

import com.projectronin.interop.common.auth.Authentication
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class ClinicalOneAuthenticationBroker(private val authenticationService: ClinicalOneAuthenticationService) {
    private val logger = KotlinLogging.logger { }
    private val expirationBuffer: Long = 60 // seconds
    private var cachedAuthentication: Authentication? = null

    fun getAuthentication(): Authentication {
        val isCacheValid =
            cachedAuthentication?.expiresAt?.isAfter(Instant.now().plusSeconds(expirationBuffer)) ?: false
        if (isCacheValid) {
            logger.debug { "Returning cached authentication for ClinicalOne" }
            return cachedAuthentication!!
        }

        logger.debug { "Requesting fresh authentication for ClinicalOne" }
        val authentication = authenticationService.getAuthentication()
        logger.debug { "Retrieved authentication from ClinicalOne, has expiration: (${authentication.expiresAt})" }
        authentication.expiresAt?.let {
            logger.debug { "Caching expiration" }
            cachedAuthentication = authentication
        }
        return authentication
    }
}
