package com.projectronin.clinical.trial.server.controller

import com.projectronin.clinical.trial.server.dataauthority.ObservationDAO
import com.projectronin.interop.fhir.r4.resource.Observation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("internal/observations")
class ObservationController(
    val observationDAO: ObservationDAO
) {

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_read:resources')")
    fun retrieve(
        @RequestParam patientFhirId: String
    ): ResponseEntity<List<Observation>> {
        val observations = observationDAO.search(subject = "Patient/$patientFhirId")
        return ResponseEntity(observations, HttpStatus.OK)
    }
}
