package com.projectronin.clinical.trial.server.transform

import com.projectronin.clinical.trial.server.data.SubjectDAO
import com.projectronin.interop.datalake.oci.client.OCIClient
import com.projectronin.interop.fhir.ronin.generators.resource.condition.rcdmConditionEncounterDiagnosis
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RCDMConditionToCTDMConditionTest {
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
    fun `invalid condition returns null`() {
        val rcdmCondition =
            rcdmConditionEncounterDiagnosis("test") {
                // subject of rcdmReference()
            }
        val condition =
            RCDMConditionToCTDMCondition(
                subjectDAO,
                dataDictionaryService,
                rcdmHelper,
            ).rcdmConditionToCTDMCondition("test-fhirId", rcdmCondition)
        Assertions.assertNull(condition)
    }
}
