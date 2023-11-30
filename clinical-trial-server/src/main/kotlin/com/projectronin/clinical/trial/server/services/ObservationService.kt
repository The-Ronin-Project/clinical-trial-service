package com.projectronin.clinical.trial.server.services

import com.projectronin.clinical.trial.server.dataauthority.ObservationDAO
import com.projectronin.interop.fhir.r4.resource.Observation
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class ObservationService(
    val observationDAO: ObservationDAO
) {
    fun getObservations(patientId: String, types: List<String>, fromDate: ZonedDateTime, toDate: ZonedDateTime): List<Observation> {
        return observationDAO.search(patientId, types, fromDate, toDate)
    }

    fun getAllObservationsByPatientId(patientId: String): List<Observation> {
        return observationDAO.search(patientId)
    }
}
