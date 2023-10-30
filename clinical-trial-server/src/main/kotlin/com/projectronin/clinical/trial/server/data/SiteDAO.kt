package com.projectronin.clinical.trial.server.data

import com.projectronin.clinical.trial.server.data.binding.SiteDOs
import com.projectronin.clinical.trial.server.data.model.SiteDO
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.springframework.stereotype.Repository

@Repository
class SiteDAO(private val database: Database) {
    /**
     * Retrieve list of [SiteDO]s
     */
    fun getSites(): List<SiteDO> {
        return database.from(SiteDOs).select().map { SiteDOs.createEntity(it) }
    }

    /**
     * Retrieve [SiteDO] based on siteId
     */
    fun getSite(siteId: String): SiteDO {
        return database.from(SiteDOs).select().where(SiteDOs.siteId eq siteId).map { SiteDOs.createEntity(it) }.single()
    }

    /**
     * Insert [SiteDO]
     */
    fun insertSite(siteDO: SiteDO): String {
        database.insert(SiteDOs) {
            set(it.siteId, siteDO.siteId)
            set(it.roninTenantMnemonic, siteDO.roninTenantMnemonic)
        }
        return siteDO.siteId
    }
}
