package com.projectronin.clinical.trial.server.clinicalone.model

data class ClinicalOneAddSubjectResponse(
    val status: String? = null,
    val result: ClinicalOneAddSubjectResponseResult? = null,
    val errorData: String? = null

) {
    data class ClinicalOneAddSubjectResponseResult(
        val screeningDate: String? = null,
        val codeBreak: String? = null,
        val userId: String? = null,
        val eventType: String? = null,
        val subjectTransferId: String? = null,
        val objectVersionNumber: String? = null,
        val id: String? = null,
        val versionStart: String? = null,
        val description: String? = null,
        val subjectNumber: String? = null,
        val studyId: String? = null,
        val siteId: String? = null,
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
