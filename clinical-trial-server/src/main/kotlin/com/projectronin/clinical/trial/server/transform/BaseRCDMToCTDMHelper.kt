package com.projectronin.clinical.trial.server.transform

import com.projectronin.interop.fhir.generators.datatypes.DynamicValues
import com.projectronin.interop.fhir.generators.datatypes.codeableConcept
import com.projectronin.interop.fhir.generators.datatypes.coding
import com.projectronin.interop.fhir.generators.datatypes.extension
import com.projectronin.interop.fhir.generators.datatypes.meta
import com.projectronin.interop.fhir.generators.primitives.of
import com.projectronin.interop.fhir.r4.CodeSystem
import com.projectronin.interop.fhir.r4.datatype.CodeableConcept
import com.projectronin.interop.fhir.r4.datatype.Extension
import com.projectronin.interop.fhir.r4.datatype.Meta
import com.projectronin.interop.fhir.r4.datatype.primitive.Canonical
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class BaseRCDMToCTDMHelper(
    private val dataDictionaryService: DataDictionaryService
) {
    fun setProfileMeta(displayValue: String): Meta? {
        dataDictionaryService.getValueSetUuidVersionByDisplay(displayValue)?.let {
            return meta {
                profile of listOf(Canonical("https://projectronin.io/fhir/StructureDefinition/CTDM-Observation"))
                tag of listOf(
                    coding {
                        system of it.first
                        display of displayValue
                        version of it.second
                    }
                )
            }
        }
        return null
    }
}
fun setCTDMExtensions(subjectId: String): List<Extension> {
    return listOf(
        extension {
            url of "https://projectronin.io/fhir/StructureDefinition/subjectId"
            value of DynamicValues.string(subjectId)
        },
        extension {
            url of "https://projectronin.io/fhir/StructureDefinition/DataTransformTimestamp"
            value of DynamicValues.dateTime(OffsetDateTime.now(ZoneOffset.UTC).toString())
        }
    )
}

fun setMetaCode(codeString: String, displayString: String): CodeableConcept {
    return codeableConcept {
        coding of listOf(
            coding {
                system of CodeSystem.LOINC.uri
                version of "2.76"
                code of codeString
                display of displayString
            }
        )
    }
}
