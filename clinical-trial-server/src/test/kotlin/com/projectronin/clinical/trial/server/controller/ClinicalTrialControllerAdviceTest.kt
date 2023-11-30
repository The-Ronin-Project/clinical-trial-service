package com.projectronin.clinical.trial.server.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class ClinicalTrialControllerAdviceTest {
    private val advice = ClinicalTrialControllerAdvice()

    @Test
    fun `handle illegal request params`() {
        val expected = ResponseEntity(ErrorResponse("Unit test"), HttpStatus.BAD_REQUEST)
        val actual = advice.handleIllegalArgumentException(IllegalArgumentException("Unit test"))
        assertEquals(expected, actual)
    }

    @Test
    fun `handle subject not found`() {
        val expected = ResponseEntity(ErrorResponse("Unit test"), HttpStatus.NOT_FOUND)
        val actual = advice.handleNotFoundException(SubjectNotFoundException("Unit test"))
        assertEquals(expected, actual)
    }
}
