package com.projectronin.clinical.trial.server.data.binding

import com.projectronin.clinical.trial.server.data.model.StudySiteDO
import com.projectronin.interop.common.ktorm.binding.binaryUuid
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

object StudySiteDOs : Table<StudySiteDO>("study_site") {
    val studySiteId = binaryUuid("study_site_id").primaryKey().bindTo { it.studySiteId }
    val studyId = varchar("study_id").bindTo { it.studyId }
    val siteId = varchar("site_id").bindTo { it.siteId }
}
