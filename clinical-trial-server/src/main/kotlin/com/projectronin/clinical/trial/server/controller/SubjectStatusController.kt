package com.projectronin.clinical.trial.server.controller

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.clinical.trial.server.services.SubjectService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class UpdateStatusRequest(
    val status: String
)

data class StatusResponse(
    val message: String
)

@RestController
class SubjectStatusController(
    val subjectService: SubjectService
) {
    // TODO: revisit scopes
    @GetMapping("studies/{studyId}/sites/{siteId}/subject/{subjectId}/status")
    @PreAuthorize("hasAuthority('SCOPE_read:resources')")
    fun retrieve(
        @PathVariable studyId: String,
        @PathVariable siteId: String,
        @PathVariable subjectId: String
    ): ResponseEntity<SubjectStatus> {
        val studySite = subjectService.getStudySiteByStudyIdAndSiteId(studyId, siteId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        val subjectStatus = subjectService.getSubjectStatus(subjectId, studySite.studySiteId) ?: return ResponseEntity(HttpStatus.NOT_FOUND)
        return ResponseEntity(subjectStatus.status, HttpStatus.OK)
    }

    // TODO: revisit scopes
    @PostMapping("studies/{studyId}/sites/{siteId}/subject/{subjectId}/status")
    @PreAuthorize("hasAuthority('SCOPE_update:resources')")
    fun update(
        @PathVariable studyId: String,
        @PathVariable siteId: String,
        @PathVariable subjectId: String,
        @RequestBody request: UpdateStatusRequest
    ): ResponseEntity<StatusResponse> {
        val status = request.status.let {
            runCatching { SubjectStatus.valueOf(it) }.getOrNull()
        } ?: return ResponseEntity(StatusResponse("Status must be one of ${SubjectStatus.values().joinToString()}."), HttpStatus.BAD_REQUEST)
        val studySite = subjectService.getStudySiteByStudyIdAndSiteId(studyId, siteId) ?: return ResponseEntity(StatusResponse("No study found for ID $studyId at Site $siteId."), HttpStatus.NOT_FOUND)
        subjectService.updateSubjectStatus(subjectId, studySite.studySiteId, status) ?: return ResponseEntity(StatusResponse("No subject $subjectId found in study $studyId at site $siteId"), HttpStatus.NOT_FOUND)
        return ResponseEntity(StatusResponse("Enrollment status updated successfully."), HttpStatus.OK)
    }
}
