package com.projectronin.clinical.trial.server.clinicalone.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClinicalOneAuthenticationTest {
    @Test
    fun `ensure toString is overwritten`() {
        val authentication =
            ClinicalOneAuthentication(
                tokenType = "type",
                accessToken = "token",
                expiresIn = 1L,
            )

        assertEquals(ClinicalOneAuthentication::class.simpleName, authentication.toString())
    }
}
