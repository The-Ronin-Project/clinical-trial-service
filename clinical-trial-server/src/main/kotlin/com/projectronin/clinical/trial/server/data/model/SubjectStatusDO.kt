package com.projectronin.clinical.trial.server.data.model

import org.ktorm.entity.Entity
import java.time.OffsetDateTime
import java.util.UUID

interface SubjectStatusDO : Entity<SubjectStatusDO> {
    companion object : Entity.Factory<SubjectStatusDO>()

    var studySiteId: UUID
    var subjectId: String
    var status: SubjectStatus
    val createdDateTime: OffsetDateTime
    var updatedDateTime: OffsetDateTime?
}
