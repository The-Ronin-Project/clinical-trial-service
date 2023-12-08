package com.projectronin.clinical.trial.server.transform

import com.projectronin.clinical.trial.server.data.SubjectDAO
import com.projectronin.interop.datalake.oci.client.OCIClient
import com.projectronin.interop.fhir.generators.datatypes.DynamicValues
import com.projectronin.interop.fhir.generators.datatypes.annotation
import com.projectronin.interop.fhir.generators.datatypes.codeableConcept
import com.projectronin.interop.fhir.generators.datatypes.coding
import com.projectronin.interop.fhir.generators.datatypes.meta
import com.projectronin.interop.fhir.generators.primitives.instant
import com.projectronin.interop.fhir.generators.primitives.markdown
import com.projectronin.interop.fhir.generators.primitives.of
import com.projectronin.interop.fhir.generators.resources.observationComponent
import com.projectronin.interop.fhir.generators.resources.observationReferenceRange
import com.projectronin.interop.fhir.r4.CodeSystem
import com.projectronin.interop.fhir.r4.datatype.primitive.Canonical
import com.projectronin.interop.fhir.r4.datatype.primitive.Code
import com.projectronin.interop.fhir.r4.validate.resource.R4ObservationValidator
import com.projectronin.interop.fhir.ronin.generators.resource.observation.rcdmObservation
import com.projectronin.interop.fhir.ronin.generators.resource.observation.rcdmObservationBloodPressure
import com.projectronin.interop.fhir.ronin.generators.resource.observation.rcdmObservationBodyHeight
import com.projectronin.interop.fhir.ronin.generators.resource.observation.rcdmObservationBodyMassIndex
import com.projectronin.interop.fhir.ronin.generators.resource.observation.rcdmObservationBodySurfaceArea
import com.projectronin.interop.fhir.ronin.generators.resource.observation.rcdmObservationBodyTemperature
import com.projectronin.interop.fhir.ronin.generators.resource.observation.rcdmObservationBodyWeight
import com.projectronin.interop.fhir.ronin.generators.resource.observation.rcdmObservationHeartRate
import com.projectronin.interop.fhir.ronin.generators.resource.observation.rcdmObservationLaboratoryResult
import com.projectronin.interop.fhir.ronin.generators.resource.observation.rcdmObservationPulseOximetry
import com.projectronin.interop.fhir.ronin.generators.resource.observation.rcdmObservationRespiratoryRate
import com.projectronin.interop.fhir.ronin.generators.util.rcdmReference
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RCDMObservationToCTDMObservationTest {
    private lateinit var subjectDAO: SubjectDAO
    private val rcdmHelper = mockk<BaseRCDMToCTDMHelper> {
        every { setProfileMeta("Body Temperature Noninvasive") } returns meta {
            profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
            tag of listOf(
                coding {
                    system of "798f075e-a48d-49d2-8ace-66aff6f55478"
                    display of "Body Temperature Noninvasive"
                    version of "1"
                }
            )
        }
        every { setProfileMeta("Heart Rate Noninvasive") } returns meta {
            profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
            tag of listOf(
                coding {
                    system of "809f547e-cb7e-438f-84af-8852d586c718"
                    display of "Heart Rate Noninvasive"
                    version of "1"
                }
            )
        }
        every { setProfileMeta("Blood Pressure Panel") } returns meta {
            profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
            tag of listOf(
                coding {
                    system of "64baa785-ba8a-448f-8714-93d57fd64db5"
                    display of "Blood Pressure Panel"
                    version of "1"
                }
            )
        }
        every { setProfileMeta("Respiratory Rate Without Provocation") } returns meta {
            profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
            tag of listOf(
                coding {
                    system of "46b10973-c8e0-4086-9c71-ec464ef363e8"
                    display of "Respiratory Rate Without Provocation"
                    version of "1"
                }
            )
        }
        every { setProfileMeta("Oxygen Saturation (SpO2) by Pulse Oximetry") } returns meta {
            profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
            tag of listOf(
                coding {
                    system of "ad0f2555-e5ef-484d-9b55-1573d86863c1"
                    display of "Oxygen Saturation (SpO2) by Pulse Oximetry"
                    version of "1"
                }
            )
        }
        every { setProfileMeta("Body Height") } returns meta {
            profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
            tag of listOf(
                coding {
                    system of "370b5c79-71f1-4f00-ab3d-cd7d430f813b"
                    display of "Body Height"
                    version of "1"
                }
            )
        }
        every { setProfileMeta("Body Weight") } returns meta {
            profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
            tag of listOf(
                coding {
                    system of "1502ca1d-b8f1-4b53-ba64-2e12a51ec896"
                    display of "Body Weight"
                    version of "1"
                }
            )
        }
        every { setProfileMeta("Body Mass Index Ratio (BMI)") } returns meta {
            profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
            tag of listOf(
                coding {
                    system of "0fd5b59f-a7db-4971-9af9-b597a5ffbfac"
                    display of "Body Mass Index Ratio (BMI)"
                    version of "1"
                }
            )
        }
        every { setProfileMeta("Body Surface Area (BSA)") } returns meta {
            profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
            tag of listOf(
                coding {
                    system of "45354da4-30b4-433e-b5aa-d99a903d5dc9"
                    display of "Body Surface Area (BSA)"
                    version of "1"
                }
            )
        }
        every { setProfileMeta("Hemoglobin (HGB) in Blood") } returns meta {
            profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
            tag of listOf(
                coding {
                    system of "c874065c-2ea4-464d-a0f2-fc94961ac5b4"
                    display of "Hemoglobin (HGB) in Blood"
                    version of "1"
                }
            )
        }
    }
    private val registryCSV = File(DataDictionaryServiceTest::class.java.getResource("/transform/registryExample2.csv")!!.file).readText()
    private val valueSetTemp =
        File(DataDictionaryServiceTest::class.java.getResource("/transform/valueSetTemp.json")!!.file).readText()
    private val valueSetHR =
        File(DataDictionaryServiceTest::class.java.getResource("/transform/valueSetHR.json")!!.file).readText()
    private val valueSetBP =
        File(DataDictionaryServiceTest::class.java.getResource("/transform/valueSetBP.json")!!.file).readText()
    private val valueSetRR =
        File(DataDictionaryServiceTest::class.java.getResource("/transform/valueSetRR.json")!!.file).readText()
    private val valueSetPulseOx =
        File(DataDictionaryServiceTest::class.java.getResource("/transform/valueSetPulseOx.json")!!.file).readText()
    private val valueSetHeight =
        File(DataDictionaryServiceTest::class.java.getResource("/transform/valueSetHeight.json")!!.file).readText()
    private val valueSetWeight =
        File(DataDictionaryServiceTest::class.java.getResource("/transform/valueSetWeight.json")!!.file).readText()
    private val valueSetBMI =
        File(DataDictionaryServiceTest::class.java.getResource("/transform/valueSetBMI.json")!!.file).readText()
    private val valueSetBSA =
        File(DataDictionaryServiceTest::class.java.getResource("/transform/valueSetBSA.json")!!.file).readText()
    private val valueSetHG =
        File(DataDictionaryServiceTest::class.java.getResource("/transform/valueSetHG.json")!!.file).readText()
    private val ociClient = mockk<OCIClient> {
        every { getObjectFromINFX("Registries/v1/data dictionary/prod/38efb390-497f-4b49-9619-a45d33048a3a/6.csv") } returns registryCSV
        every { getObjectFromINFX("ValueSets/v2/published/798f075e-a48d-49d2-8ace-66aff6f55478/5.json") } returns valueSetTemp
        every { getObjectFromINFX("ValueSets/v2/published/809f547e-cb7e-438f-84af-8852d586c718/5.json") } returns valueSetHR
        every { getObjectFromINFX("ValueSets/v2/published/64baa785-ba8a-448f-8714-93d57fd64db5/3.json") } returns valueSetBP
        every { getObjectFromINFX("ValueSets/v2/published/46b10973-c8e0-4086-9c71-ec464ef363e8/3.json") } returns valueSetRR
        every { getObjectFromINFX("ValueSets/v2/published/ad0f2555-e5ef-484d-9b55-1573d86863c1/4.json") } returns valueSetPulseOx
        every { getObjectFromINFX("ValueSets/v2/published/370b5c79-71f1-4f00-ab3d-cd7d430f813b/3.json") } returns valueSetHeight
        every { getObjectFromINFX("ValueSets/v2/published/1502ca1d-b8f1-4b53-ba64-2e12a51ec896/3.json") } returns valueSetWeight
        every { getObjectFromINFX("ValueSets/v2/published/0fd5b59f-a7db-4971-9af9-b597a5ffbfac/3.json") } returns valueSetBMI
        every { getObjectFromINFX("ValueSets/v2/published/45354da4-30b4-433e-b5aa-d99a903d5dc9/4.json") } returns valueSetBSA
        every { getObjectFromINFX("ValueSets/v2/published/c874065c-2ea4-464d-a0f2-fc94961ac5b4/5.json") } returns valueSetHG
    }
    private val dataDictionaryService = DataDictionaryService(ociClient)

    @BeforeEach
    fun setup() {
        subjectDAO = mockk {
            every { getSubjectByFhirId("test-fhirId") } returns "subjectId"
            every { getSubjectByFhirId("test-notfhirId") } returns null
        }
        dataDictionaryService.load()
    }

    @Test
    fun `can transform body temperature observation`() {
        val rcdmObservation = rcdmObservationBodyTemperature("test") {
            subject of rcdmReference("Patient", "456")
        }

        val observation = RCDMObservationToCTDMObservation(subjectDAO, dataDictionaryService, rcdmHelper).rcdmObservationToCTDMObservation("test-fhirId", rcdmObservation)
        R4ObservationValidator.validate(observation!!).alertIfErrors()
        assertEquals(observation.meta!!.profile[0].value, "https://projectronin.io/fhir/StructureDefinition/CTDM-Observation")
    }

    @Test
    fun `can transform heart rate observation`() {
        val rcdmObservation = rcdmObservationHeartRate("test") {
            subject of rcdmReference("Patient", "456")
        }
        val observation = RCDMObservationToCTDMObservation(subjectDAO, dataDictionaryService, rcdmHelper).rcdmObservationToCTDMObservation("test-fhirId", rcdmObservation)
        R4ObservationValidator.validate(observation!!).alertIfErrors()
        assertEquals(observation.meta!!.profile[0].value, "https://projectronin.io/fhir/StructureDefinition/CTDM-Observation")
    }

    @Test
    fun `can transform blood pressure observation`() {
        val rcdmObservation = rcdmObservationBloodPressure("test") {
            subject of rcdmReference("Patient", "456")
        }

        val observation = RCDMObservationToCTDMObservation(subjectDAO, dataDictionaryService, rcdmHelper).rcdmObservationToCTDMObservation("test-fhirId", rcdmObservation)
        R4ObservationValidator.validate(observation!!).alertIfErrors()
        assertEquals(observation.meta!!.profile[0].value, "https://projectronin.io/fhir/StructureDefinition/CTDM-Observation")
    }

    @Test
    fun `can transform respiratory rate observation`() {
        val rcdmObservation = rcdmObservationRespiratoryRate("test") {
            subject of rcdmReference("Patient", "456")
        }

        val observation = RCDMObservationToCTDMObservation(subjectDAO, dataDictionaryService, rcdmHelper).rcdmObservationToCTDMObservation("test-fhirId", rcdmObservation)
        R4ObservationValidator.validate(observation!!).alertIfErrors()
        assertEquals(observation.meta!!.profile[0].value, "https://projectronin.io/fhir/StructureDefinition/CTDM-Observation")
    }

    @Test
    fun `can transform O2 observation`() {
        val rcdmObservation = rcdmObservationPulseOximetry("test") {
            subject of rcdmReference("Patient", "456")
        }

        val observation = RCDMObservationToCTDMObservation(subjectDAO, dataDictionaryService, rcdmHelper).rcdmObservationToCTDMObservation("test-fhirId", rcdmObservation)
        R4ObservationValidator.validate(observation!!).alertIfErrors()
        assertEquals(observation.meta!!.profile[0].value, "https://projectronin.io/fhir/StructureDefinition/CTDM-Observation")
    }

    @Test
    fun `can transform height observation`() {
        val rcdmObservation = rcdmObservationBodyHeight("test") {
            subject of rcdmReference("Patient", "456")
        }

        val observation = RCDMObservationToCTDMObservation(subjectDAO, dataDictionaryService, rcdmHelper).rcdmObservationToCTDMObservation("test-fhirId", rcdmObservation)
        R4ObservationValidator.validate(observation!!).alertIfErrors()
        assertEquals(observation.meta!!.profile[0].value, "https://projectronin.io/fhir/StructureDefinition/CTDM-Observation")
    }

    @Test
    fun `can transform weight observation`() {
        val rcdmObservation = rcdmObservationBodyWeight("test") {
            subject of rcdmReference("Patient", "456")
        }
        val observation = RCDMObservationToCTDMObservation(subjectDAO, dataDictionaryService, rcdmHelper).rcdmObservationToCTDMObservation("test-fhirId", rcdmObservation)
        R4ObservationValidator.validate(observation!!).alertIfErrors()
        assertEquals(observation.meta!!.profile[0].value, "https://projectronin.io/fhir/StructureDefinition/CTDM-Observation")
    }

    @Test
    fun `can transform BMI observation`() {
        val rcdmObservation = rcdmObservationBodyMassIndex("test") {
            subject of rcdmReference("Patient", "456")
        }
        val observation = RCDMObservationToCTDMObservation(subjectDAO, dataDictionaryService, rcdmHelper).rcdmObservationToCTDMObservation("test-fhirId", rcdmObservation)
        R4ObservationValidator.validate(observation!!).alertIfErrors()
        assertEquals(observation.meta!!.profile[0].value, "https://projectronin.io/fhir/StructureDefinition/CTDM-Observation")
    }

    @Test
    fun `can transform BSA observation`() {
        val rcdmObservation = rcdmObservationBodySurfaceArea("test") {
            subject of rcdmReference("Patient", "456")
        }
        val observation = RCDMObservationToCTDMObservation(subjectDAO, dataDictionaryService, rcdmHelper).rcdmObservationToCTDMObservation("test-fhirId", rcdmObservation)
        R4ObservationValidator.validate(observation!!).alertIfErrors()
        assertEquals(observation.meta!!.profile[0].value, "https://projectronin.io/fhir/StructureDefinition/CTDM-Observation")
    }

    @Test
    fun `can transform lab observation with most fields`() {
        val rcdmObservation = rcdmObservationLaboratoryResult("test") {
            status of Code("corrected")
            category plus codeableConcept {
                coding of listOf(
                    coding {
                        system of CodeSystem.OBSERVATION_CATEGORY.uri
                        code of Code("laboratory")
                    }
                )
            }
            code of codeableConcept {
                coding of listOf(
                    coding {
                        system of "http://loinc.org"
                        code of Code("30313-1")
                        display of "Hemoglobin [Mass/volume] in Arterial blood"
                    }
                )
            }
            effective of DynamicValues.dateTime("2023-08-08")
            issued of instant {}
            interpretation plus codeableConcept {
                coding of listOf(
                    coding {
                        system of "http://terminology.hl7.org/CodeSystem/data-absent-reason"
                        code of Code("unknown")
                        display of "Unknown"
                    }
                )
            }
            subject of rcdmReference("Patient", "456")
            note plus annotation {
                text of markdown {
                    value of "Standard Biopsy"
                }
            }
            bodySite of codeableConcept {
                coding plus coding {
                    system of "http://snomed.info/sct"
                    code of "368225008"
                    display of "Entire Left Forearm"
                }
            }
            method of codeableConcept {
                coding of listOf(
                    coding {
                        system of "http://terminology.hl7.org/CodeSystem/data-absent-reason"
                        code of Code("unknown")
                        display of "Unknown"
                    }
                )
            }
            referenceRange plus observationReferenceRange {
                text of "There's a reference range here"
            }
            component plus observationComponent {}
        }
        val observation = RCDMObservationToCTDMObservation(subjectDAO, dataDictionaryService, rcdmHelper).rcdmObservationToCTDMObservation("test-fhirId", rcdmObservation)
        R4ObservationValidator.validate(observation!!).alertIfErrors()
        assertEquals(observation.meta!!.profile[0].value, "https://projectronin.io/fhir/StructureDefinition/CTDM-Observation")
    }

    @Test
    fun `can transform lab observation with data absent reason`() {
        val rcdmObservation = rcdmObservation("test") {
            status of Code("corrected")
            category plus codeableConcept {
                coding of listOf(
                    coding {
                        system of CodeSystem.OBSERVATION_CATEGORY.uri
                        code of Code("laboratory")
                    }
                )
            }
            code of codeableConcept {
                coding of listOf(
                    coding {
                        system of "http://loinc.org"
                        code of Code("30313-1")
                        display of "Hemoglobin [Mass/volume] in Arterial blood"
                    }
                )
            }
            dataAbsentReason of codeableConcept {
                coding of listOf(
                    coding {
                        system of "http://terminology.hl7.org/CodeSystem/data-absent-reason"
                        code of Code("unknown")
                        display of "Unknown"
                    }
                )
            }
            subject of rcdmReference("Patient", "456")
        }
        val observation = RCDMObservationToCTDMObservation(subjectDAO, dataDictionaryService, rcdmHelper).rcdmObservationToCTDMObservation("test-fhirId", rcdmObservation)
        R4ObservationValidator.validate(observation!!).alertIfErrors()
        assertEquals(observation.meta!!.profile[0].value, "https://projectronin.io/fhir/StructureDefinition/CTDM-Observation")
    }

    @Test
    fun `invalid observation code`() {
        val rcdmObservation = rcdmObservationLaboratoryResult("test") {
            subject of rcdmReference("Patient", "456")
            code of codeableConcept {
                coding of listOf(
                    coding {
                        system of "http://loinc.org"
                        code of Code("30-1")
                        display of "Hemoglobin [Mass/volume] in Arterial blood"
                    }
                )
            }
        }
        val exception = assertThrows<Exception> {
            RCDMObservationToCTDMObservation(subjectDAO, dataDictionaryService, rcdmHelper).rcdmObservationToCTDMObservation("test-fhirId", rcdmObservation)
        }
        assertEquals("Observation code 30-1 not in data dictionary for CTDM", exception.message)
    }

    @Test
    fun `subject reference not a patient`() {
        val rcdmObservation = rcdmObservationLaboratoryResult("test") {
            subject of rcdmReference("Location", "456")
            code of codeableConcept {
                coding of listOf(
                    coding {
                        system of "http://loinc.org"
                        code of Code("30313-1")
                        display of "Hemoglobin [Mass/volume] in Arterial blood"
                    }
                )
            }
        }
        val observation = RCDMObservationToCTDMObservation(subjectDAO, dataDictionaryService, rcdmHelper).rcdmObservationToCTDMObservation("test-fhirId", rcdmObservation)
        assertNull(observation)
    }

    @Test
    fun `no subject found for fhir id`() {
        val rcdmObservation = rcdmObservationLaboratoryResult("test") {
            subject of rcdmReference("Patient", "456")
            code of codeableConcept {
                coding of listOf(
                    coding {
                        system of "http://loinc.org"
                        code of Code("30313-1")
                        display of "Hemoglobin [Mass/volume] in Arterial blood"
                    }
                )
            }
        }

        val exception = assertThrows<Exception> {
            RCDMObservationToCTDMObservation(subjectDAO, dataDictionaryService, rcdmHelper).rcdmObservationToCTDMObservation("test-notfhirId", rcdmObservation)
        }
        assertEquals("No subject found for Ronin FHIR Id: test-notfhirId", exception.message)
    }
}
