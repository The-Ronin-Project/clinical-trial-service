package com.projectronin.clinical.trial.server.kafka

import com.projectronin.clinical.trial.server.services.SubjectService
import com.projectronin.interop.fhir.r4.resource.Observation
import com.projectronin.interop.fhir.r4.resource.Patient
import com.projectronin.kafka.data.RoninEvent
import mu.KotlinLogging
import org.springframework.context.annotation.DependsOn
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
@DependsOn("liquibase")
class ActivePatientService(
    val subjectService: SubjectService
) {
    private val activePatients = mutableSetOf<String>()

    @PostConstruct
    fun initialize() {
        subjectService.getActiveFhirIds().forEach(this::addActivePatient)
    }

    @Synchronized
    fun addActivePatient(patientRoninFhirId: String) {
        activePatients.add(patientRoninFhirId)
    }

    @Synchronized
    fun removeActivePatient(patientRoninFhirId: String) {
        activePatients.remove(patientRoninFhirId)
    }

    @Synchronized
    fun isActivePatient(patientRoninFhirId: String?): Boolean = activePatients.contains(patientRoninFhirId)

    @Synchronized
    fun getActivePatients(): List<String> = activePatients.toList()
}
