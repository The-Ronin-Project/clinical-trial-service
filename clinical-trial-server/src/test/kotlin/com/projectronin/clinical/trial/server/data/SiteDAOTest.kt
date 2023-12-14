package com.projectronin.clinical.trial.server.data

import com.github.database.rider.core.api.connection.ConnectionHolder
import com.github.database.rider.core.api.dataset.DataSet
import com.github.database.rider.core.api.dataset.ExpectedDataSet
import com.projectronin.clinical.trial.server.data.model.SiteDO
import com.projectronin.interop.common.test.database.dbrider.DBRiderConnection
import com.projectronin.interop.common.test.database.ktorm.KtormHelper
import com.projectronin.interop.common.test.database.liquibase.LiquibaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@LiquibaseTest(changeLog = "clinicaltrial/db/changelog/clinicaltrial.db.changelog-master.yaml")
class SiteDAOTest {
    @DBRiderConnection
    lateinit var connectionHolder: ConnectionHolder

    private val siteDAO = SiteDAO(KtormHelper.database())

    @Test
    @DataSet(value = ["/dbunit/site/NoSites.yaml"], cleanAfter = true)
    @ExpectedDataSet(value = ["/dbunit/site/OneSite.yaml"])
    fun `insert site`() {
        siteDAO.insertSite(
            SiteDO {
                siteId = "siteId"
                roninTenantMnemonic = "ronincer"
            },
        )
    }

    @Test
    @DataSet(value = ["/dbunit/site/MultipleSites.yaml"], cleanAfter = true)
    fun `get all sites`() {
        val sites = siteDAO.getSites()
        assertEquals(sites.size, 3)
    }

    @Test
    @DataSet(value = ["/dbunit/site/MultipleSites.yaml"], cleanAfter = true)
    fun `get one site`() {
        val site = siteDAO.getSite("siteId2")
        assertEquals(site.roninTenantMnemonic, "mda")
    }
}
