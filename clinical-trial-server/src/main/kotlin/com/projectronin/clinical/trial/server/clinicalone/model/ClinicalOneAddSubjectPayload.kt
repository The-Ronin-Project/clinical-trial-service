package com.projectronin.clinical.trial.server.clinicalone.model

data class ClinicalOneAddSubjectPayload(
    val subject: ClinicalOneAddSubjectInnerPayload
) {
    data class ClinicalOneAddSubjectInnerPayload(
        val screeningDate: String? = null,
        val codeBreak: String? = null,
        val userId: String? = null,
        val id: String? = null,
        val versionStart: String? = null,
        val description: String? = null,
        val subjectNumber: String? = null,
        val studyId: String,
        val siteId: String,
        val dob: String? = null,
        val state: String? = null,
        val stateDate: String? = null,
        val screeningFailure: String? = null,
        val enrollmentFailure: String? = null,
        val enrollmentOverride: String? = null,
        val informedConsentDate: String? = null,
        val gender: String? = null,
        val reason: String? = null,
        val comment: String? = null,
        val studyVersion: String? = null
    )
}
