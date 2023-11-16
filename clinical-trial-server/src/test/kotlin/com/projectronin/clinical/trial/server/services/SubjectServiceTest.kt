package com.projectronin.clinical.trial.server.services

import com.projectronin.clinical.trial.models.Subject
import com.projectronin.clinical.trial.server.clinicalone.ClinicalOneClient
import com.projectronin.clinical.trial.server.data.StudySiteDAO
import com.projectronin.clinical.trial.server.data.SubjectDAO
import com.projectronin.clinical.trial.server.data.SubjectStatusDAO
import com.projectronin.clinical.trial.server.data.model.StudySiteDO
import com.projectronin.clinical.trial.server.data.model.SubjectDO
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.clinical.trial.server.data.model.SubjectStatusDO
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class SubjectServiceTest {
    private var clinicalOneClient = mockk<ClinicalOneClient>()
    private var subjectDAO = mockk<SubjectDAO>()
    private var subjectStatusDAO = mockk<SubjectStatusDAO>()
    private var studySiteDAO = mockk<StudySiteDAO>()
    private var subjectService = SubjectService(
        clinicalOneClient,
        studySiteDAO,
        subjectDAO,
        subjectStatusDAO
    )

    private val subjectId = "subjectId"
    private val siteId = "siteId"
    private val studyId = "studyId"
    private val roninFhirId = "tenant-id"
    private val status = "ACTIVE"
    private val studySiteId1 = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770")
    private val studySiteId2 = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5771")
    private val studySiteId = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770")

    private val subject = Subject(
        roninFhirId = roninFhirId,
        siteId = siteId,
        status = status,
        studyId = studyId
    )

    private val expectedSubjectId = "subjectId"
    private val expectedSubject = Subject(
        id = expectedSubjectId,
        roninFhirId = roninFhirId,
        siteId = siteId,
        status = status,
        studyId = studyId
    )

    private val studySiteDO: StudySiteDO = StudySiteDO {
        siteId = this@SubjectServiceTest.siteId
        studyId = this@SubjectServiceTest.studyId
    }

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

    @Test
    fun `create subject which doesn't exist`() {
        every { subjectDAO.getSubjectByFhirId(subject.roninFhirId) } returns null
        studySiteDO["studySiteId"] = studySiteId
        every { studySiteDAO.getStudySiteByStudyIdAndSiteId(subject.studyId, subject.siteId) } returns
            studySiteDO
        every { clinicalOneClient.getSubjectId(subject.siteId, subject.studyId) } returns expectedSubjectId
        every { subjectDAO.insertSubject(expectedSubject.toSubjectDO()) } returns ""
        every {
            subjectStatusDAO.insertSubjectStatus(
                SubjectStatusDO {
                    studySiteId = studySiteDO.studySiteId
                    subjectId = expectedSubjectId
                    status = SubjectStatus.ACTIVE
                }
            )
        } returns Pair(studySiteId, "")
        val actualSubject = subjectService.createSubject(subject)
        assertEquals(expectedSubject, actualSubject)
    }

    @Test
    fun `create subject which does exist`() {
        studySiteDO["studySiteId"] = studySiteId
        every { studySiteDAO.getStudySiteByStudyIdAndSiteId(subject.studyId, subject.siteId) } returns
            studySiteDO
        every { subjectDAO.getSubjectByFhirId(roninFhirId) } returns subjectId
        every {
            subjectStatusDAO.updateSubjectStatus(any(), any(), any())
        } returns mockk()

        val actualSubject = subjectService.createSubject(subject)
        assertEquals(expectedSubject, actualSubject)
    }

    @Test
    fun `update subject status`() {
        every {
            subjectStatusDAO.updateSubjectStatus(any(), any(), any())
        } returns mockk()

        val response = subjectService.updateSubjectStatus(subject, studySiteId, SubjectStatus.ACTIVE)
        assertEquals(Unit, response)
    }

    @Test
    fun `get fhir ids by status`() {
        val statuses = SubjectStatus.values().toList()
        every {
            subjectDAO.getFhirIdsByStatus(statuses)
        } returns setOf(roninFhirId)

        val fhirIds = subjectService.getFhirIdsByStatuses(statuses)
        assertEquals(setOf(roninFhirId), fhirIds)
    }

    @Test
    fun `get active fhir ids works`() {
        every {
            subjectDAO.getFhirIdsByStatus(listOf(SubjectStatus.ACTIVE, SubjectStatus.ENROLLED))
        } returns setOf(roninFhirId)

        val fhirIds = subjectService.getActiveFhirIds()
        assertEquals(setOf(roninFhirId), fhirIds)
    }

    // Utility
    private fun Subject.toSubjectDO(): SubjectDO {
        return SubjectDO {
            subjectId = this@toSubjectDO.id
            roninPatientId = this@toSubjectDO.roninFhirId
        }
    }
}
