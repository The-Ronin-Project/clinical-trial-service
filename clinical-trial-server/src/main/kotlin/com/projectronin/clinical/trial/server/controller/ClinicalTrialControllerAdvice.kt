package com.projectronin.clinical.trial.server.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

data class ErrorResponse(
    val message: String?,
)

@ControllerAdvice
class ClinicalTrialControllerAdvice {
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(e.message)
        return ResponseEntity(body, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(SubjectNotFoundException::class)
    fun handleNotFoundException(e: SubjectNotFoundException): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(e.message)
        return ResponseEntity(body, HttpStatus.NOT_FOUND)
    }
}
