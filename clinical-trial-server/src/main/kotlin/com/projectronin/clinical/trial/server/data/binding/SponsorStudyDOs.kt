package com.projectronin.clinical.trial.server.data.binding

import com.projectronin.clinical.trial.server.data.model.SponsorStudyDO
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

object SponsorStudyDOs : Table<SponsorStudyDO>("sponsor_study") {
    val sponsorId = varchar("sponsor_id").primaryKey().bindTo { it.sponsorId }
    val studyId = varchar("study_id").primaryKey().bindTo { it.studyId }
}
