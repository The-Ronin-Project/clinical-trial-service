package com.projectronin.clinical.trial.server.kafka

import com.projectronin.clinical.trial.server.dataauthority.ObservationDAO
import com.projectronin.clinical.trial.server.services.SubjectService
import com.projectronin.clinical.trial.server.transform.RCDMObservationToCTDMObservation
import com.projectronin.clinical.trial.server.transform.RCDMPatientToCTDMObservations
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
    private val observationDAO: ObservationDAO
) {

    private val tenants = listOf("ronin", "ronincer", "ggwadc8y") // TODO: swap this with a call to the tenant service

    @KafkaListener(topics = ["oci.us-phoenix-1.ehr-data-authority.observation.v1"], groupId = "clinical-trial-service")
    fun consumeObservation(message: RoninEvent<Observation>) {
        val observation = message.data
        observation.subject?.decomposedId()?.let { patientFhirId ->
            if (checkTenantAndPatient(patientFhirId)) {
                val ctdmObservation = observationTransformer.rcdmObservationToCTDMObservation(patientFhirId, observation)
                if (ctdmObservation != null) {
                    KotlinLogging.logger { }.info { "Observation ${observation.id?.value} added" }
                    observationDAO.insert(ctdmObservation)
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
                observationDAO.insert(it)
                KotlinLogging.logger { }.info { "Observation ${it.id?.value} added" }
            }
        }
    }

    private fun checkTenantAndPatient(patientFHIRID: String?): Boolean {
        if (patientFHIRID?.split("-")?.first() !in tenants) return false
        val activePatients = subjectService.getActiveFhirIds()
        return patientFHIRID in activePatients
    }
}
