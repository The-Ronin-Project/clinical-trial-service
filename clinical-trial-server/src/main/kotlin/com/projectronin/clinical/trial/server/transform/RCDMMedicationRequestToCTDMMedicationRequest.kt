package com.projectronin.clinical.trial.server.transform

import com.projectronin.clinical.trial.server.data.SubjectDAO
import com.projectronin.interop.fhir.r4.resource.MedicationRequest
import org.springframework.stereotype.Component

@Component
class RCDMMedicationRequestToCTDMMedicationRequest(
    private val subjectDAO: SubjectDAO,
    private val dataDictionaryService: DataDictionaryService,
    private val rcdmHelper: BaseRCDMToCTDMHelper,
) {
    fun rcdmMedicationRequestToCTDMMedicationRequest(
        fhirPatientId: String,
        rcdmMedicationRequest: MedicationRequest,
    ): MedicationRequest? {
        return null
    }
}
