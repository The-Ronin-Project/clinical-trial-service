package com.projectronin.clinical.trial.server.services

import com.projectronin.clinical.trial.server.dataauthority.ObservationDAO
import com.projectronin.interop.fhir.generators.resources.observation
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class ObservationServiceTest {
    private var observationDAO = mockk<ObservationDAO>()
    private var observationService = ObservationService(
        observationDAO
    )

    private val subjectId = "subjectId"
    private val fromDate = ZonedDateTime.of(2023, 11, 11, 0, 0, 0, 0, ZoneId.of("UTC"))
    private val toDate = ZonedDateTime.of(2023, 12, 11, 0, 0, 0, 0, ZoneId.of("UTC"))

    @Test
    fun getObservations() {
        val expected = listOf(observation { id of Id("id") })

        every { observationDAO.search(subjectId, emptyList(), fromDate, toDate) } returns expected

        val actual = observationService.getObservations(subjectId, types = emptyList(), fromDate, toDate)
        assertEquals(actual, expected)
    }

    @Test
    fun getAllObservationsBySubjectId() {
        val expected = listOf(observation { id of Id("id1") }, observation { id of Id("id2") })

        every { observationDAO.search(subjectId) } returns expected

        val actual = observationService.getAllObservationsBySubjectId(subjectId)
        assertEquals(actual, expected)
    }
}
