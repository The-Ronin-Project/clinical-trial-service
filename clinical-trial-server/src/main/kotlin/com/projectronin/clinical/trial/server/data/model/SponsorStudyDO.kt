package com.projectronin.clinical.trial.server.data.model

import org.ktorm.entity.Entity

interface SponsorStudyDO : Entity<SponsorStudyDO> {
    companion object : Entity.Factory<SponsorStudyDO>()

    var sponsorId: String
    var studyId: String
}
