package com.projectronin.clinical.trial.server.data

import com.github.database.rider.core.api.connection.ConnectionHolder
import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.core.api.dataset.ExpectedDataSet
import com.projectronin.clinical.trial.server.data.model.StudySiteDO
import com.projectronin.interop.common.test.database.dbrider.DBRiderConnection
import com.projectronin.interop.common.test.database.ktorm.KtormHelper
import com.projectronin.interop.common.test.database.liquibase.LiquibaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@LiquibaseTest(changeLog = "clinicaltrial/db/changelog/clinicaltrial.db.changelog-master.yaml")
class StudySiteDAOTest {
    @DBRiderConnection
    lateinit var connectionHolder: ConnectionHolder

    private val studySiteDAO = StudySiteDAO(KtormHelper.database())

    @Test
    @DataSet(value = ["/dbunit/studysite/NoStudySite.yaml"], cleanAfter = true)
    @ExpectedDataSet(value = ["/dbunit/studysite/OneStudySite.yaml"], ignoreCols = ["study_site_id"])
    fun `insert StudySite`() {
        studySiteDAO.insertStudySite(
            StudySiteDO {
                studyId = "studyId"
                siteId = "siteId"
            },
        )
    }

    @Test
    @DataSet(value = ["/dbunit/studysite/MultipleStudySites.yaml"], cleanAfter = true)
    fun `get all StudySites`() {
        val studysites = studySiteDAO.getStudySites()
        assertEquals(studysites.size, 4)
    }

    @Test
    @DataSet(value = ["/dbunit/studysite/MultipleStudySites.yaml"], cleanAfter = true)
    fun `get all StudySites by tenant`() {
        val studysites = studySiteDAO.getStudySitesByTenant("psj")
        assertEquals(studysites.size, 2)
    }

    @Test
    @DataSet(value = ["/dbunit/studysite/MultipleStudySites.yaml"], cleanAfter = true)
    fun `get StudySite by Site ID and Study ID`() {
        val studySite = studySiteDAO.getStudySiteByStudyIdAndSiteId("studyId1", "siteId1")
        assertNotNull(studySite)

        val noStudySite = studySiteDAO.getStudySiteByStudyIdAndSiteId("noStudy", "siteId1")
        assertNull(noStudySite)
    }
}
