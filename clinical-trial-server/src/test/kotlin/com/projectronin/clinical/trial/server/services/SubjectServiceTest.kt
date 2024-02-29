package com.projectronin.clinical.trial.server.services

import com.projectronin.clinical.trial.models.Subject
import com.projectronin.clinical.trial.server.clinicalone.ClinicalOneClient
import com.projectronin.clinical.trial.server.clinicalone.model.SubjectResult
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
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class SubjectServiceTest {
    private var clinicalOneClient = mockk<ClinicalOneClient>()
    private var subjectDAO = mockk<SubjectDAO>()
    private var subjectStatusDAO = mockk<SubjectStatusDAO>()
    private var studySiteDAO = mockk<StudySiteDAO>()
    private var subjectService =
        SubjectService(
            clinicalOneClient,
            studySiteDAO,
            subjectDAO,
            subjectStatusDAO,
        )

    private val subjectId = "subjectId"
    private val subjectNumber = "001-001"
    private val siteId = "siteId"
    private val studyId = "studyId"
    private val roninFhirId = "tenant-id"
    private val status = "ACTIVE"
    private val studySiteId1 = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770")
    private val studySiteId2 = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5771")
    private val studySiteId = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770")

    private val subject =
        Subject(
            roninFhirId = roninFhirId,
            siteId = siteId,
            status = status,
            studyId = studyId,
            number = subjectNumber,
        )

    private val expectedSubjectId = "subjectId"
    private val expectedSubject =
        Subject(
            id = expectedSubjectId,
            roninFhirId = roninFhirId,
            siteId = siteId,
            status = status,
            studyId = studyId,
            number = subjectNumber,
        )

    private val studySiteDO: StudySiteDO =
        StudySiteDO {
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
        every { subjectDAO.getFullSubjectByFhirId(subject.roninFhirId) } returns null
        studySiteDO["studySiteId"] = studySiteId
        every { studySiteDAO.getStudySiteByStudyIdAndSiteId(subject.studyId, subject.siteId) } returns
            studySiteDO
        every { clinicalOneClient.getSubjectIdAndSubjectNumber(subject.siteId, subject.studyId) } returns
            SubjectResult(
                expectedSubjectId,
                subjectNumber,
            )
        every { subjectDAO.insertSubject(expectedSubject.toSubjectDO()) } returns ""
        every {
            subjectStatusDAO.insertSubjectStatus(
                SubjectStatusDO {
                    studySiteId = studySiteDO.studySiteId
                    subjectId = expectedSubjectId
                    status = SubjectStatus.ACTIVE
                },
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
        every { subjectDAO.getFullSubjectByFhirId(roninFhirId) } returns expectedSubject.toSubjectDO()
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

    @Test
    fun `get subject ID by fhir ID`() {
        val expected = "subjectID"
        every {
            subjectDAO.getSubjectByFhirId("Patient/fhirID")
        } returns expected

        val subjectId = subjectService.getSubjectIdByFhirId("Patient/fhirID")
        assertEquals(expected, subjectId)
    }

    @Test
    fun `get subject by fhir ID`() {
        val expected = subject
        val subjectDO = subject.toSubjectDO()
        val subjectStatusDO =
            SubjectStatusDO {
                studySiteId = studySiteId1
                subjectId = subject.id
            }
        val studySiteDO =
            StudySiteDO {
                siteId = subject.siteId
                studyId = subject.studyId
            }
        every {
            subjectDAO.getSubjectByRoninFhirId("Patient/fhirID")
        } returns Triple(subjectDO, subjectStatusDO, studySiteDO)

        val res = subjectService.getSubjectsByRoninFhirId("Patient/fhirID")
        assertEquals(expected.roninFhirId, res?.roninFhirId)
    }

    @Test
    fun `get subject by fhir ID returns empty array when no records found`() {
        every {
            subjectDAO.getSubjectByRoninFhirId("Patient/fhirID")
        } returns null

        val res = subjectService.getSubjectsByRoninFhirId("Patient/fhirID")
        assertEquals(res, null)
    }

    @Test
    fun `get subject by site, study and number returns null when no records found`() {
        every {
            subjectDAO.getFullSubjectBySubjectNumberAndSiteIdAndStudyId("subjectNumber", "siteId", "studyId")
        } returns null

        val res = subjectService.getSubjectBySubjectNumberAndSiteIdAndStudyId("subjectNumber", "siteId", "studyId")
        assertEquals(res, null)
    }

    @Test
    fun `get subject by site, study and number`() {
        val expected = subject
        val subjectDO = subject.toSubjectDO()
        val subjectStatusDO =
            SubjectStatusDO {
                studySiteId = studySiteId1
                subjectId = subject.id
                status = SubjectStatus.ACTIVE
            }
        val studySiteDO =
            StudySiteDO {
                siteId = subject.siteId
                studyId = subject.studyId
            }
        every {
            subjectDAO.getFullSubjectBySubjectNumberAndSiteIdAndStudyId("subjectNumber", "siteId", "studyId")
        } returns Triple(subjectDO, subjectStatusDO, studySiteDO)

        val res = subjectService.getSubjectBySubjectNumberAndSiteIdAndStudyId("subjectNumber", "siteId", "studyId")
        assertEquals(res, expected)
    }

    @Test
    fun `create subject with subject number happy path`() {
        studySiteDO["studySiteId"] = studySiteId
        every {
            subjectDAO.getFullSubjectBySubjectNumberAndSiteIdAndStudyId("001-001", "siteId", "studyId")
        } returns null

        every {
            clinicalOneClient.validateSubjectNumber(subject)
        } returns subject

        every {
            subjectService.getStudySiteByStudyIdAndSiteId(any(), any())
        } returns studySiteDO

        every {
            subjectDAO.insertSubject(subject.toSubjectDO())
        } returns ""
        every {
            subjectStatusDAO.insertSubjectStatus(any())
        } returns Pair(studySiteId, "")
        val res = subjectService.createSubjectWithSubjectNumber(subject)
        assertEquals(res, subject)
    }

    @Test
    fun `create subject with subject number with subject number already used in db with matching fhir id`() {
        val subjectDO = subject.toSubjectDO()
        val subjectStatusDO =
            SubjectStatusDO {
                studySiteId = studySiteId1
                subjectId = subject.id
                status = SubjectStatus.ACTIVE
            }
        val studySiteDO =
            StudySiteDO {
                siteId = subject.siteId
                studyId = subject.studyId
            }
        every {
            subjectDAO.getFullSubjectBySubjectNumberAndSiteIdAndStudyId("001-001", "siteId", "studyId")
        } returns Triple(subjectDO, subjectStatusDO, studySiteDO)

        every {
            clinicalOneClient.validateSubjectNumber(subject)
        } returns subject

        val exception =
            assertThrows<IllegalArgumentException> {
                subjectService.createSubjectWithSubjectNumber(subject)
            }
        assertEquals(exception.message, "Subject is already bound with this subject number")
    }

    @Test
    fun `create subject with subject number with subject number already used in db with no matching fhir id`() {
        val expected = subject
        val subjectDO = subject.toSubjectDO()
        subjectDO.roninPatientId = "wrongFhirId"
        val subjectStatusDO =
            SubjectStatusDO {
                studySiteId = studySiteId1
                subjectId = subject.id
                status = SubjectStatus.ACTIVE
            }
        val studySiteDO =
            StudySiteDO {
                siteId = subject.siteId
                studyId = subject.studyId
            }
        every {
            subjectDAO.getFullSubjectBySubjectNumberAndSiteIdAndStudyId("001-001", "siteId", "studyId")
        } returns Triple(subjectDO, subjectStatusDO, studySiteDO)

        every {
            clinicalOneClient.validateSubjectNumber(subject)
        } returns subject

        val exception =
            assertThrows<IllegalArgumentException> {
                subjectService.createSubjectWithSubjectNumber(subject)
            }
        assertEquals(exception.message, "Subject number currently bound to different patient")
    }

    @Test
    fun `create subject with subject number with subject number not found in clinical one`() {
        every {
            subjectDAO.getFullSubjectBySubjectNumberAndSiteIdAndStudyId("001-001", "siteId", "studyId")
        } returns null

        every {
            clinicalOneClient.validateSubjectNumber(subject)
        } returns null

        val exception =
            assertThrows<IllegalArgumentException> {
                subjectService.createSubjectWithSubjectNumber(subject)
            }
        assertEquals(exception.message, "Subject with given subject number not found in Clinical One Trial")
    }

    // Utility
    private fun Subject.toSubjectDO(): SubjectDO {
        return SubjectDO {
            subjectId = this@toSubjectDO.id
            roninPatientId = this@toSubjectDO.roninFhirId
            subjectNumber = this@toSubjectDO.number
        }
    }
}
