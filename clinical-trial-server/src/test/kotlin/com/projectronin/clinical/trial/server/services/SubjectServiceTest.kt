package com.projectronin.clinical.trial.server.services
import com.projectronin.clinical.trial.server.data.StudySiteDAO
import com.projectronin.clinical.trial.server.data.SubjectStatusDAO
import com.projectronin.clinical.trial.server.data.model.StudySiteDO
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.clinical.trial.server.data.model.SubjectStatusDO
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class SubjectServiceTest {
    private var subjectStatusDAO = mockk<SubjectStatusDAO>()
    private var studySiteDAO = mockk<StudySiteDAO>()
    private var subjectService = SubjectService(
        subjectStatusDAO,
        studySiteDAO
    )

    private val subjectId = "subjectId"
    private val siteId = "siteId"
    private val studyId = "studyId"
    private val studySiteId1 = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770")
    private val studySiteId2 = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5771")

    @Test
    fun `get - SubjectStatus does exist`() {
        val status1 = SubjectStatusDO()
        status1["studySiteId"] = studySiteId1
        status1["subjectId"] = subjectId
        status1["status"] = SubjectStatus.ACTIVE

        val status2 = SubjectStatusDO()
        status2["studySiteId"] = studySiteId2
        status2["subjectId"] = subjectId
        status2["status"] = SubjectStatus.WITHDRAWN

        every { subjectStatusDAO.getSubjectStatusBySubjectId(subjectId) } returns listOf(status1, status2)

        val actual = subjectService.getSubjectStatus(subjectId, studySiteId1)!!
        assertEquals(actual, status1)
    }

    @Test
    fun `get - SubjectStatus does not exist`() {
        every { subjectStatusDAO.getSubjectStatusBySubjectId(subjectId) } returns listOf()

        val actual = subjectService.getSubjectStatus(subjectId, studySiteId1)
        assertNull(actual)
    }

    @Test
    fun `insert SubjectStatus`() {
        val expected = SubjectStatusDO()
        expected["studySiteId"] = studySiteId1
        expected["subjectId"] = subjectId
        expected["status"] = SubjectStatus.NEW

        every { subjectStatusDAO.insertSubjectStatus(any()) } returns Pair(studySiteId1, subjectId)

        subjectService.insertSubjectStatus(subjectId, studySiteId1, SubjectStatus.ACTIVE)
    }

    @Test
    fun `update SubjectStatus`() {
        val expected = SubjectStatusDO()
        expected["studySiteId"] = studySiteId1
        expected["subjectId"] = subjectId
        expected["status"] = SubjectStatus.NEW

        every { subjectStatusDAO.updateSubjectStatus(any(), any(), any()) } returns expected

        val actual = subjectService.updateSubjectStatus(subjectId, studySiteId1, SubjectStatus.ACTIVE)
        assertEquals(expected, actual)
    }

    @Test
    fun `get study site by study id and site id`() {
        val expected = StudySiteDO()
        expected["studySiteId"] = studySiteId1
        expected["studyId"] = studyId
        expected["siteId"] = siteId

        every { studySiteDAO.getStudySiteByStudyIdAndSiteId(any(), any()) } returns expected

        val actual = subjectService.getStudySiteByStudyIdAndSiteId(subjectId, siteId)
        assertEquals(expected, actual)
    }
}
