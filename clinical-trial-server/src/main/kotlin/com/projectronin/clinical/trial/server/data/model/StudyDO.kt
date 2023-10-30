package com.projectronin.clinical.trial.server.data.model

import org.ktorm.entity.Entity

interface StudyDO : Entity<StudyDO> {
    companion object : Entity.Factory<StudyDO>()

    var studyId: String
}
