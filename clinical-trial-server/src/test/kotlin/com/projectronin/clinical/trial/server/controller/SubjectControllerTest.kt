package com.projectronin.clinical.trial.server.controller

import com.projectronin.clinical.trial.models.Subject
import com.projectronin.clinical.trial.server.kafka.DataLoadEventProducer
import com.projectronin.clinical.trial.server.services.SubjectService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class SubjectControllerTest {
    private var subjectService = mockk<SubjectService>()
    private var dataLoadEventProducer =
        mockk<DataLoadEventProducer> {
            every { producePatientResourceRequest(any(), any()) } just Runs
        }
    private var subjectController = SubjectController(subjectService, dataLoadEventProducer)

    private val expectedPatientIds =
        listOf(
            "tenant-patientId1",
            "tenant-patientId2",
            "tenant-patientId3",
        )
    private val subjectStatuses = listOf("ACTIVE", "WITHDRAWN")

    private val expectedActiveSubjects =
        listOf(
            Subject(roninFhirId = "${expectedPatientIds[0]}"),
            Subject(roninFhirId = "${expectedPatientIds[1]}"),
            Subject(roninFhirId = "${expectedPatientIds[2]}"),
        )

    private val subjectFhirID = "tenant-patientFhirId"
    private val subjectToCreate =
        Subject(
            id = "",
            roninFhirId = "$subjectFhirID",
            siteId = "siteId",
            status = "ACTIVE",
            studyId = "UUID",
        )

    private val createdSubject =
        Subject(
            id = "subjectId",
            roninFhirId = "tenant-patientFhirId",
            siteId = "siteId",
            status = "ACTIVE",
            studyId = "UUID",
        )

    @Test
    fun `get returns active subjects - list is empty`() {
        every { subjectService.getActiveFhirIds() } returns emptySet()
        val response = subjectController.retrieve(true)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `get returns active subjects - list is populated`() {
        every { subjectService.getActiveFhirIds() } returns expectedPatientIds.toSet()
        val response = subjectController.retrieve(true)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedActiveSubjects, response.body)
    }

    @Test
    fun `get without activesubjectids returns 404`() {
        val response = subjectController.retrieve(false)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `get with default activesubjectids returns 404`() {
        val response = subjectController.retrieve()
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `get by roninfhirid with invalid fhirId returns null`() {
        every { subjectService.getSubjectsByRoninFhirId(any()) } returns null
        val response = subjectController.retrieveByRoninFhirId("invalid-fhirId")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `get by roninfhirid with valid fhirId returns subject`() {
        every { subjectService.getSubjectsByRoninFhirId(any()) } returns createdSubject
        val response = subjectController.retrieveByRoninFhirId(createdSubject.roninFhirId)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(response.body?.roninFhirId, createdSubject.roninFhirId)
    }

    @Test
    fun `post returns created subject`() {
        every { subjectService.createSubject(subjectToCreate) } returns createdSubject
        val response = subjectController.create(subjectToCreate)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(createdSubject, response.body)
    }

    @Test
    fun `post errors`() {
        every { subjectService.createSubject(subjectToCreate) } returns null
        val response = subjectController.create(subjectToCreate)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }

    @Test
    fun `put returns created subject`() {
        every { subjectService.createSubjectWithSubjectNumber(subjectToCreate) } returns createdSubject
        val response = subjectController.createWithSubjectNumber(subjectToCreate)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(createdSubject, response.body)
    }

    @Test
    fun `put errors`() {
        every { subjectService.createSubjectWithSubjectNumber(subjectToCreate) } returns null
        val response = subjectController.createWithSubjectNumber(subjectToCreate)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }
}
