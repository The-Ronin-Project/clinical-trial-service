package com.projectronin.clinical.trial.server.clinicalone.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClinicalOneCredentialsFormParametersTest {
    val grantType = "client_credentials"
    val scope = "read:resource"

    @Test
    fun `ensure class initializes properly`() {
        val creds =
            ClinicalOneCredentialsFormParameters(
                scope = scope,
            )

        assertEquals(grantType, creds.grantType)
        assertEquals(scope, creds.scope)
    }
}
