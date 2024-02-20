package com.projectronin.clinical.trial.server.transform

import com.projectronin.clinical.trial.server.data.SubjectDAO
import com.projectronin.interop.fhir.r4.resource.Condition
import org.springframework.stereotype.Component

@Component
class RCDMConditionToCTDMCondition(
    private val subjectDAO: SubjectDAO,
    private val dataDictionaryService: DataDictionaryService,
    private val rcdmHelper: BaseRCDMToCTDMHelper,
) {
    fun rcdmConditionToCTDMCondition(
        fhirPatientId: String,
        rcdmCondition: Condition,
    ): Condition? {
        return null
    }
}
