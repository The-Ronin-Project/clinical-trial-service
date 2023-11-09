package com.projectronin.clinical.trial.server.controller

import com.projectronin.clinical.trial.server.data.model.StudySiteDO
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.clinical.trial.server.data.model.SubjectStatusDO
import com.projectronin.clinical.trial.server.services.SubjectService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.UUID

class SubjectStatusControllerTest {
    private var subjectService = mockk<SubjectService>()
    private var subjectStatusController = SubjectStatusController(subjectService)

    private val studySiteId1 = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770")

    @Test
    fun `retrieve - invalid study subject combo`() {
        every { subjectService.getStudySiteByStudyIdAndSiteId("1", "2") } returns null

        val response = subjectStatusController.retrieve("1", "2", "1")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `retrieve - invalid  combo`() {
        val studySite = StudySiteDO()
        studySite["studySiteId"] = studySiteId1
        studySite["studyId"] = "1"
        studySite["siteId"] = "2"

        every { subjectService.getStudySiteByStudyIdAndSiteId("1", "2") } returns studySite
        every { subjectService.getSubjectStatus("1", studySiteId1) } returns null

        val response = subjectStatusController.retrieve("1", "2", "1")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun `retrieve - valid subject status`() {
        val studySite = StudySiteDO()
        studySite["studySiteId"] = studySiteId1
        studySite["studyId"] = "1"
        studySite["siteId"] = "2"

        val expectedSubjectStatus = SubjectStatusDO {
            studySiteId = studySiteId1
            subjectId = "1"
            status = SubjectStatus.ACTIVE
        }

        every { subjectService.getStudySiteByStudyIdAndSiteId("1", "2") } returns studySite
        every { subjectService.getSubjectStatus("1", studySiteId1) } returns expectedSubjectStatus

        val response = subjectStatusController.retrieve("1", "2", "1")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(response.body, SubjectStatus.ACTIVE)
    }

    @Test
    fun `update - handles invalid status`() {
        val response = subjectStatusController.update("1", "2", "1", UpdateStatusRequest("NOT_VALID_STATUS"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(response.body!!.message, "Status must be one of NEW, SCREENED, ACTIVE, ENROLLED, SCREEN_FAILED, WITHDRAWN, COMPLETE.")
    }

    @Test
    fun `update - no StudySite found`() {
        val studySite = StudySiteDO()
        studySite["studySiteId"] = studySiteId1
        studySite["studyId"] = "1"
        studySite["siteId"] = "2"

        every { subjectService.getStudySiteByStudyIdAndSiteId("1", "2") } returns null

        val response = subjectStatusController.update("1", "2", "1", UpdateStatusRequest("ACTIVE"))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(response.body!!.message, "No study found for ID 1 at Site 2.")
    }

    @Test
    fun `update - no status found for subject`() {
        val studySite = StudySiteDO()
        studySite["studySiteId"] = studySiteId1
        studySite["studyId"] = "1"
        studySite["siteId"] = "2"

        every { subjectService.getStudySiteByStudyIdAndSiteId("1", "2") } returns studySite
        every { subjectService.updateSubjectStatus("1", studySiteId1, any()) } returns null

        val response = subjectStatusController.update("1", "2", "1", UpdateStatusRequest("ACTIVE"))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(response.body!!.message, "No subject 1 found in study 1 at site 2")
    }

    @Test
    fun `update - returns updated status`() {
        val studySite = StudySiteDO()
        studySite["studySiteId"] = studySiteId1
        studySite["studyId"] = "1"
        studySite["siteId"] = "2"

        val expectedSubjectStatus = SubjectStatusDO {
            studySiteId = studySiteId1
            subjectId = "1"
            status = SubjectStatus.WITHDRAWN
        }

        every { subjectService.getStudySiteByStudyIdAndSiteId("1", "2") } returns studySite
        every { subjectService.updateSubjectStatus("1", studySiteId1, any()) } returns expectedSubjectStatus

        val response = subjectStatusController.update("1", "2", "1", UpdateStatusRequest("WITHDRAWN"))

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(response.body!!.message, "Enrollment status updated successfully.")
    }
}
