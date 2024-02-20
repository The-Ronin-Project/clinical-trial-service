package com.projectronin.clinical.trial.server.kafka

import com.projectronin.clinical.trial.server.dataauthority.ObservationDAO
import com.projectronin.clinical.trial.server.services.SubjectService
import com.projectronin.clinical.trial.server.transform.DataDictionaryService
import com.projectronin.clinical.trial.server.transform.RCDMConditionToCTDMCondition
import com.projectronin.clinical.trial.server.transform.RCDMMedicationRequestToCTDMMedicationRequest
import com.projectronin.clinical.trial.server.transform.RCDMObservationToCTDMObservation
import com.projectronin.clinical.trial.server.transform.RCDMPatientToCTDMObservations
import com.projectronin.interop.fhir.r4.resource.Condition
import com.projectronin.interop.fhir.r4.resource.MedicationRequest
import com.projectronin.interop.fhir.r4.resource.Observation
import com.projectronin.interop.fhir.r4.resource.Patient
import com.projectronin.kafka.data.RoninEvent
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class EHRDAListener(
    private val subjectService: SubjectService,
    private val patientTransformer: RCDMPatientToCTDMObservations,
    private val observationTransformer: RCDMObservationToCTDMObservation,
    private val medicationRequestTransformer: RCDMMedicationRequestToCTDMMedicationRequest,
    private val conditionTransformer: RCDMConditionToCTDMCondition,
    private val observationDAO: ObservationDAO,
    private val dataDictionaryService: DataDictionaryService,
) {
    private val tenants = listOf("ronin", "ronincer", "ggwadc8y") // TODO: swap this with a call to the tenant service

    @KafkaListener(topics = ["oci.us-phoenix-1.ehr-data-authority.condition.v1"], groupId = "clinical-trial-service")
    fun consumeCondition(message: RoninEvent<Condition>) {
        val condition = message.data
        condition.subject?.decomposedId()?.let { patientFhirId ->
            if (checkTenantAndPatient(patientFhirId)) {
                val ctdmCondition =
                    conditionTransformer.rcdmConditionToCTDMCondition(
                        patientFhirId,
                        condition,
                    )
                if (ctdmCondition != null) {
                    KotlinLogging.logger { }.info { "Condition ${condition.id?.value} added" }
                } else {
                    KotlinLogging.logger { }.warn { "Condition ${condition.id?.value} not applicable" }
                }
            }
        }
    }

    @KafkaListener(topics = ["oci.us-phoenix-1.ehr-data-authority.observation.v1"], groupId = "clinical-trial-service")
    fun consumeObservation(message: RoninEvent<Observation>) {
        val observation = message.data
        observation.subject?.decomposedId()?.let { patientFhirId ->
            if (checkObservations(patientFhirId, observation)) {
                val ctdmObservation =
                    observationTransformer.rcdmObservationToCTDMObservation(patientFhirId, observation)
                if (ctdmObservation != null) {
                    KotlinLogging.logger { }.info { "Observation ${observation.id?.value} added" }
                    observationDAO.update(ctdmObservation)
                } else {
                    KotlinLogging.logger { }.warn { "Observation ${observation.id?.value} not applicable" }
                }
            }
        }
    }

    @KafkaListener(topics = ["oci.us-phoenix-1.ehr-data-authority.patient.v1"], groupId = "clinical-trial-service")
    fun consumePatient(message: RoninEvent<Patient>) {
        val patient = message.data
        if (checkTenantAndPatient(patient.id?.value)) {
            val demographicObservations = patientTransformer.splitPatientDemographics(patient)
            demographicObservations.forEach {
                observationDAO.update(it)
                KotlinLogging.logger { }.info { "Observation ${it.id?.value} added" }
            }
        }
    }

    @KafkaListener(topics = ["oci.us-phoenix-1.ehr-data-authority.medication-request.v1"], groupId = "clinical-trial-service")
    fun consumeMedicationRequest(message: RoninEvent<MedicationRequest>) {
        val medRequest = message.data
        medRequest.subject?.decomposedId()?.let { patientFhirId ->
            if (checkTenantAndPatient(patientFhirId)) {
                val ctdmMedicationRequest =
                    medicationRequestTransformer.rcdmMedicationRequestToCTDMMedicationRequest(
                        patientFhirId,
                        medRequest,
                    )
                if (ctdmMedicationRequest != null) {
                    KotlinLogging.logger { }.info { "MedicationRequest ${ctdmMedicationRequest.id?.value} added" }
                    // TODO: medicationRequestDAO.update(ctdmMedicationRequest)
                } else {
                    KotlinLogging.logger { }.warn { "MedicationRequest ${medRequest.id?.value} not applicable" }
                }
            }
        }
    }

    private fun checkObservations(
        patientFHIRID: String?,
        observation: Observation,
    ): Boolean {
        val obsCode = observation.code?.coding?.get(0)?.code?.value
        val obsSystem = observation.code?.coding?.get(0)?.system?.value
        val validId = checkTenantAndPatient(patientFHIRID)
        val validDDCode: Boolean =
            if (listOf(obsSystem, obsCode).any { it == null }) {
                false
            } else {
                // if statement above checks if either observation code or system are null, so both should not be null
                dataDictionaryService.getDataDictionaryByCode(obsSystem!!, obsCode!!) ?: false
                true
            }
        if (!validId) {
            KotlinLogging.logger { }.warn { "Invalid $patientFHIRID" }
        }
        return validId && validDDCode
    }

    private fun checkTenantAndPatient(patientFHIRID: String?): Boolean {
        if (patientFHIRID?.split("-")?.first() !in tenants) return false
        val activePatients = subjectService.getActiveFhirIds()
        return patientFHIRID in activePatients
    }
}
