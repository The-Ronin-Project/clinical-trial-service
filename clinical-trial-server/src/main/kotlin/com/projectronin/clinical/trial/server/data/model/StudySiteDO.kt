package com.projectronin.clinical.trial.server.data.model

import org.ktorm.entity.Entity
import java.util.UUID

interface StudySiteDO : Entity<StudySiteDO> {
    companion object : Entity.Factory<StudySiteDO>()

    val studySiteId: UUID
    var studyId: String
    var siteId: String
}
