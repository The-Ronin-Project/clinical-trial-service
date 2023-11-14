package com.projectronin.clinical.trial.server.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ConvertersTest {
    private val roninFhirId = "tenant-fhir-id-of-object"
    private val badRoninFhirId = "tenantfhiridofobject"
    private val expectedTenant = "tenant"

    @Test
    fun `get tenant id from ronin fhir id`() {
        val tenantId = roninFhirId.tenantIdFromRoninFhirId()
        assertEquals(expectedTenant, tenantId)
    }

    @Test
    fun `get tenant id from ronin fhir id fails`() {
        val exception = assertThrows<IllegalStateException> {
            badRoninFhirId.tenantIdFromRoninFhirId()
        }

        assertEquals("Ronin FHIR Id is not of form \"tenant-fhirid\".", exception.message)
    }
}
