package com.projectronin.clinical.trial.server.dataauthority

import com.projectronin.interop.fhir.generators.resources.condition
import com.projectronin.interop.fhir.generators.resources.observation
import com.projectronin.interop.fhir.r4.datatype.DynamicValue
import com.projectronin.interop.fhir.r4.datatype.DynamicValueType
import com.projectronin.interop.fhir.r4.datatype.Extension
import com.projectronin.interop.fhir.r4.datatype.primitive.DateTime
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import com.projectronin.interop.fhir.r4.datatype.primitive.asFHIR
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DaoUtilsTest {
    @Test
    fun `populateDataUpdateTimestampExtension adds extension`() {
        val observation = observation { id of Id("testObservation") }
        val expectedExtensionUri = "https://projectronin.io/fhir/StructureDefinition/DataUpdateTimestamp"

        val actualObservation = observation.populateDataUpdateTimestampExtension()
        assertEquals(observation.id, actualObservation.id)
        assertNotNull(actualObservation.extension)
        assertEquals(expectedExtensionUri, actualObservation.extension.first().url?.value)
        assertEquals(DynamicValueType.DATE_TIME, actualObservation.extension.first().value?.type)
        assertInstanceOf(DateTime::class.java, actualObservation.extension.first().value?.value)
    }

    @Test
    fun `populateDataUpdateTimestampExtension does not interfere with other extensions`() {
        val firstExtension =
            Extension(
                url = Uri("https://projectronin.io/fhir/StructureDefinition/subjectId"),
                value = DynamicValue(DynamicValueType.STRING, "subjectId".asFHIR()),
            )

        val condition =
            condition {
                id of Id("testCondition")
                extension of listOf(firstExtension)
            }
        val expectedExtensionUri = "https://projectronin.io/fhir/StructureDefinition/DataUpdateTimestamp"

        val actualCondition = condition.populateDataUpdateTimestampExtension()
        assertEquals(2, actualCondition.extension.size)
        assertTrue(actualCondition.extension.contains(firstExtension))
        assertEquals(expectedExtensionUri, actualCondition.extension[1].url?.value)
        assertEquals(DynamicValueType.DATE_TIME, actualCondition.extension[1].value?.type)
        assertInstanceOf(DateTime::class.java, actualCondition.extension[1].value?.value)
    }

    @Test
    fun `extension urls are what is expected`() {
        assertEquals(
            "https://projectronin.io/fhir/StructureDefinition/DataUpdateTimestamp",
            ExtensionUrls.DATA_UPDATE_TIMESTAMP_URL,
        )

        assertEquals(
            "https://projectronin.io/fhir/StructureDefinition/DataTransformTimestamp",
            ExtensionUrls.DATA_TRANSFORM_TIMESTAMP_URL,
        )

        assertEquals(
            "https://projectronin.io/fhir/StructureDefinition/subjectId",
            ExtensionUrls.SUBJECT_ID_URL,
        )
    }
}
