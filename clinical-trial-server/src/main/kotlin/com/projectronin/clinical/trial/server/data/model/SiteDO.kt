package com.projectronin.clinical.trial.server.data.model

import org.ktorm.entity.Entity

interface SiteDO : Entity<SiteDO> {
    companion object : Entity.Factory<SiteDO>()

    var siteId: String
    var roninTenantMnemonic: String
}
