package com.projectronin.clinical.trial.server.data

import com.projectronin.clinical.trial.server.data.binding.StudyDOs
import com.projectronin.clinical.trial.server.data.model.StudyDO
import org.ktorm.database.Database
import org.ktorm.dsl.insert
import org.springframework.stereotype.Repository

@Repository
class StudyDAO(private val database: Database) {
    /**
     * Inserts [StudyDO]
     */
    fun insertStudy(studyDO: StudyDO): String {
        database.insert(StudyDOs) {
            set(it.studyId, studyDO.studyId)
        }
        return studyDO.studyId
    }
}
