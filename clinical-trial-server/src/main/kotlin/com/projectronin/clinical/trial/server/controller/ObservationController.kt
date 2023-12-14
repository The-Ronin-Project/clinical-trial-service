package com.projectronin.clinical.trial.server.controller

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.projectronin.clinical.trial.server.services.ObservationService
import com.projectronin.clinical.trial.server.services.SubjectService
import com.projectronin.clinical.trial.server.util.parseFhirDateTime
import com.projectronin.interop.fhir.r4.resource.Observation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ObservationDateRange(
    val startDate: ZonedDateTime,
    val endDate: ZonedDateTime,
) {
    @JsonCreator
    constructor(startDate: String, endDate: String) :
        this(
            parseFhirDateTime(startDate) ?: throw IllegalArgumentException("Invalid start_date: $startDate"),
            parseFhirDateTime(endDate) ?: throw IllegalArgumentException("Invalid end_date: $endDate"),
        )
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetObservationsRequest(
    val observationName: List<String>,
    val dateRange: ObservationDateRange,
    val offset: Int = 1,
    val limit: Int = 10,
    val testMode: Boolean = false,
) {
    init {
        require(offset >= 1) { "offset must be at least 1." }
        require((1..100).contains(limit)) { "limit must be within [1, 100]." }
    }
}

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Pagination(
    val offset: Int,
    val limit: Int,
    val hasMore: Boolean,
    val totalCount: Int,
)

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GetObservationsResponse(
    val subjectId: String,
    val ctdmObservations: List<Observation>,
    val pagination: Pagination,
)

class SubjectNotFoundException(message: String) : Exception(message)

@RestController
class ObservationController(
    val subjectService: SubjectService,
    val observationService: ObservationService,
) {
    @PostMapping("studies/{studyId}/sites/{siteId}/subject/{subjectId}/observations")
    @PreAuthorize("hasAuthority('SCOPE_read:subject_data')")
    fun retrieve(
        @PathVariable studyId: String,
        @PathVariable siteId: String,
        @PathVariable subjectId: String,
        @RequestBody request: GetObservationsRequest,
    ): ResponseEntity<GetObservationsResponse> {
        val fromDate: ZonedDateTime = request.dateRange.startDate
        val toDate: ZonedDateTime = request.dateRange.endDate
        val observationNames = request.observationName
        val offset = request.offset
        val roninOffset = offset - 1 // 0 based indexing
        val limit = request.limit

        // get Fhir ID
        subjectService.getFhirIdBySubjectId(subjectId)
            ?: throw SubjectNotFoundException("Subject ID not found: $subjectId")

        // get Observations
        val observations = observationService.getObservations(subjectId, observationNames, fromDate, toDate)

        // pagination logic
        val page =
            if (observations.size > roninOffset) {
                observations.subList(roninOffset, observations.size).take(limit)
            } else {
                emptyList()
            }

        val response =
            GetObservationsResponse(
                subjectId = subjectId,
                ctdmObservations = page,
                pagination = Pagination(offset, limit, (offset + limit < observations.size), observations.size),
            )
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("internal/observations")
    @PreAuthorize("hasAuthority('SCOPE_read:subject_data')")
    fun retrieveInternal(
        @RequestParam patientFhirId: String,
    ): ResponseEntity<List<Observation>> {
        val subjectId =
            subjectService.getSubjectIdByFhirId(patientFhirId)
                ?: throw SubjectNotFoundException("Subject ID not found for Fhir ID: $patientFhirId")
        val observations = observationService.getAllObservationsBySubjectId(subjectId)
        return ResponseEntity(observations, HttpStatus.OK)
    }
}
