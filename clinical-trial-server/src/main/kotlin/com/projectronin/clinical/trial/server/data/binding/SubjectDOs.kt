package com.projectronin.clinical.trial.server.data.binding

import com.projectronin.clinical.trial.server.data.model.SubjectDO
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

object SubjectDOs : Table<SubjectDO>("subject") {
    val subjectId = varchar("subject_id").primaryKey().bindTo { it.subjectId }
    val roninPatientId = varchar("ronin_patient_id").bindTo { it.roninPatientId }
    val subjectNumber = varchar("subject_number").bindTo { it.subjectNumber }
}
