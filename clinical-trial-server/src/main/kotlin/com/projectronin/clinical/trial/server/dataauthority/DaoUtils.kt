package com.projectronin.clinical.trial.server.dataauthority

import com.projectronin.clinical.trial.server.util.copy
import com.projectronin.interop.fhir.r4.datatype.DynamicValue
import com.projectronin.interop.fhir.r4.datatype.DynamicValueType
import com.projectronin.interop.fhir.r4.datatype.Extension
import com.projectronin.interop.fhir.r4.datatype.primitive.DateTime
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import com.projectronin.interop.fhir.r4.resource.DomainResource
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun DomainResource<*>.populateDataUpdateTimestampExtension(): DomainResource<*> =
    copy(
        this,
        mapOf(
            "extension" to
                this.extension +
                Extension(
                    url = Uri(ExtensionUrls.DATA_UPDATE_TIMESTAMP_URL),
                    value = DynamicValue(DynamicValueType.DATE_TIME, DateTime(OffsetDateTime.now(ZoneOffset.UTC).toString())),
                ),
        ),
    )

class ExtensionUrls {
    companion object {
        const val DATA_UPDATE_TIMESTAMP_URL = "https://projectronin.io/fhir/StructureDefinition/DataUpdateTimestamp"
        const val DATA_TRANSFORM_TIMESTAMP_URL = "https://projectronin.io/fhir/StructureDefinition/DataTransformTimestamp"
        const val SUBJECT_ID_URL = "https://projectronin.io/fhir/StructureDefinition/subjectId"
    }
}
