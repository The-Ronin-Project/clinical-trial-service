package com.projectronin.clinical.trial.server.dataauthority

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
class ObservationDAO(
    private val resourceDatabase: ClinicalTrialDataAuthorityDatabase,
) : BaseCollectionDAO<Observation>(
        resourceDatabase,
        Observation::class.java,
    ) {
    fun search(
        subjectId: String? = null,
        valueSetIds: List<String>? = null,
        fromDate: ZonedDateTime? = null,
        toDate: ZonedDateTime? = null,
    ): List<Observation> {
        val list = mutableListOf(listOf<Observation>())

        if (subjectId == null && valueSetIds.isNullOrEmpty()) {
            list.add(
                resourceDatabase.run(collection) {
                    find().execute().map {
                        JacksonUtil.readJsonObject(it.toString(), Observation::class)
                    }
                },
            )
        } else {
            val querySet = chunkQuery(subjectId, valueSetIds)
            querySet.forEach { query ->
                list.add(
                    resourceDatabase.run(collection) {
                        find(query).execute().map {
                            JacksonUtil.readJsonObject(it.toString(), Observation::class)
                        }
                    },
                )
            }
        }

        // Filter on fromDate and toDate post-query due to XDev API
        return list.flatten().filter { observation ->
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

    private fun chunkQuery(
        subjectId: String? = null,
        valueSetIds: List<String>? = null,
    ): List<String> {
        val querySet = mutableListOf<String>()

        @Suppress("ktlint:standard:max-line-length")
        val subjectFragment =
            subjectId?.let {
                "JSON_CONTAINS(extension, '[{\"url\": \"${ExtensionUrls.SUBJECT_ID_URL}\", \"valueString\": \"$it\"}]')"
            }
        valueSetIds?.let {
            val valueSetChunky = valueSetIds.chunked(50)
            valueSetChunky.forEach { valueSet ->
                @Suppress("ktlint:standard:max-line-length")
                val query = (subjectFragment?.let { "$it AND " } ?: "") + "( meta.tag[0].system in" + valueSet.joinToString("','", "('", "') )")
                querySet.add(query)
            }
            // subjectId and valueSetIds can't both be respectively null or empty, so if valueSetIds is empty then the subject query must be set
        } ?: querySet.add(subjectFragment!!)

        return querySet
    }
}
