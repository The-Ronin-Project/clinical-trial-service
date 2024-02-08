package com.projectronin.clinical.trial.server.transform

import com.projectronin.clinical.trial.server.data.SubjectDAO
import com.projectronin.interop.datalake.oci.client.OCIClient
import com.projectronin.interop.fhir.ronin.generators.resource.rcdmMedicationRequest
import com.projectronin.interop.fhir.ronin.generators.util.rcdmReference
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RCDMMedicationRequestToCTDMMedicationRequestTest {
    private lateinit var subjectDAO: SubjectDAO
    private val rcdmHelper =
        mockk<BaseRCDMToCTDMHelper> {
        }

    private val ociClient =
        mockk<OCIClient> {
        }
    private val dataDictionaryService = DataDictionaryService(ociClient, "7")

    @BeforeEach
    fun setup() {
        subjectDAO =
            mockk {
            }
    }

    @Test
    fun `Invalid Medication Request returns null`() {
        val rcdmMedicationRequest =
            rcdmMedicationRequest("test") {
                subject of rcdmReference("Location", "456")
            }
        val medicationRequest =
            RCDMMedicationRequestToCTDMMedicationRequest(
                subjectDAO,
                dataDictionaryService,
                rcdmHelper,
            ).rcdmMedicationRequestToCTDMMedicationRequest("test-fhirId", rcdmMedicationRequest)
        Assertions.assertNull(medicationRequest)
    }
}
