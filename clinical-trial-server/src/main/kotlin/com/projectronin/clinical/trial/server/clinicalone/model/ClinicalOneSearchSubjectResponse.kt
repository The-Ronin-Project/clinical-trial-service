package com.projectronin.clinical.trial.server.clinicalone.model

data class ClinicalOneSearchSubjectResponse(
    val status: String? = null,
    val result: List<ClinicalOneSubjectResponseResult>? = null,
    val errorData: String? = null,
)
