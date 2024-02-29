package com.projectronin.clinical.trial.server

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClinicalTrialServerConfigTest {
    private val config = ClinicalTrialServerConfig()

    @Test
    fun `http client builder works`() {
        val httpClient = config.getHttpClient()
        assertEquals(httpClient.connectTimeoutMillis, 15000)
        assertEquals(httpClient.readTimeoutMillis, 15000)
    }
}
