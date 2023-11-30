package com.projectronin.clinical.trial.server.kafka

import com.projectronin.clinical.trial.server.dataauthority.ObservationDAO
import com.projectronin.clinical.trial.server.services.SubjectService
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
    private val observationDAO: ObservationDAO
) {

    private val tenants = listOf("ronin", "ronincer", "ggwadc8y") // TODO: swap this with a call to the tenant service

    @KafkaListener(topics = ["oci.us-phoenix-1.ehr-data-authority.observation.v1"], groupId = "clinical-trial-service")
    fun consumeObservation(message: RoninEvent<Observation>) {
        val observation = message.data
        if (checkTenantAndPatient(observation.subject?.decomposedId())) {
            KotlinLogging.logger { }.warn { "Active observation" }
        }
    }

    @KafkaListener(topics = ["oci.us-phoenix-1.ehr-data-authority.patient.v1"], groupId = "clinical-trial-service")
    fun consumePatient(message: RoninEvent<Patient>) {
        val patient = message.data
        KotlinLogging.logger { }.warn { "Patient: $patient" }
        if (checkTenantAndPatient(patient.id?.value)) {
            val demographicObservations = patientTransformer.splitPatientDemographics(patient)
            demographicObservations.forEach {
                observationDAO.insert(it)
                KotlinLogging.logger { }.warn { "observation added" }
            }
        }
    }

    private fun checkTenantAndPatient(patientFHIRID: String?): Boolean {
        if (patientFHIRID?.split("-")?.first() !in tenants) return false
        val activePatients = subjectService.getActiveFhirIds()
        KotlinLogging.logger { }.warn { "Active patients: $activePatients" }
        return patientFHIRID in activePatients
    }
}
