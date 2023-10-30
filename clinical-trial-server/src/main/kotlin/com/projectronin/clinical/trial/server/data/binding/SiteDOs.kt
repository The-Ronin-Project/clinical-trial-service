package com.projectronin.clinical.trial.server.data.binding

import com.projectronin.clinical.trial.server.data.model.SiteDO
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

object SiteDOs : Table<SiteDO>("site") {
    val siteId = varchar("site_id").primaryKey().bindTo { it.siteId }
    val roninTenantMnemonic = varchar("ronin_tenant_mnemonic").bindTo { it.roninTenantMnemonic }
}
