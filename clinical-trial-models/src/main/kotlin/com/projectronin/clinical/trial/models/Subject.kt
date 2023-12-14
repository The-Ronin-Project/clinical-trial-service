package com.projectronin.clinical.trial.models

data class Subject(
    val id: String = "",
    val roninFhirId: String,
    val siteId: String = "",
    val status: String = "",
    val studyId: String = "",
)
