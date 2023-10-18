package com.projectronin.clinical.trial.server.dataauthority

import com.projectronin.interop.common.jackson.JacksonUtil
import com.projectronin.interop.fhir.r4.resource.Patient
import org.springframework.stereotype.Component

@Component
class PatientDAO(private val resourceDatabase: ClinicalTrialDataAuthorityDatabase) {

    val collection = resourceDatabase.createCollection(Patient::class.java)

    fun getAll(): List<Patient> {
        val list = mutableListOf<Patient>()

        resourceDatabase.run(collection) {
            find().execute().forEach {
                list.add(JacksonUtil.readJsonObject(it.toString(), Patient::class))
            }
        }
        return list
    }
}
