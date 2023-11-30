package com.projectronin.clinical.trial.server.dataauthority

import com.mysql.cj.xdevapi.DbDoc
import com.mysql.cj.xdevapi.JsonString
import com.projectronin.clinical.trial.server.util.getEffectiveDateTime
import com.projectronin.clinical.trial.server.util.getEffectivePeriod
import com.projectronin.interop.common.jackson.JacksonUtil
import com.projectronin.interop.fhir.r4.datatype.DynamicValueType
import com.projectronin.interop.fhir.r4.datatype.Period
import com.projectronin.interop.fhir.r4.datatype.primitive.DateTime
import com.projectronin.interop.fhir.r4.resource.Observation
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class ObservationDAO(private val resourceDatabase: ClinicalTrialDataAuthorityDatabase) {

    val collection = resourceDatabase.createCollection(Observation::class.java)

    fun findByIdQuery(fhirId: String): DbDoc? {
        return resourceDatabase.run(collection) {
            find("id = :id")
                .bind("id", fhirId.removePrefix("Observation/"))
                .execute()
                .fetchOne()
        }
    }

    private fun getDatabaseId(fhirId: String): String? {
        // "_id" is the MySQL-specific document identifier, which is different from the FHIR "id"
        return findByIdQuery(fhirId)?.let { (it["_id"] as JsonString).string }
    }

    fun findById(fhirId: String): Observation? {
        return findByIdQuery(fhirId)?.let {
            JacksonUtil.readJsonObject(it.toString(), Observation::class)
        }
    }

    fun getAll(): List<Observation> {
        return resourceDatabase.run(collection) {
            find().execute().map {
                JacksonUtil.readJsonObject(it.toString(), Observation::class)
            }
        }
    }

    fun insert(observation: Observation): String {
        resourceDatabase.run(collection) {
            add(JacksonUtil.writeJsonValue(observation)).execute()
        }
        return observation.id?.value.toString()
    }

    fun update(observation: Observation) {
        getDatabaseId(observation.id?.value.toString())?.let {
            resourceDatabase.run(collection) {
                replaceOne(
                    it,
                    JacksonUtil.writeJsonValue(observation)
                )
            }
        } ?: insert(observation) // add new resource if not found
    }

    fun delete(fhirId: String) {
        getDatabaseId(fhirId)?.let { resourceDatabase.run(collection) { removeOne(it) } }
    }

    fun search(
        subject: String? = null,
        valueSetIds: List<String>? = null,
        fromDate: ZonedDateTime? = null,
        toDate: ZonedDateTime? = null
    ): List<Observation> {
        val queryFragments = mutableListOf<String>()
        subject?.let { queryFragments.add("('$it' = subject.reference)") }
        valueSetIds?.joinToString(" OR ") { ("('$it' in meta.tag[*].system)") }?.let { queryFragments.add("( $it )") }

        val query = queryFragments.joinToString(" AND ")

        val list = resourceDatabase.run(collection) {
            find(query).execute().map {
                JacksonUtil.readJsonObject(it.toString(), Observation::class)
            }
        }

        // Filter on fromDate and toDate post-query due to XDev API
        return list.filter { observation ->
            when (observation.effective?.type) {
                DynamicValueType.DATE_TIME -> {
                    val date = (observation.effective!!.value as DateTime).getEffectiveDateTime()
                    (toDate?.let { date?.let { date -> date.isBefore(it) || date == it } } ?: true) &&
                        (fromDate?.let { date?.let { date -> date.isAfter(it) || date == it } } ?: true)
                }
                DynamicValueType.PERIOD -> {
                    val period = (observation.effective!!.value as Period).getEffectivePeriod()
                    (toDate?.let { period.second?.let { end -> end.isBefore(it) || end == it } } ?: true) &&
                        (fromDate?.let { period.first?.let { start -> start.isAfter(it) || start == it } } ?: true)
                }
                else -> false
            }
        }
    }
}
