package com.projectronin.clinical.trial.server.data.binding

import com.projectronin.clinical.trial.server.data.model.SponsorDO
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

object SponsorDOs : Table<SponsorDO>("sponsor") {
    val sponsorId = varchar("sponsor_id").primaryKey().bindTo { it.sponsorId }
    val clinicaloneTenant = varchar("clinicalone_tenant").bindTo { it.clinicaloneTenant }
}
