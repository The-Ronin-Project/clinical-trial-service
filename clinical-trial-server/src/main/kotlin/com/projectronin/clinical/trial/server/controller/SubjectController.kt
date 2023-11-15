package com.projectronin.clinical.trial.server.controller

import com.projectronin.clinical.trial.models.Subject
import com.projectronin.clinical.trial.server.kafka.ActivePatientService
import com.projectronin.clinical.trial.server.services.SubjectService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("subjects")
class SubjectController(
    val subjectService: SubjectService,
    val activePatientService: ActivePatientService
) {
    /**
     * Get Subjects from the clinical trial service.
     *
     * [activeIdsOnly] - when supplied, a list of Subjects will be returned which only
     * have the [Subject.roninFhirId] property populated. This functionality
     * is intended for mirth to grab all active patient ids.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_read:resources')")
    fun retrieve(
        @RequestParam activeIdsOnly: Boolean = false
    ): ResponseEntity<List<Subject>> {
        val subjects = if (activeIdsOnly) {
            activePatientService.getActivePatients().map {
                Subject(roninFhirId = it)
            }
        } else {
            emptyList()
        }

        return if (subjects.isNotEmpty()) {
            ResponseEntity.ok(subjects)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_create:resources')")
    fun create(
        @RequestBody subject: Subject
    ): ResponseEntity<Subject> {
        subjectService.createSubject(subject)?.let {
            activePatientService.addActivePatient(it.roninFhirId)
            return ResponseEntity(it, HttpStatus.CREATED)
        }

        return ResponseEntity.internalServerError().build()
    }
}
