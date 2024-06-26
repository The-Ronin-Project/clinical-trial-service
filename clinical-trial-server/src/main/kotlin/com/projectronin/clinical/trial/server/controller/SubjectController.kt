package com.projectronin.clinical.trial.server.controller

import com.projectronin.clinical.trial.models.Subject
import com.projectronin.clinical.trial.server.kafka.DataLoadEventProducer
import com.projectronin.clinical.trial.server.services.SubjectService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("subjects")
class SubjectController(
    val subjectService: SubjectService,
    val dataLoadEventProducer: DataLoadEventProducer,
) {
    /**
     * Get Subjects from the clinical trial service.
     *
     * [activeIdsOnly] - when supplied, a list of Subjects will be returned which only
     * have the [Subject.roninFhirId] property populated. This functionality
     * is intended for mirth to grab all active patient ids.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_read:subject')")
    fun retrieve(
        @RequestParam activeIdsOnly: Boolean = false,
    ): ResponseEntity<List<Subject>> {
        val subjects =
            if (activeIdsOnly) {
                subjectService.getActiveFhirIds().map {
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

    // TODO: We should add tenantid and patientid to route and switch preauth to @PreAuthPatient
    @GetMapping("/{roninFhirId}")
    @PreAuthorize("authentication.providerRoninId != null")
    @CrossOrigin(origins = ["\${cors.ronin.frontend}"], maxAge = 1800)
    fun retrieveByRoninFhirId(
        @PathVariable roninFhirId: String,
    ): ResponseEntity<Subject> {
        val subject = subjectService.getSubjectsByRoninFhirId(roninFhirId)
        return if (subject != null) {
            ResponseEntity.ok(subject)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // TODO: We should add tenantid and patientid to route and switch preauth to @PreAuthPatient
    @PutMapping
    @PreAuthorize("authentication.providerRoninId != null")
    @CrossOrigin(origins = ["\${cors.ronin.frontend}"], maxAge = 1800)
    fun createWithSubjectNumber(
        @RequestBody subject: Subject,
    ): ResponseEntity<Subject> {
        subjectService.createSubjectWithSubjectNumber(subject)?.let {
            dataLoadEventProducer.producePatientResourceRequest(it.roninFhirId, it.roninFhirId.split("-").first())
            return ResponseEntity(it, HttpStatus.CREATED)
        }
        return ResponseEntity.internalServerError().build()
    }

    @PostMapping
    @PreAuthorize("authentication.providerRoninId != null")
    @CrossOrigin(origins = ["\${cors.ronin.frontend}"], maxAge = 1800)
    fun create(
        @RequestBody subject: Subject,
    ): ResponseEntity<Subject> {
        subjectService.createSubject(subject)?.let {
            dataLoadEventProducer.producePatientResourceRequest(it.roninFhirId, it.roninFhirId.split("-").first())
            return ResponseEntity(it, HttpStatus.CREATED)
        }

        return ResponseEntity.internalServerError().build()
    }
}
