package com.projectronin.clinical.trial.server.dataauthority

import com.mysql.cj.xdevapi.DbDoc
import com.mysql.cj.xdevapi.JsonString
import com.projectronin.clinical.trial.server.util.getEffectiveDateTime
import com.projectronin.interop.common.jackson.JacksonUtil
import com.projectronin.interop.fhir.r4.datatype.primitive.DateTime
import com.projectronin.interop.fhir.r4.resource.DomainResource
import com.projectronin.interop.fhir.r4.resource.Resource
import java.time.ZonedDateTime

open class BaseCollectionDAO<T : Resource<T>>(
    private val resourceDatabase: ClinicalTrialDataAuthorityDatabase,
    private val resourceType: Class<T>,
) {
    private val kClassInstance = resourceType.kotlin
    val collection = resourceDatabase.createCollection(resourceType)

    fun getAll(): List<T> =
        resourceDatabase.run(collection) {
            find().execute().map {
                JacksonUtil.readJsonObject(it.toString(), kClassInstance)
            }
        }

    fun findById(fhirId: String): T? =
        findByIdQuery(fhirId)?.let {
            JacksonUtil.readJsonObject(it.toString(), kClassInstance)
        }

    fun findUpdated(sinceDate: ZonedDateTime?): List<T> {
        val updatedResources =
            resourceDatabase.run(collection) {
                find(
                    ":dataUpdateTimestampUrl in extension[*].url",
                ).bind(
                    "dataUpdateTimestampUrl",
                    ExtensionUrls.DATA_UPDATE_TIMESTAMP_URL,
                ).execute().map {
                    JacksonUtil.readJsonObject(it.toString(), kClassInstance)
                }
            }

        sinceDate?.let {
            return updatedResources.filter { resource ->
                val updatedExtension =
                    (resource as DomainResource<*>).extension.first { extension ->
                        extension.url?.value == ExtensionUrls.DATA_UPDATE_TIMESTAMP_URL
                    }
                (updatedExtension.value?.value as DateTime).getEffectiveDateTime()!!.isAfter(it)
            }
        }

        return updatedResources
    }

    fun <T : Resource<T>> insert(resource: T): String {
        resourceDatabase.run(collection) {
            add(JacksonUtil.writeJsonValue(resource)).execute()
        }
        return resource.id?.value.toString()
    }

    fun <T : Resource<T>> update(resource: T) {
        getDatabaseId(resource.id?.value.toString())?.let {
            resourceDatabase.run(collection) {
                replaceOne(
                    it,
                    JacksonUtil.writeJsonValue(
                        (resource as DomainResource<*>).populateDataUpdateTimestampExtension(),
                    ),
                )
            }
        } ?: insert(resource)
    }

    fun delete(fhirId: String) {
        getDatabaseId(fhirId)?.let {
            resourceDatabase.run(collection) {
                removeOne(it)
            }
        }
    }

    private fun findByIdQuery(fhirId: String): DbDoc? =
        resourceDatabase.run(collection) {
            find("id = :id")
                .bind("id", fhirId.removePrefix("${resourceType.simpleName}/"))
                .execute()
                .fetchOne()
        }

    // "_id" is the MySQL-specific document identifier, which is different from the FHIR "id"
    private fun getDatabaseId(fhirId: String): String? =
        findByIdQuery(fhirId)?.let {
            (it["_id"] as JsonString).string
        }
}
