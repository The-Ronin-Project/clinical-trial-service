package com.projectronin.clinical.trial.server.data

import com.github.database.rider.core.api.connection.ConnectionHolder
import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.core.api.dataset.ExpectedDataSet
import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.clinical.trial.server.data.model.SubjectStatusDO
import com.projectronin.interop.common.test.database.dbrider.DBRiderConnection
import com.projectronin.interop.common.test.database.ktorm.KtormHelper
import com.projectronin.interop.common.test.database.liquibase.LiquibaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.SQLIntegrityConstraintViolationException
import java.util.UUID

@LiquibaseTest(changeLog = "clinicaltrial/db/changelog/clinicaltrial.db.changelog-master.yaml")
class SubjectStatusDAOTest {
    @DBRiderConnection
    lateinit var connectionHolder: ConnectionHolder

    private val subjectStatusDAO = SubjectStatusDAO(KtormHelper.database())
    private val subjectDAO = SubjectDAO(KtormHelper.database())

    @Test
    @DataSet(value = ["/dbunit/subjectstatus/NoSubjectStatus.yaml"], cleanAfter = true)
    @ExpectedDataSet(value = ["/dbunit/subjectstatus/OneSubjectStatus.yaml"], ignoreCols = ["created_datetime", "subject_number"])
    fun `insert Subject Status`() {
        subjectStatusDAO.insertSubjectStatus(
            SubjectStatusDO {
                studySiteId = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770")
                subjectId = "subjectId"
                status = SubjectStatus.ACTIVE
            },
        )
    }

    @DataSet(value = ["/dbunit/subjectstatus/OneSubjectStatus.yaml"], cleanAfter = true)
    @ExpectedDataSet(value = ["/dbunit/subjectstatus/UpdatedSubjectStatus.yaml"], ignoreCols = ["created_datetime", "updated_datetime"])
    @Test
    fun `update Subject Status`() {
        val studysite = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770")
        val updated =
            subjectStatusDAO.updateSubjectStatus(studysite, "subjectId") {
                it.status = SubjectStatus.WITHDRAWN
            }
        assertEquals(updated!!.studySiteId, studysite)
        assertEquals(updated.status, SubjectStatus.WITHDRAWN)
    }

    @DataSet(value = ["/dbunit/subjectstatus/MultipleSubjectStatuses.yaml"], cleanAfter = true)
    @Test
    fun `get all Subject Status`() {
        val subjectStatus = subjectStatusDAO.getSubjectStatus()
        assertEquals(subjectStatus.size, 10)
    }

    @DataSet(value = ["/dbunit/subjectstatus/MultipleSubjectStatuses.yaml"], cleanAfter = true)
    @Test
    fun `get Subject Status by Subject ID`() {
        val subjectStatus = subjectStatusDAO.getSubjectStatusBySubjectId("subjectId6")
        assertEquals(subjectStatus.size, 2)
        assertEquals(
            listOf(
                UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770"),
                UUID.fromString("9691d550-90f5-4fb8-83f9-e4a3840e37eb"),
            ),
            subjectStatus.map {
                it.studySiteId
            },
        )
    }

    @DataSet(value = ["/dbunit/subjectstatus/MultipleSubjectStatuses.yaml"], cleanAfter = true)
    @Test
    fun `get Subject Status by Status`() {
        val status = listOf(SubjectStatus.ACTIVE, SubjectStatus.NEW, SubjectStatus.ENROLLED)
        val subjectStatus = subjectStatusDAO.getSubjectsByStatus(status)
        assertEquals(subjectStatus.size, 7)
    }

    @DataSet(value = ["/dbunit/subjectstatus/MultipleSubjectStatuses.yaml"], cleanAfter = true)
    @Test
    fun `get Subject Status by study site and status`() {
        val status = listOf(SubjectStatus.ACTIVE, SubjectStatus.NEW, SubjectStatus.ENROLLED)
        val subjectStatus =
            subjectStatusDAO.getSubjectStatusByStudySiteAndStatus(
                UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770"),
                status,
            )
        assertEquals(subjectStatus.size, 3)
    }

    @Test
    @DataSet(value = ["/dbunit/subjectstatus/OneSubjectStatus.yaml"], cleanAfter = true)
    fun `handles failed insert`() {
        assertThrows<SQLIntegrityConstraintViolationException> {
            subjectStatusDAO.insertSubjectStatus(
                SubjectStatusDO {
                    studySiteId = UUID.fromString("5f781c30-02f3-4f06-adcf-7055bcbc5770")
                    subjectId = "subjectId"
                    status = SubjectStatus.NEW
                },
            )
        }
    }
}
