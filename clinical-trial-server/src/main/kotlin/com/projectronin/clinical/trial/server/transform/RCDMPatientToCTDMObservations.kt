package com.projectronin.clinical.trial.server.transform

import com.projectronin.clinical.trial.server.data.SubjectDAO
import com.projectronin.interop.fhir.generators.datatypes.DynamicValues
import com.projectronin.interop.fhir.generators.datatypes.codeableConcept
import com.projectronin.interop.fhir.generators.datatypes.coding
import com.projectronin.interop.fhir.r4.CodeSystem
import com.projectronin.interop.fhir.r4.datatype.Annotation
import com.projectronin.interop.fhir.r4.datatype.CodeableConcept
import com.projectronin.interop.fhir.r4.datatype.Coding
import com.projectronin.interop.fhir.r4.datatype.Reference
import com.projectronin.interop.fhir.r4.datatype.primitive.Code
import com.projectronin.interop.fhir.r4.datatype.primitive.FHIRString
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.r4.datatype.primitive.Markdown
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import com.projectronin.interop.fhir.r4.datatype.primitive.asFHIR
import com.projectronin.interop.fhir.r4.resource.Observation
import com.projectronin.interop.fhir.r4.resource.Patient
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Component
class RCDMPatientToCTDMObservations(
    private val subjectDAO: SubjectDAO,
    private val rcdmHelper: BaseRCDMToCTDMHelper
) {

    fun splitPatientDemographics(rcdmPatient: Patient): List<Observation> {
        val obsList: MutableList<Observation> = ArrayList<Observation>()

        // get study patient info
        val fhirId = rcdmPatient.id?.value
        val subject = fhirId?.let { subjectDAO.getSubjectByFhirId(it) }
            ?: throw Exception("No subject found for Ronin FHIR Id: ${rcdmPatient.id?.value}")

        // set birthDate
        rcdmPatient.birthDate?.let {
            obsList.add(
                rcdmPatientToDemographics(subject).copy(
                    meta = rcdmHelper.setProfileMeta("Birth Date"),
                    code = setMetaCode("21112-8", "Birth date"),
                    value = DynamicValues.dateTime(it.value!!)
                )
            )
        }
        // set gender
        rcdmPatient.gender?.let {
            obsList.add(
                rcdmPatientToDemographics(subject).copy(
                    meta = rcdmHelper.setProfileMeta("Sex"),
                    code = setMetaCode("46098-0", "Sex"),
                    value = DynamicValues.dateTime(it.value!!)
                )
            )
        }
        // set marital status
        rcdmPatient.maritalStatus?.let {
            obsList.add(
                rcdmPatientToDemographics(subject).copy(
                    meta = rcdmHelper.setProfileMeta("Marital Status"),
                    code = setMetaCode("45404-10", "Marital Status"),
                    value = DynamicValues.codeableConcept(it)
                )
            )
        }
        // set multiple birth
        rcdmPatient.multipleBirth?.let {
            obsList.add(
                rcdmPatientToDemographics(subject).copy(
                    meta = rcdmHelper.setProfileMeta("Multiple Birth"),
                    code = setMetaCode("57722-1", "Multiple Birth"),
                    value = it
                )
            )
        }
        // set communication language
        rcdmPatient.communication.forEach { lang ->
            lang.language?.let {
                obsList.add(
                    rcdmPatientToDemographics(subject).copy(
                        meta = rcdmHelper.setProfileMeta("Communication Language"),
                        code = CodeableConcept(
                            coding = listOf(
                                Coding(
                                    system = CodeSystem.SNOMED_CT.uri,
                                    version = "2023-09-01".asFHIR(),
                                    code = Code("161139007"),
                                    display = "Language spoken (observable entity)".asFHIR()
                                )
                            )
                        ),
                        value = DynamicValues.codeableConcept(it)
                    )
                )
            }
        }
        // set religion, birthsex, gender identity, race, and ethnicity
        rcdmPatient.extension.forEach { ext ->
            when (ext.url) {
                Uri("http://hl7.org/fhir/StructureDefinition/patient-religion") ->
                    obsList.add(
                        rcdmPatientToDemographics(subject).copy(
                            meta = rcdmHelper.setProfileMeta("Religion"),
                            code = CodeableConcept(
                                coding = listOf(
                                    Coding(
                                        system = CodeSystem.SNOMED_CT.uri,
                                        version = "2023-09-01".asFHIR(),
                                        code = Code("160538000"),
                                        display = "Religious affiliation (observable entity)".asFHIR()
                                    )
                                )
                            ),
                            value = ext.value
                        )
                    )
                Uri("http://hl7.org/fhir/us/core/StructureDefinition/us-core-birthsex") ->
                    obsList.add(
                        rcdmPatientToDemographics(subject).copy(
                            meta = rcdmHelper.setProfileMeta("Birth Sex Observation"),
                            code = setMetaCode("76689-9", "Sex assigned at birth"),
                            value = ext.value
                        )
                    )
                Uri("http://hl7.org/fhir/us/core/StructureDefinition/us-core-genderIdentity") ->
                    obsList.add(
                        rcdmPatientToDemographics(subject).copy(
                            meta = rcdmHelper.setProfileMeta("Gender Identity"),
                            code = setMetaCode("76691-5", "Gender Identity"),
                            value = ext.value
                        )
                    )
                Uri("http://hl7.org/fhir/us/core/StructureDefinition/us-core-race") ->
                    ext.extension.forEach {
                        when (it.url) {
                            Uri("ombCategory") -> obsList.add(
                                rcdmPatientToDemographicsRace(subject).copy(
                                    value = it.value,
                                    note = listOf(
                                        Annotation(
                                            author = DynamicValues.string("Ronin"),
                                            text = Markdown("OMB Race Category")
                                        )
                                    )
                                )
                            )
                            Uri("detailed") -> obsList.add(
                                rcdmPatientToDemographicsRace(subject).copy(
                                    value = it.value,
                                    note = listOf(
                                        Annotation(
                                            author = DynamicValues.string("Ronin"),
                                            text = Markdown("Detailed CDC Race")
                                        )
                                    )
                                )
                            )
                            Uri("text") -> obsList.add(
                                rcdmPatientToDemographicsRace(subject).copy(
                                    value = it.value,
                                    note = listOf(
                                        Annotation(
                                            author = DynamicValues.string("Ronin"),
                                            text = Markdown("Free-text Race")
                                        )
                                    )
                                )
                            )
                        }
                    }
                Uri("http://hl7.org/fhir/us/core/StructureDefinition/us-core-ethnicity") ->
                    ext.extension.forEach {
                        when (it.url) {
                            Uri("ombCategory") -> obsList.add(
                                rcdmPatientToDemographicsEthnicity(subject).copy(
                                    value = it.value,
                                    note = listOf(
                                        Annotation(
                                            author = DynamicValues.string("Ronin"),
                                            text = Markdown("OMB Ethnicity Category")
                                        )
                                    )
                                )
                            )
                            Uri("detailed") -> obsList.add(
                                rcdmPatientToDemographicsEthnicity(subject).copy(
                                    value = it.value,
                                    note = listOf(
                                        Annotation(
                                            author = DynamicValues.string("Ronin"),
                                            text = Markdown("Detailed CDC Ethnicity")
                                        )
                                    )
                                )
                            )
                            Uri("text") -> obsList.add(
                                rcdmPatientToDemographicsEthnicity(subject).copy(
                                    value = it.value,
                                    note = listOf(
                                        Annotation(
                                            author = DynamicValues.string("Ronin"),
                                            text = Markdown("Free-text Ethnicity")
                                        )
                                    )
                                )
                            )
                        }
                    }
            }
        }
        return obsList.map { it.copy(subject = Reference(reference = FHIRString("Patient/$fhirId"))) }
    }

    fun rcdmPatientToDemographics(subjectId: String): Observation {
        return Observation(
            id = Id(UUID.randomUUID().toString()),
            extension = setCTDMExtensions(subjectId),
            status = Code("Final"),
            category = listOf(
                CodeableConcept(
                    coding = listOf(
                        Coding(
                            display = "Demographics".asFHIR()
                        )
                    )
                )
            ),
            code = null,
            effective = DynamicValues.dateTime(OffsetDateTime.now(ZoneOffset.UTC).toString())
        )
    }

    fun rcdmPatientToDemographicsRace(subject: String): Observation {
        return rcdmPatientToDemographics(subject).copy(
            meta = rcdmHelper.setProfileMeta("Race"),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.SNOMED_CT.uri,
                        version = "2023-09-01".asFHIR(),
                        code = Code("103579009"),
                        display = "Race (observable entity)".asFHIR()
                    )
                )
            )
        )
    }

    fun rcdmPatientToDemographicsEthnicity(subject: String): Observation {
        return rcdmPatientToDemographics(subject).copy(
            meta = rcdmHelper.setProfileMeta("Ethnicity"),
            code = CodeableConcept(
                coding = listOf(
                    Coding(
                        system = CodeSystem.SNOMED_CT.uri,
                        version = "2023-09-01".asFHIR(),
                        code = Code("186034007"),
                        display = "Ethnicity / related nationality data (observable entity)".asFHIR()
                    )
                )
            )
        )
    }
}
