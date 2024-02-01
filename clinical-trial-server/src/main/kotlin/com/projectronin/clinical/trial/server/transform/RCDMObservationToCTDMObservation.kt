package com.projectronin.clinical.trial.server.transform

import com.projectronin.clinical.trial.server.data.SubjectDAO
import com.projectronin.interop.fhir.r4.datatype.Reference
import com.projectronin.interop.fhir.r4.resource.Observation
import org.springframework.stereotype.Component

@Component
class RCDMObservationToCTDMObservation(
    private val subjectDAO: SubjectDAO,
    private val dataDictionaryService: DataDictionaryService,
    private val rcdmHelper: BaseRCDMToCTDMHelper,
) {
    fun rcdmObservationToCTDMObservation(
        fhirPatientId: String,
        rcdmObservation: Observation,
    ): Observation? {
        // only transform observations if the subject reference is a patient.
        if (rcdmObservation.subject?.isForType("Patient") == true) {
            // get data dictionary value for observation code
            // rcdm validation requires valid code objects, so these should be populated and not null
            val obsCodeSystem = rcdmObservation.code!!.coding[0].system!!.value!!
            val obsCodeValue = rcdmObservation.code!!.coding[0].code!!.value!!
            // EHRDAListener checks for valid observation code and system values
            val dataDictionaryRow =
                dataDictionaryService.getDataDictionaryByCode(obsCodeSystem, obsCodeValue)!!
            val ctdmSubject =
                subjectDAO.getSubjectByFhirId(fhirPatientId)
                    ?: throw Exception("No subject found for Ronin FHIR Id: $fhirPatientId")

            return rcdmObservation.copy(
                meta = dataDictionaryRow.get(0).let { rcdmHelper.setProfileMeta(it.valueSetDisplayTitle) },
                implicitRules = null,
                language = null,
                text = null,
                contained = listOf(),
                extension = setCTDMExtensions(ctdmSubject),
                modifierExtension = listOf(),
                subject =
                    Reference(
                        reference = rcdmObservation.subject!!.reference,
                    ),
                identifier = listOf(),
                basedOn = listOf(),
                partOf = listOf(),
                focus = listOf(),
                encounter = null,
                performer = listOf(),
                hasMember = listOf(),
                derivedFrom = listOf(),
            )
        } else {
            return null
        }
    }
}
