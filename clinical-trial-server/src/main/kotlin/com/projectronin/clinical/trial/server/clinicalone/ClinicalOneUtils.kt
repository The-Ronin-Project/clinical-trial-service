package com.projectronin.clinical.trial.server.clinicalone

import com.projectronin.clinical.trial.server.clinicalone.model.ClinicalOneAddSubjectPayload

fun createAddSubjectPayload(siteId: String, studyId: String): ClinicalOneAddSubjectPayload =
    ClinicalOneAddSubjectPayload(
        ClinicalOneAddSubjectPayload.ClinicalOneAddSubjectInnerPayload(
            siteId = siteId,
            studyId = studyId
        )
    )
