package com.projectronin.clinical.trial.server.controller

import com.projectronin.clinical.trial.server.services.ObservationService
import com.projectronin.clinical.trial.server.services.SubjectService
import com.projectronin.clinical.trial.server.transform.DataDictionaryService
import com.projectronin.interop.fhir.generators.resources.observation
import com.projectronin.interop.fhir.r4.datatype.Coding
import com.projectronin.interop.fhir.r4.datatype.Meta
import com.projectronin.interop.fhir.r4.datatype.Reference
import com.projectronin.interop.fhir.r4.datatype.primitive.FHIRString
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.lang.IllegalArgumentException
import java.time.ZoneId
import java.time.ZonedDateTime

internal class ObservationControllerTest {

    private var observationService = mockk<ObservationService>()
    private var subjectService = mockk<SubjectService>()
    private var dataDictionaryService = mockk<DataDictionaryService>()
    private var observationController = ObservationController(subjectService, observationService, dataDictionaryService)

    private val startTime = ZonedDateTime.of(2023, 11, 11, 0, 0, 0, 0, ZoneId.of("UTC"))
    private val endTime = ZonedDateTime.of(2023, 12, 12, 0, 0, 0, 0, ZoneId.of("UTC"))

    @Test
    fun `request body - invalid params`() {
        assertThrows<IllegalArgumentException> {
            GetObservationsRequest(
                listOf("lab"),
                ObservationDateRange(startTime, endTime),
                -1
            )
        }

        assertThrows<IllegalArgumentException> {
            GetObservationsRequest(
                listOf("lab"),
                ObservationDateRange(startTime, endTime),
                2,
                1000
            )
        }

        assertThrows<IllegalArgumentException> {
            GetObservationsRequest(
                listOf("lab"),
                ObservationDateRange("2023-11-11", "2023-12-888")
            )
        }
    }

    @Test
    fun `retrieve - empty response`() {
        val requestBody = GetObservationsRequest(
            listOf("lab"),
            ObservationDateRange(startTime, endTime),
            1,
            10
        )

        val expectedResponse = GetObservationsResponse(
            "subjectID",
            emptyList(),
            Pagination(1, 10, false, 0)
        )

        every { subjectService.getFhirIdBySubjectId("subjectID") } returns "fhirID"
        every { dataDictionaryService.getValueSetUuidVersionByDisplay("lab") } returns Pair("1", "version")
        every { observationService.getObservations("fhirID", listOf("1"), startTime, endTime) } returns emptyList()

        val response = observationController.retrieve("studyID", "siteID", "subjectID", requestBody)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(response.body, expectedResponse)
    }

    @Test
    fun `retrieve - response has more`() {
        val requestBody = GetObservationsRequest(
            listOf("lab"),
            ObservationDateRange(startTime, endTime),
            1,
            10
        )

        val observations = (1..20).toList().map {
            observation {
                id of Id("id-$it")
                meta of Meta(tag = listOf(Coding(system = Uri("1"), display = FHIRString("lab"))))
            }
        }

        val expectedObservations = observations.slice(0..9)

        val expectedResponse = GetObservationsResponse(
            "subjectID",
            expectedObservations,
            Pagination(1, 10, true, 20)
        )

        every { subjectService.getFhirIdBySubjectId("subjectID") } returns "fhirID"
        every { dataDictionaryService.getValueSetUuidVersionByDisplay("lab") } returns Pair("1", "version")
        every { observationService.getObservations("fhirID", listOf("1"), startTime, endTime) } returns observations

        val response = observationController.retrieve("studyID", "siteID", "subjectID", requestBody)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse, response.body)
    }

    @Test
    fun `retrieve - response does not have more`() {
        val requestBody = GetObservationsRequest(
            listOf("lab"),
            ObservationDateRange(startTime, endTime),
            18,
            10
        )

        val observations = (1..20).toList().map {
            observation {
                id of Id("id-$it")
                meta of Meta(tag = listOf(Coding(system = Uri("1"), display = FHIRString("lab"))))
            }
        }

        val expectedObservations = observations.slice(17..19)

        val expectedResponse = GetObservationsResponse(
            "subjectID",
            expectedObservations,
            Pagination(18, 10, false, 20)
        )

        every { subjectService.getFhirIdBySubjectId("subjectID") } returns "fhirID"
        every { dataDictionaryService.getValueSetUuidVersionByDisplay("lab") } returns Pair("1", "version")
        every { observationService.getObservations("fhirID", listOf("1"), startTime, endTime) } returns observations

        val response = observationController.retrieve("studyID", "siteID", "subjectID", requestBody)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedResponse, response.body)
    }

    @Test
    fun `retrieve all internal`() {
        val expectedObservations = (1..20).toList().map {
            observation {
                id of Id("id-$it")
                subject of Reference(reference = FHIRString("Patient/fhirID"))
                meta of Meta(tag = listOf(Coding(system = Uri("1"), display = FHIRString("lab"))))
            }
        }

        every { observationService.getAllObservationsByPatientId("Patient/fhirID") } returns expectedObservations

        val response = observationController.retrieveInternal("fhirID")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(expectedObservations, response.body)
    }
}
