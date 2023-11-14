package com.projectronin.clinical.trial.server.clinicalone.auth

import com.fasterxml.jackson.annotation.JsonProperty
import com.projectronin.interop.common.auth.Authentication
import java.time.Instant

class ClinicalOneAuthentication(
    override val tokenType: String,
    @JsonProperty("access_token")
    override val accessToken: String,
    @JsonProperty("token type")
    override val refreshToken: String? = null,
    @JsonProperty("expires in")
    private val expiresIn: Long?,
    override val scope: String? = null
) : Authentication {
    override val expiresAt: Instant? = expiresIn?.let { Instant.now().plusSeconds(expiresIn) }

    // Override toString() to prevent accidentally leaking the accessToken
    override fun toString(): String = this::class.simpleName!!
}
