package com.projectronin.clinical.trial.server.model

data class Subject(
    val id: String = "",
    val roninFhirId: String,
    val siteId: String = "",
    val status: String = "",
    val studyId: String = ""
)
