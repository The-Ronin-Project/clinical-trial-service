package com.projectronin.clinical.trial.server.data.binding

import com.projectronin.clinical.trial.server.data.model.StudyDO
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

object StudyDOs : Table<StudyDO>("study") {
    val studyId = varchar("study_id").primaryKey().bindTo { it.studyId }
}
