package com.projectronin.clinical.trial.server.data

import com.projectronin.clinical.trial.server.data.binding.SiteDOs
import com.projectronin.clinical.trial.server.data.binding.StudySiteDOs
import com.projectronin.clinical.trial.server.data.model.StudySiteDO
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.innerJoin
import org.ktorm.dsl.insert
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class StudySiteDAO(private val database: Database) {

    /**
     * Insert a [StudySiteDO], returns generated [UUID]
     */
    fun insertStudySite(studySiteDO: StudySiteDO): UUID {
        val newUUID = UUID.randomUUID()
        database.insert(StudySiteDOs) {
            set(it.studySiteId, newUUID)
            set(it.studyId, studySiteDO.studyId)
            set(it.siteId, studySiteDO.siteId)
        }
        return newUUID
    }

    /**
     * Retrieves list of [StudySiteDO]s
     */
    fun getStudySites(): List<StudySiteDO> {
        return database.from(StudySiteDOs).select().map { StudySiteDOs.createEntity(it) }
    }

    /**
     * Retrieves one [StudySiteDO] based on studySite UUID String
     */

    fun getStudySite(studySiteId: UUID): StudySiteDO? {
        return database.from(StudySiteDOs).select()
            .where(StudySiteDOs.studySiteId eq studySiteId)
            .map { StudySiteDOs.createEntity(it) }.firstOrNull()
    }

    /**
     * Retrieves one [StudySiteDO] based on study and site Ids
     */

    fun getStudySiteByStudyIdAndSiteId(studyId: String, siteId: String): StudySiteDO? {
        return database.from(StudySiteDOs)
            .select()
            .where(StudySiteDOs.siteId eq siteId)
            .where(StudySiteDOs.studyId eq studyId)
            .map { StudySiteDOs.createEntity(it) }.firstOrNull()
    }

    /**
     * Retrieves list of [StudySiteDO]s by Ronin Tenant Mnemonic
     */
    fun getStudySitesByTenant(tenant: String): List<StudySiteDO> {
        return database.from(StudySiteDOs)
            .innerJoin(SiteDOs, SiteDOs.siteId eq StudySiteDOs.siteId)
            .select()
            .where(SiteDOs.roninTenantMnemonic eq tenant)
            .map { StudySiteDOs.createEntity(it) }
    }
}
