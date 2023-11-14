package com.projectronin.clinical.trial.server.clinicalone.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
data class ClinicalOneCredentialsHeader(
    @Value("\${clinicalone.client.id}")
    val clientId: String,
    @Value("\${clinicalone.client.secret")
    val clientSecret: String
) {
    // Override toString() to prevent accidentally leaking the clientSecret
    override fun toString(): String = this::class.simpleName!!
}
