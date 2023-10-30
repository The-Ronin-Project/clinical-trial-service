package com.projectronin.clinical.trial.server.data.binding

import com.projectronin.clinical.trial.server.data.model.SubjectStatus
import com.projectronin.clinical.trial.server.data.model.SubjectStatusDO
import com.projectronin.interop.common.ktorm.binding.binaryUuid
import com.projectronin.interop.common.ktorm.binding.utcDateTime
import org.ktorm.schema.Table
import org.ktorm.schema.enum
import org.ktorm.schema.varchar

object SubjectStatusDOs : Table<SubjectStatusDO>("subject_status") {
    val studySiteId = binaryUuid("study_site_id").primaryKey().bindTo { it.studySiteId }
    val subjectId = varchar("subject_id").primaryKey().bindTo { it.subjectId }
    val status = enum<SubjectStatus>("status").bindTo { it.status }
    val createdDateTime = utcDateTime("created_datetime").bindTo { it.createdDateTime }
    val updatedDateTime = utcDateTime("updated_datetime").bindTo { it.updatedDateTime }
}
