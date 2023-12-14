package com.projectronin.clinical.trial.server.kafka

import com.projectronin.clinical.trial.server.dataauthority.ObservationDAO
import com.projectronin.clinical.trial.server.services.SubjectService
import com.projectronin.clinical.trial.server.transform.RCDMObservationToCTDMObservation
import com.projectronin.clinical.trial.server.transform.RCDMPatientToCTDMObservations
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
    private val listener = EHRDAListener(subjectService, patientTransformer, observationTransformer, observationDAO)

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
    }

    @Test
    fun `observation listener works`() {
        val message =
            mockk<RoninEvent<Observation>> {
                every { data } returns
                    mockk {
                        every { subject?.decomposedId() } returns "ronincer-patientId1"
                    }
                every { tenantId } returns "ronincer"
            }
        every { subjectService.getActiveFhirIds() } returns setOf("ronincer-patientId1")
        every { observationTransformer.rcdmObservationToCTDMObservation("ronincer-patientId1", any()) } returns mockk()
        assertDoesNotThrow { listener.consumeObservation(message) }
    }
}
