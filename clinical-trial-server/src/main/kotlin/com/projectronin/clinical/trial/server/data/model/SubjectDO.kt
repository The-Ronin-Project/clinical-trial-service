package com.projectronin.clinical.trial.server.data.model

import org.ktorm.entity.Entity

interface SubjectDO : Entity<SubjectDO> {
    companion object : Entity.Factory<SubjectDO>()

    var subjectId: String
    var roninPatientId: String
}
