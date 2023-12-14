package com.projectronin.clinical.trial.server.clinicalone.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class ClinicalOneCredentialsFormParameters(
    @Value("\${clinicalone.client.scope}")
    val scope: String,
) {
    val grantType: String = "client_credentials"
}
