package com.projectronin.clinical.trial.server.transform

import com.projectronin.interop.fhir.r4.CodeSystem
import com.projectronin.interop.fhir.r4.datatype.CodeableConcept
import com.projectronin.interop.fhir.r4.datatype.Coding
import com.projectronin.interop.fhir.r4.datatype.DynamicValue
import com.projectronin.interop.fhir.r4.datatype.DynamicValueType
import com.projectronin.interop.fhir.r4.datatype.Extension
import com.projectronin.interop.fhir.r4.datatype.Meta
import com.projectronin.interop.fhir.r4.datatype.primitive.Canonical
import com.projectronin.interop.fhir.r4.datatype.primitive.Code
import com.projectronin.interop.fhir.r4.datatype.primitive.DateTime
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import com.projectronin.interop.fhir.r4.datatype.primitive.asFHIR
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class BaseRCDMToCTDMHelper(
    private val dataDictionaryService: DataDictionaryService,
) {
    fun setProfileMeta(displayValue: String): Meta? {
        dataDictionaryService.getValueSetUuidVersionByDisplay(displayValue)?.let {
            return Meta(
                profile = listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation")),
                tag =
                    listOf(
                        Coding(
                            system = Uri(it.first),
                            display = displayValue.asFHIR(),
                            version = it.second.asFHIR(),
                        ),
                    ),
            )
        }
        return null
    }
}

fun setCTDMExtensions(subjectId: String): List<Extension> {
    return listOf(
        Extension(
            url = Uri("https://projectronin.io/fhir/StructureDefinition/subjectId"),
            value = DynamicValue(DynamicValueType.STRING, subjectId.asFHIR()),
        ),
        Extension(
            url = Uri("https://projectronin.io/fhir/StructureDefinition/DataTransformTimestamp"),
            value = DynamicValue(DynamicValueType.DATE_TIME, DateTime(OffsetDateTime.now(ZoneOffset.UTC).toString())),
        ),
    )
}

fun setMetaCode(
    codeString: String,
    displayString: String,
): CodeableConcept {
    return CodeableConcept(
        coding =
            listOf(
                Coding(
                    system = CodeSystem.LOINC.uri,
                    version = "2.76".asFHIR(),
                    code = Code(codeString),
                    display = displayString.asFHIR(),
                ),
            ),
    )
}
