package com.projectronin.clinical.trial.server.kafka

import com.projectronin.clinical.trial.server.dataauthority.ObservationDAO
import com.projectronin.clinical.trial.server.services.SubjectService
import com.projectronin.clinical.trial.server.transform.DataDictionaryRow
import com.projectronin.clinical.trial.server.transform.DataDictionaryService
import com.projectronin.clinical.trial.server.transform.RCDMObservationToCTDMObservation
import com.projectronin.clinical.trial.server.transform.RCDMPatientToCTDMObservations
import com.projectronin.interop.fhir.r4.datatype.Coding
import com.projectronin.interop.fhir.r4.datatype.Reference
import com.projectronin.interop.fhir.r4.datatype.primitive.Code
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import com.projectronin.interop.fhir.r4.datatype.primitive.asFHIR
import com.projectronin.interop.fhir.r4.resource.Observation
import com.projectronin.interop.fhir.r4.resource.Patient
import com.projectronin.kafka.data.RoninEvent
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class EHRDAListenerTest {
    private var subjectService = mockk<SubjectService>()
    private var patientTransformer = mockk<RCDMPatientToCTDMObservations>()
    private var observationTransformer = mockk<RCDMObservationToCTDMObservation>()
    private var observationDAO =
        mockk<ObservationDAO> {
            every { update(any()) } just Runs
        }
    private var dictionaryService = mockk<DataDictionaryService>()
    private val listener = EHRDAListener(subjectService, patientTransformer, observationTransformer, observationDAO, dictionaryService)

    @Test
    fun `patient listener works`() {
        val message =
            mockk<RoninEvent<Patient>> {
                every { data } returns
                    mockk {
                        every { id?.value } returns "ronincer-patientId1"
                    }
                every { tenantId } returns "ronincer"
            }
        every { subjectService.getActiveFhirIds() } returns setOf("ronincer-patientId1")
        every { patientTransformer.splitPatientDemographics(any()) } returns listOf(mockk())
        assertDoesNotThrow { listener.consumePatient(message) }

        val message2 =
            mockk<RoninEvent<Patient>> {
                every { data } returns
                    mockk {
                        every { id?.value } returns null
                    }
                every { tenantId } returns "ronincer"
            }
        assertDoesNotThrow { listener.consumePatient(message2) }

        val message3 =
            mockk<RoninEvent<Patient>> {
                every { data } returns
                    mockk {
                        every { id } returns null
                    }
                every { tenantId } returns "ronincer"
            }
        assertDoesNotThrow { listener.consumePatient(message3) }
    }

    @Test
    fun `observation listener works with no data dictionary entry or bad id`() {
        val message =
            mockk<RoninEvent<Observation>> {
                every { data } returns
                    mockk {
                        every { subject?.decomposedId() } returns "ronincer-patientId1"
                        every { code?.coding?.get(0)?.system?.value } returns null
                        every { code?.coding?.get(0)?.code?.value } returns "37581-6"
                    }
                every { tenantId } returns "ronincer"
            }
        every { subjectService.getActiveFhirIds() } returns setOf("ronincer-patientId1")
        assertDoesNotThrow { listener.consumeObservation(message) }

        val message2 =
            mockk<RoninEvent<Observation>> {
                every { data } returns
                    mockk {
                        every { subject?.decomposedId() } returns "ronincer-patientId2"
                        every { code?.coding?.get(0)?.system?.value } returns "http://loinc.org"
                        every { code?.coding?.get(0)?.code?.value } returns null
                    }
                every { tenantId } returns "ronincer"
            }
        every { subjectService.getActiveFhirIds() } returns setOf("ronincer-patientId2")
        assertDoesNotThrow { listener.consumeObservation(message2) }

        val message3 =
            mockk<RoninEvent<Observation>> {
                every { data } returns
                    mockk {
                        every { subject?.decomposedId() } returns "potato-patientId3"
                        every { code?.coding?.get(0)?.system?.value } returns "http://loinc.org"
                        every { code?.coding?.get(0)?.code } returns null
                    }
                every { tenantId } returns "ronincer"
            }
        every { subjectService.getActiveFhirIds() } returns setOf("ronincer-patientId3")
        assertDoesNotThrow { listener.consumeObservation(message3) }

        val message4 =
            mockk<RoninEvent<Observation>> {
                every { data } returns
                    mockk {
                        every { subject } returns null
                        every { code?.coding?.get(0)?.system } returns null
                        every { code?.coding?.get(0)?.code?.value } returns "37581-6"
                    }
                every { tenantId } returns "ronincer"
            }
        assertDoesNotThrow { listener.consumeObservation(message4) }

        val message5 =
            mockk<RoninEvent<Observation>> {
                every { data } returns
                    mockk {
                        every { subject } returns Reference(reference = "potato-patientId3".asFHIR())
                        every { code?.coding?.get(0)?.system } returns Uri("http://loinc.org")
                        every { code?.coding?.get(0)?.code } returns Code("37581-6")
                    }
                every { tenantId } returns "ronincer"
            }
        assertDoesNotThrow { listener.consumeObservation(message5) }

        val message6 =
            mockk<RoninEvent<Observation>> {
                every { data } returns
                    mockk {
                        every { subject } returns Reference(reference = "potato-patientId3".asFHIR())
                        every { code?.coding?.get(0) } returns Coding()
                    }
                every { tenantId } returns "ronincer"
            }
        assertDoesNotThrow { listener.consumeObservation(message6) }

        val message7 =
            mockk<RoninEvent<Observation>> {
                every { data } returns
                    mockk {
                        every { subject } returns Reference(reference = "potato-patientId3".asFHIR())
                        every { code?.coding } returns listOf()
                    }
                every { tenantId } returns "ronincer"
            }
        assertDoesNotThrow { listener.consumeObservation(message7) }
        val message8 =
            mockk<RoninEvent<Observation>> {
                every { data } returns
                    mockk {
                        every { subject } returns Reference(reference = "potato-patientId3".asFHIR())
                        every { code } returns null
                    }
                every { tenantId } returns "ronincer"
            }
        assertDoesNotThrow { listener.consumeObservation(message8) }
    }

    @Test
    fun `observation listener works with data dictionary entry`() {
        val message =
            mockk<RoninEvent<Observation>> {
                every { data } returns
                    mockk {
                        every { subject?.decomposedId() } returns "ronincer-patientId1"
                        every { code?.coding?.get(0)?.system?.value } returns "http://loinc.org"
                        every { code?.coding?.get(0)?.code?.value } returns "37581-6"
                    }
                every { tenantId } returns "ronincer"
            }
        every { subjectService.getActiveFhirIds() } returns setOf("ronincer-patientId1")
        every {
            dictionaryService.getDataDictionaryByCode("http://loinc.org", any())
        } returns listOf(DataDictionaryRow("test", "test", "test", "test"))
        every { observationTransformer.rcdmObservationToCTDMObservation("ronincer-patientId1", any()) } returns mockk()
        assertDoesNotThrow { listener.consumeObservation(message) }
    }

    @Test
    fun `observation listener works when transform fails`() {
        val message =
            mockk<RoninEvent<Observation>> {
                every { data } returns
                    mockk {
                        every { subject?.decomposedId() } returns "ronincer-patientId1"
                        every { code?.coding?.get(0)?.system?.value } returns "http://loinc.org"
                        every { code?.coding?.get(0)?.code?.value } returns "37581-6"
                    }
                every { tenantId } returns "ronincer"
            }
        every { subjectService.getActiveFhirIds() } returns setOf("ronincer-patientId1")
        every {
            dictionaryService.getDataDictionaryByCode("http://loinc.org", any())
        } returns listOf(DataDictionaryRow("test", "test", "test", "test"))
        every { observationTransformer.rcdmObservationToCTDMObservation("ronincer-patientId1", any()) } returns null
        assertDoesNotThrow { listener.consumeObservation(message) }
    }
}
