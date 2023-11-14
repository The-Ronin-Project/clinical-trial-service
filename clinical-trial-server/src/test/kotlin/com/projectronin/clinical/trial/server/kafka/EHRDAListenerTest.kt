package com.projectronin.clinical.trial.server.kafka

import com.projectronin.clinical.trial.server.services.SubjectService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EHRDAListenerTest {

    // ActivePatientService class tests
    private lateinit var activePatientService: ActivePatientService
    private var subjectService = mockk<SubjectService>()
    private val patientId1 = "patientId1"
    private val patientId2 = "patientId2"
    private val activePatients = listOf(patientId1, patientId2)

    @BeforeEach
    fun setup() {
        activePatientService = ActivePatientService(subjectService)
    }

    @Test
    fun `initializes with some patient data`() {
        every { subjectService.getActiveFhirIds() } returns activePatients
        activePatientService.initialize()

        assertEquals(activePatients, activePatientService.getActivePatients())
    }

    @Test
    fun `patient is added`() {
        activePatientService.addActivePatient(patientId1)

        assertEquals(listOf(patientId1), activePatientService.getActivePatients())
    }

    @Test
    fun `patient is added with existing patient`() {
        activePatientService.addActivePatient(patientId1)
        activePatientService.addActivePatient(patientId2)

        assertEquals(listOf(patientId1, patientId2), activePatientService.getActivePatients())
    }

    @Test
    fun `patient is added as duplicate`() {
        activePatientService.addActivePatient(patientId1)
        activePatientService.addActivePatient(patientId1)

        assertEquals(listOf(patientId1), activePatientService.getActivePatients())
    }

    @Test
    fun `patient is removed when existing`() {
        activePatientService.addActivePatient(patientId1)
        activePatientService.removeActivePatient(patientId1)

        assertEquals(emptyList<String>(), activePatientService.getActivePatients())
    }

    @Test
    fun `removing patient without existing has no effect on existing`() {
        activePatientService.addActivePatient(patientId1)
        activePatientService.removeActivePatient(patientId2)

        assertEquals(listOf(patientId1), activePatientService.getActivePatients())
    }

    @Test
    fun `patient is not active when removed as only patient`() {
        activePatientService.addActivePatient(patientId1)
        activePatientService.removeActivePatient(patientId1)

        assertFalse(activePatientService.isActivePatient(patientId1))
    }

    @Test
    fun `patient is not active when removed with other patients active`() {
        activePatientService.addActivePatient(patientId1)
        activePatientService.removeActivePatient(patientId2)

        assertTrue(activePatientService.isActivePatient(patientId1))
        assertFalse(activePatientService.isActivePatient(patientId2))
    }
}
