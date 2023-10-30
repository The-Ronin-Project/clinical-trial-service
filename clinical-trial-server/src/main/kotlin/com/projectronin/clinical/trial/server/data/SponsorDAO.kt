package com.projectronin.clinical.trial.server.data

import com.projectronin.clinical.trial.server.data.binding.SponsorDOs
import com.projectronin.clinical.trial.server.data.model.SponsorDO
import org.ktorm.database.Database
import org.ktorm.dsl.insert
import org.springframework.stereotype.Repository

@Repository
class SponsorDAO(private val database: Database) {
    fun insertSponsor(sponsorDO: SponsorDO): String {
        database.insert(SponsorDOs) {
            set(it.sponsorId, sponsorDO.sponsorId)
        }
        return sponsorDO.sponsorId
    }
}
