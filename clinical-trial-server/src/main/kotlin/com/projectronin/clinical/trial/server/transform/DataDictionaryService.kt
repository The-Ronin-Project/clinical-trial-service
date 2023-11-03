package com.projectronin.clinical.trial.server.transform

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MappingIterator
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.projectronin.interop.common.jackson.JacksonUtil
import com.projectronin.interop.datalake.oci.client.OCIClient
import com.projectronin.interop.fhir.r4.resource.ValueSet
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

// @Service OCI configuration needs to be set up before we hook this into our spring boot startup TODO
class DataDictionaryService(private val ociClient: OCIClient) {
    private val logger = KotlinLogging.logger { }
    private val mapper = CsvMapper().apply {
        registerKotlinModule()
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
    private val schema = CsvSchema.emptySchema().withHeader()

    // Using ConcurrentHashMap for thread safety. contains a map of 'SystemValue' to a list of Data Dictionary UUIDs
    private var lookupMap: Map<SystemValue, List<String>> = ConcurrentHashMap()

    @PostConstruct
    fun init() {
        load() // run at startup
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // 24 hours in milliseconds
    fun load() {
        val tempMap = mutableMapOf<SystemValue, MutableList<String>>()
        val registryCSV =
            ociClient.getObjectFromINFX("Registries/v1/data dictionary/prod/38efb390-497f-4b49-9619-a45d33048a3a")
        val reader: MappingIterator<DataDictionaryRow> =
            mapper.readerFor(DataDictionaryRow::class.java).with(schema).readValues(registryCSV)
        val rows = reader.readAll()
        rows.forEach { dataDictionaryRow ->
            val valueSet =
                ociClient.getObjectFromINFX("ValueSets/v2/published/${dataDictionaryRow.valueSetUuid}")?.let {
                    JacksonUtil.readJsonObject(it, ValueSet::class)
                }
            valueSet?.expansion?.contains?.forEach valueSetLoop@{ value ->
                val key = SystemValue(value = value.code?.value, system = value.system?.value)
                if (key.system == null || key.value == null) {
                    logger.error { "Either code or system is null for value set item $value" }
                    return@valueSetLoop
                }
                tempMap.computeIfAbsent(key) { mutableListOf() }.add(dataDictionaryRow.valueSetUuid)
            }
        }
        lookupMap = tempMap
    }

    /**
     * For a given LOINC or SNOMED system and code value, return the associated data dictionary UUID(s)
     */
    fun getDataDictionaryByCode(system: String, value: String): List<String>? {
        return lookupMap[SystemValue(system, value)]
    }
}

data class DataDictionaryRow(
    val valueSetUuid: String,
    val productItemLabel: String
)

data class SystemValue(
    val system: String?,
    val value: String?
)
