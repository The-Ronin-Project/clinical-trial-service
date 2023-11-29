package com.projectronin.clinical.trial.server.controller

import com.projectronin.clinical.trial.server.dataauthority.ObservationDAO
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

internal class ObservationControllerTest {
    private val obsDao = mockk<ObservationDAO>()
    private val controller = ObservationController(obsDao)

    @Test
    fun `works`() {
        every { obsDao.search(any()) } returns listOf(mockk())
        assertDoesNotThrow { controller.retrieve("12345") }
    }
}
