package com.projectronin.clinical.trial.server.clinicalone.model

data class ClinicalOneAddSubjectPayload(
    val subject: ClinicalOneAddSubjectInnerPayload
) {
    data class ClinicalOneAddSubjectInnerPayload(
        val siteId: String,
        val studyId: String,
        val state: String = "new"
    )
}
