package com.projectronin.clinical.trial.server.transform

import com.projectronin.clinical.trial.server.data.SubjectDAO
import com.projectronin.interop.fhir.generators.datatypes.DynamicValues
import com.projectronin.interop.fhir.generators.datatypes.coding
import com.projectronin.interop.fhir.generators.datatypes.extension
import com.projectronin.interop.fhir.generators.datatypes.meta
import com.projectronin.interop.fhir.generators.primitives.of
import com.projectronin.interop.fhir.generators.resources.patientCommunication
import com.projectronin.interop.fhir.r4.CodeSystem
import com.projectronin.interop.fhir.r4.CodeableConcepts
import com.projectronin.interop.fhir.r4.datatype.CodeableConcept
import com.projectronin.interop.fhir.r4.datatype.Coding
import com.projectronin.interop.fhir.r4.datatype.Identifier
import com.projectronin.interop.fhir.r4.datatype.primitive.Canonical
import com.projectronin.interop.fhir.r4.datatype.primitive.Code
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import com.projectronin.interop.fhir.r4.datatype.primitive.asFHIR
import com.projectronin.interop.fhir.ronin.generators.resource.rcdmPatient
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RCDMPatientToCTDMObservationsTest {
    private lateinit var subjectDAO: SubjectDAO
    private val rcdmHelper =
        mockk<BaseRCDMToCTDMHelper> {
            every { setProfileMeta("Birth Date") } returns
                meta {
                    profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
                    tag of
                        listOf(
                            coding {
                                system of "BirthDateUUID"
                                display of "Birth Date"
                                version of "1"
                            },
                        )
                }
            every { setProfileMeta("Sex") } returns
                meta {
                    profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
                    tag of
                        listOf(
                            coding {
                                system of "SexUUID"
                                display of "Sex"
                                version of "1"
                            },
                        )
                }
            every { setProfileMeta("Marital Status") } returns
                meta {
                    profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
                    tag of
                        listOf(
                            coding {
                                system of "MaritalStatusUUID"
                                display of "Marital Status"
                                version of "1"
                            },
                        )
                }
            every { setProfileMeta("Multiple Birth") } returns
                meta {
                    profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
                    tag of
                        listOf(
                            coding {
                                system of "MultipleBirthUUID"
                                display of "Multiple Birth"
                                version of "1"
                            },
                        )
                }
            every { setProfileMeta("Communication Language") } returns
                meta {
                    profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
                    tag of
                        listOf(
                            coding {
                                system of "CommunicationLanguageUUID"
                                display of "Communication Language"
                                version of "1"
                            },
                        )
                }
            every { setProfileMeta("Religion") } returns
                meta {
                    profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
                    tag of
                        listOf(
                            coding {
                                system of "ReligionUUID"
                                display of "Religion"
                                version of "1"
                            },
                        )
                }
            every { setProfileMeta("Birth Sex Observation") } returns
                meta {
                    profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
                    tag of
                        listOf(
                            coding {
                                system of "BirthSexObservationUUID"
                                display of "Birth Sex Observation"
                                version of "1"
                            },
                        )
                }
            every { setProfileMeta("Gender Identity") } returns
                meta {
                    profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
                    tag of
                        listOf(
                            coding {
                                system of "GenderIdentityUUID"
                                display of "Gender Identity"
                                version of "1"
                            },
                        )
                }
            every { setProfileMeta("Ethnicity") } returns
                meta {
                    profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
                    tag of
                        listOf(
                            coding {
                                system of "EthnicityUUID"
                                display of "Ethnicity"
                                version of "1"
                            },
                        )
                }
            every { setProfileMeta("Race") } returns
                meta {
                    profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
                    tag of
                        listOf(
                            coding {
                                system of "RaceUUID"
                                display of "Race"
                                version of "1"
                            },
                        )
                }
        }
    val roninFhir =
        Identifier(
            system = CodeSystem.RONIN_FHIR_ID.uri,
            value = "fhirId".asFHIR(),
            type = CodeableConcepts.RONIN_FHIR_ID,
        )

    val roninFhir2 =
        Identifier(
            system = CodeSystem.RONIN_FHIR_ID.uri,
            value = "notfhirId".asFHIR(),
            type = CodeableConcepts.RONIN_FHIR_ID,
        )

    @BeforeEach
    fun setup() {
        subjectDAO =
            mockk {
                every { getSubjectByFhirId("test-fhirId") } returns "subjectId"
                every { getSubjectByFhirId("test-notfhirId") } returns null
            }
    }

    @Test
    fun `split rcdm Patient with all fields`() {
        val rcdmPatient =
            rcdmPatient("test") {
                id of Id("fhirId")
                extension of
                    listOf(
                        extension {
                            url of "http://hl7.org/fhir/StructureDefinition/patient-religion"
                            value of DynamicValues.string("Catholic")
                        },
                        extension {
                            url of "http://hl7.org/fhir/us/core/StructureDefinition/us-core-birthsex"
                            value of DynamicValues.string("female")
                        },
                        extension {
                            url of "http://hl7.org/fhir/us/core/StructureDefinition/us-core-genderIdentity"
                            value of DynamicValues.string("they")
                        },
                        extension {
                            url of "http://hl7.org/fhir/us/core/StructureDefinition/us-core-race"
                            extension of
                                listOf(
                                    extension {
                                        url of "ombCategory"
                                        value of
                                            DynamicValues.codeableConcept(
                                                CodeableConcept(
                                                    coding =
                                                        listOf(
                                                            Coding(
                                                                system = Uri("https://loinc.org"),
                                                                version = "2.44".asFHIR(),
                                                                code = Code("2106-3"),
                                                                display = "Other".asFHIR(),
                                                            ),
                                                        ),
                                                ),
                                            )
                                    },
                                    extension {
                                        url of "detailed"
                                        value of
                                            DynamicValues.codeableConcept(
                                                CodeableConcept(
                                                    coding =
                                                        listOf(
                                                            Coding(
                                                                system = Uri("https://loinc.org"),
                                                                version = "2.44".asFHIR(),
                                                                code = Code("2500-7"),
                                                                display = "White".asFHIR(),
                                                            ),
                                                        ),
                                                ),
                                            )
                                    },
                                    extension {
                                        url of "text"
                                        value of DynamicValues.string("Asian")
                                    },
                                )
                        },
                        extension {
                            url of "http://hl7.org/fhir/us/core/StructureDefinition/us-core-ethnicity"
                            extension of
                                listOf(
                                    extension {
                                        url of "ombCategory"
                                        value of
                                            DynamicValues.codeableConcept(
                                                CodeableConcept(
                                                    coding =
                                                        listOf(
                                                            Coding(
                                                                system = Uri("https://loinc.org"),
                                                                version = "2.69".asFHIR(),
                                                                code = Code("UK"),
                                                                display = "Unknown".asFHIR(),
                                                            ),
                                                        ),
                                                ),
                                            )
                                    },
                                    extension {
                                        url of "detailed"
                                        value of
                                            DynamicValues.codeableConcept(
                                                CodeableConcept(
                                                    coding =
                                                        listOf(
                                                            Coding(
                                                                system = Uri("https://loinc.org"),
                                                                version = "2.63".asFHIR(),
                                                                code = Code("6"),
                                                                display = "White".asFHIR(),
                                                            ),
                                                        ),
                                                ),
                                            )
                                    },
                                    extension {
                                        url of "text"
                                        value of DynamicValues.string("Asian")
                                    },
                                )
                        },
                    )
                identifier of listOf(roninFhir)
                gender of Code("female")
                maritalStatus of CodeableConcept(text = "single".asFHIR())
                multipleBirth of DynamicValues.boolean(false)
                communication of
                    listOf(
                        patientCommunication {
                            language of CodeableConcept(text = "English".asFHIR())
                        },
                    )
            }

        val observations = RCDMPatientToCTDMObservations(subjectDAO, rcdmHelper).splitPatientDemographics(rcdmPatient)
        assertEquals(observations.size, 14)
        val metas = observations.map { it.meta }
        assertTrue(metas.contains(rcdmHelper.setProfileMeta("Marital Status")))
        assertTrue(metas.contains(rcdmHelper.setProfileMeta("Sex")))
        assertTrue(metas.contains(rcdmHelper.setProfileMeta("Race")))
        assertTrue(metas.contains(rcdmHelper.setProfileMeta("Ethnicity")))
    }

    @Test
    fun `split rcdm Patient with birth date only`() {
        val rcdmPatient =
            rcdmPatient("test") {
                id of Id("fhirId")
                identifier of listOf(roninFhir)
            }
        val observations = RCDMPatientToCTDMObservations(subjectDAO, rcdmHelper).splitPatientDemographics(rcdmPatient)
        assertEquals(observations.size, 2)
    }

    @Test
    fun `FHIR Id doesn't match subject`() {
        val rcdmPatient =
            rcdmPatient("test") {
                identifier of listOf(roninFhir2)
            }
        assertThrows<Exception> {
            RCDMPatientToCTDMObservations(subjectDAO, rcdmHelper).splitPatientDemographics(
                rcdmPatient,
            )
        }
    }

    @Test
    fun `create unique fhir id test`() {
        val id1 =
            RCDMPatientToCTDMObservations(subjectDAO, rcdmHelper).createUniqueFHIRID(
                "ronin-12345",
                "data-dictionary-12345",
            )
        val id2 =
            RCDMPatientToCTDMObservations(subjectDAO, rcdmHelper).createUniqueFHIRID(
                "ronin-12345",
                "data-dictionary-12345",
            )
        assertEquals(id1, id2)
    }
}
