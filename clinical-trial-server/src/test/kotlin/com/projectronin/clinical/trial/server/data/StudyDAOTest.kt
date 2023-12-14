package com.projectronin.clinical.trial.server.data

import com.github.database.rider.core.api.connection.ConnectionHolder
import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.core.api.dataset.ExpectedDataSet
import com.projectronin.clinical.trial.server.data.model.StudyDO
import com.projectronin.interop.common.test.database.dbrider.DBRiderConnection
import com.projectronin.interop.common.test.database.ktorm.KtormHelper
import com.projectronin.interop.common.test.database.liquibase.LiquibaseTest
import org.junit.jupiter.api.Test

@LiquibaseTest(changeLog = "clinicaltrial/db/changelog/clinicaltrial.db.changelog-master.yaml")
class StudyDAOTest {
    @DBRiderConnection
    lateinit var connectionHolder: ConnectionHolder

    private val studyDAO = StudyDAO(KtormHelper.database())

    @Test
    @DataSet(value = ["/dbunit/study/NoStudies.yaml"], cleanAfter = true)
    @ExpectedDataSet(value = ["/dbunit/study/OneStudy.yaml"])
    fun `insert`() {
        studyDAO.insertStudy(
            StudyDO {
                studyId = "studyId1"
            },
        )
    }
}
