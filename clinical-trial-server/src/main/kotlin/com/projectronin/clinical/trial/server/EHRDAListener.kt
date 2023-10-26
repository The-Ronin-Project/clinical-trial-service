package com.projectronin.clinical.trial.server

import com.projectronin.interop.fhir.r4.resource.Observation
import com.projectronin.interop.fhir.r4.resource.Patient
import com.projectronin.kafka.data.RoninEvent
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class EHRDAListener(private val activePatientService: ActivePatientService) {

    @KafkaListener(topics = ["oci.us-phoenix-1.ehr-data-authority.observation.v1"], groupId = "clinical-trial-service")
    fun consumeObservation(message: RoninEvent<Observation>) {
        val observation = message.data
        KotlinLogging.logger { }.warn { "Got Observation" }
        if (activePatientService.isActivePatient(observation.subject?.decomposedId())) {
            KotlinLogging.logger { }.warn { "Active observation" }
        }
    }

    @KafkaListener(topics = ["oci.us-phoenix-1.ehr-data-authority.patient.v1"], groupId = "clinical-trial-service")
    fun consumePatient(message: RoninEvent<Patient>) {
        val patient = message.data
        KotlinLogging.logger { }.error { "Got Patient" }
        if (activePatientService.isActivePatient(patient.id?.value)) {
            KotlinLogging.logger { }.warn { "Active patient" }
        }
    }
}

@Service
class ActivePatientService() { // inject subject DAO

    private val activePatients = mutableSetOf<String>()

    @PostConstruct
    fun initialize() {
        // TODO: call subject DAO for current list of active patients
        addActivePatient("InitTestPatient")
    }

    @Synchronized
    fun addActivePatient(patientID: String) {
        activePatients.add(patientID)
    }

    @Synchronized
    fun removeActivePatient(patientID: String) {
        activePatients.remove(patientID)
    }

    @Synchronized
    fun isActivePatient(patientID: String?): Boolean {
        return activePatients.contains(patientID)
    }

    @Synchronized
    fun getActivePatients(): List<String> {
        return activePatients.toList()
    }
}
