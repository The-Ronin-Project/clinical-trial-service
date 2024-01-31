package com.projectronin.clinical.trial.server.data

import com.github.database.rider.core.api.connection.ConnectionHolder
import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.core.api.dataset.ExpectedDataSet
import com.projectronin.clinical.trial.server.data.model.SubjectDO
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.interop.common.test.database.dbrider.DBRiderConnection
import com.projectronin.interop.common.test.database.ktorm.KtormHelper
import com.projectronin.interop.common.test.database.liquibase.LiquibaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

@LiquibaseTest(changeLog = "clinicaltrial/db/changelog/clinicaltrial.db.changelog-master.yaml")
class SubjectDAOTest {
    @DBRiderConnection
    lateinit var connectionHolder: ConnectionHolder

    private val subjectDAO = SubjectDAO(KtormHelper.database())

    @Test
    @DataSet(value = ["/dbunit/subject/NoSubjects.yaml"], cleanAfter = true)
    @ExpectedDataSet(value = ["/dbunit/subject/OneSubject.yaml"])
    fun `insert Subject`() {
        subjectDAO.insertSubject(
            SubjectDO {
                subjectId = "subjectId"
                roninPatientId = "roninFhirId"
                subjectNumber = "001-001"
            },
        )
    }

    @Test
    @DataSet(value = ["/dbunit/subject/OneSubject.yaml"], cleanAfter = true)
    fun `get Subjects`() {
        val subject = subjectDAO.getSubjects()
        assertEquals(subject[0].roninPatientId, "roninFhirId")
        assertEquals(subject[0].subjectId, "subjectId")
    }

    @Test
    @DataSet(value = ["/dbunit/subject/OneSubject.yaml"], cleanAfter = true)
    fun `get Subject by FHIR id`() {
        val subjectId = subjectDAO.getSubjectByFhirId("roninFhirId")
        assertEquals(subjectId, "subjectId")
    }

    @Test
    @DataSet(value = ["/dbunit/subject/OneSubject.yaml"], cleanAfter = true)
    fun `get FHIR id by Subject`() {
        val fhirId = subjectDAO.getFhirIdBySubject("subjectId")
        assertEquals(fhirId, "roninFhirId")
    }

    @DataSet(value = ["/dbunit/subjectstatus/MultipleSubjectStatuses.yaml"], cleanAfter = true)
    @Test
    fun `get Subject Ronin FHIR ids by status`() {
        val status = listOf(SubjectStatus.ACTIVE, SubjectStatus.NEW, SubjectStatus.ENROLLED)
        val subjects = subjectDAO.getFhirIdsByStatus(status)
        assertEquals(subjects.size, 6)
        assertEquals(subjects.first(), "roninFhirId1")
        assertEquals(subjects.last(), "roninFhirId7")
    }

    @DataSet(value = ["/dbunit/subjectstatus/OneSubjectStatus.yaml"], cleanAfter = true)
    @Test
    fun `get Subject by Ronin FHIR id`() {
        val subject = subjectDAO.getSubjectByRoninFhirId(fhirId = "roninFhirId")
        assertEquals(subject?.first?.roninPatientId, "roninFhirId")
        assertEquals(subject?.second?.studySiteId, UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770"))
        assertEquals(subject?.third?.studyId, "studyId")
    }

    @DataSet(value = ["/dbunit/subjectstatus/NoSubjectStatus.yaml"], cleanAfter = true)
    @Test
    fun `get Subject by Ronin FHIR id returns empty when unassigned`() {
        val subject = subjectDAO.getSubjectByRoninFhirId(fhirId = "roninFhirId")
        assertEquals(subject, null)
    }

    @DataSet(value = ["/dbunit/subject/OneSubject.yaml"], cleanAfter = true)
    @Test
    fun `get full subject by Ronin FHIR id`() {
        val subject = subjectDAO.getFullSubjectByFhirId("roninFhirId")
        assertEquals("subjectId", subject?.subjectId)
        assertEquals("roninFhirId", subject?.roninPatientId)
        assertEquals("001-001", subject?.subjectNumber)
    }
}
