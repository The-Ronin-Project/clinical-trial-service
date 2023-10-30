package com.projectronin.clinical.trial.server.data.model

import org.ktorm.entity.Entity

interface SponsorDO : Entity<SponsorDO> {
    companion object : Entity.Factory<SponsorDO>()

    var sponsorId: String
}
