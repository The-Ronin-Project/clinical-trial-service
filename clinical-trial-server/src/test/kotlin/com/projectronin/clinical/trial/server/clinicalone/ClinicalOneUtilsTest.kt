package com.projectronin.clinical.trial.server.clinicalone

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClinicalOneUtilsTest {
    @Test
    fun `createAddSubjectPayload works`() {
        val siteId = "siteId"
        val studyId = "studyId"

        val result = createAddSubjectPayload(siteId, studyId)
        assertEquals(siteId, result.subject.siteId)
        assertEquals(studyId, result.subject.studyId)
    }
}
