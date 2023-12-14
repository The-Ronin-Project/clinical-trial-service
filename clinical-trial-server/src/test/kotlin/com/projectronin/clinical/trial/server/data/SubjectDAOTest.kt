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
}
