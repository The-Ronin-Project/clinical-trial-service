package com.projectronin.clinical.trial.server.clinicalone.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClinicalOneCredentialsHeaderTest {
    @Test
    fun `ensure toString is overwritten`() {
        val creds =
            ClinicalOneCredentialsHeader(
                clientId = "clientId",
                clientSecret = "clientSecret",
            )

        assertEquals(ClinicalOneCredentialsHeader::class.simpleName, creds.toString())
    }
}
