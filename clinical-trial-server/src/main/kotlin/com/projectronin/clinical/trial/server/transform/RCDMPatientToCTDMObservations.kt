package com.projectronin.clinical.trial.server.transform

import com.projectronin.clinical.trial.server.data.SubjectDAO
import com.projectronin.interop.fhir.generators.datatypes.DynamicValues
import com.projectronin.interop.fhir.generators.datatypes.annotation
import com.projectronin.interop.fhir.generators.datatypes.codeableConcept
import com.projectronin.interop.fhir.generators.datatypes.coding
import com.projectronin.interop.fhir.generators.primitives.of
import com.projectronin.interop.fhir.generators.resources.ObservationGenerator
import com.projectronin.interop.fhir.generators.resources.observation
import com.projectronin.interop.fhir.r4.CodeSystem
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.r4.datatype.primitive.Markdown
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
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
        val subject = rcdmPatient.id?.value?.let { subjectDAO.getSubjectByFhirId(it) }
            ?: throw Exception("No subject found for Ronin FHIR Id: ${rcdmPatient.id?.value}")

        // set birthDate
        rcdmPatient.birthDate?.let {
            obsList.add(
                rcdmPatientToDemographics(subject) {
                    meta of rcdmHelper.setProfileMeta("Birth Date")
                    code of setMetaCode("21112-8", "Birth date")
                    value of DynamicValues.dateTime(it.value!!)
                }
            )
        }
        // set gender
        rcdmPatient.gender?.let {
            obsList.add(
                rcdmPatientToDemographics(subject) {
                    meta of rcdmHelper.setProfileMeta("Sex")
                    code of setMetaCode("46098-0", "Sex")
                    value of DynamicValues.dateTime(it.value!!)
                }
            )
        }
        // set marital status
        rcdmPatient.maritalStatus?.let {
            obsList.add(
                rcdmPatientToDemographics(subject) {
                    meta of rcdmHelper.setProfileMeta("Marital Status")
                    code of setMetaCode("45404-10", "Marital Status")
                    value of DynamicValues.codeableConcept(it)
                }
            )
        }
        // set multiple birth
        rcdmPatient.multipleBirth?.let {
            obsList.add(
                rcdmPatientToDemographics(subject) {
                    meta of rcdmHelper.setProfileMeta("Multiple Birth")
                    code of setMetaCode("57722-1", "Multiple Birth")
                    value of it
                }
            )
        }
        // set communication language
        rcdmPatient.communication.forEach { lang ->
            lang.language?.let {
                obsList.add(
                    rcdmPatientToDemographics(subject) {
                        meta of rcdmHelper.setProfileMeta("Communication Language")
                        code of codeableConcept {
                            coding of listOf(
                                coding {
                                    system of CodeSystem.SNOMED_CT.uri
                                    version of "2023-09-01"
                                    code of "161139007"
                                    display of "Language spoken (observable entity)"
                                }
                            )
                        }
                        value of DynamicValues.codeableConcept(it)
                    }
                )
            }
        }
        // set religion, birthsex, gender identity, race, and ethnicity
        rcdmPatient.extension.forEach { ext ->
            when (ext.url) {
                Uri("http://hl7.org/fhir/StructureDefinition/patient-religion") ->
                    obsList.add(
                        rcdmPatientToDemographics(subject) {
                            meta of rcdmHelper.setProfileMeta("Religion")
                            code of codeableConcept {
                                coding of listOf(
                                    coding {
                                        system of CodeSystem.SNOMED_CT.uri
                                        version of "2023-09-01"
                                        code of "160538000"
                                        display of "Religious affiliation (observable entity)"
                                    }
                                )
                            }
                            value of ext.value
                        }
                    )
                Uri("http://hl7.org/fhir/us/core/StructureDefinition/us-core-birthsex") ->
                    obsList.add(
                        rcdmPatientToDemographics(subject) {
                            meta of rcdmHelper.setProfileMeta("Birth Sex Observation")
                            code of setMetaCode("76689-9", "Sex assigned at birth")
                            value of ext.value
                        }
                    )
                Uri("http://hl7.org/fhir/us/core/StructureDefinition/us-core-genderIdentity") ->
                    obsList.add(
                        rcdmPatientToDemographics(subject) {
                            meta of rcdmHelper.setProfileMeta("Gender Identity")
                            code of setMetaCode("76691-5", "Gender Identity")
                            value of ext.value
                        }
                    )
                Uri("http://hl7.org/fhir/us/core/StructureDefinition/us-core-race") ->
                    ext.extension.forEach {
                        when (it.url) {
                            Uri("ombCategory") -> obsList.add(
                                rcdmPatientToDemographicsRace(subject) {
                                    value of it.value
                                    note of listOf(
                                        annotation {
                                            author of DynamicValues.string("Ronin")
                                            text of Markdown("OMB Race Category")
                                        }
                                    )
                                }
                            )
                            Uri("detailed") -> obsList.add(
                                rcdmPatientToDemographicsRace(subject) {
                                    value of it.value
                                    note of listOf(
                                        annotation {
                                            author of DynamicValues.string("Ronin")
                                            text of Markdown("Detailed CDC Race")
                                        }
                                    )
                                }
                            )
                            Uri("text") -> obsList.add(
                                rcdmPatientToDemographicsRace(subject) {
                                    value of it.value
                                    note of listOf(
                                        annotation {
                                            author of DynamicValues.string("Ronin")
                                            text of Markdown("Free-text Race")
                                        }
                                    )
                                }
                            )
                        }
                    }
                Uri("http://hl7.org/fhir/us/core/StructureDefinition/us-core-ethnicity") ->
                    ext.extension.forEach {
                        when (it.url) {
                            Uri("ombCategory") -> obsList.add(
                                rcdmPatientToDemographicsEthnicity(subject) {
                                    value of it.value
                                    note of listOf(
                                        annotation {
                                            author of DynamicValues.string("Ronin")
                                            text of Markdown("OMB Ethnicity Category")
                                        }
                                    )
                                }
                            )
                            Uri("detailed") -> obsList.add(
                                rcdmPatientToDemographicsEthnicity(subject) {
                                    value of it.value
                                    note of listOf(
                                        annotation {
                                            author of DynamicValues.string("Ronin")
                                            text of Markdown("Detailed CDC Ethnicity")
                                        }
                                    )
                                }
                            )
                            Uri("text") -> obsList.add(
                                rcdmPatientToDemographicsEthnicity(subject) {
                                    value of it.value
                                    note of listOf(
                                        annotation {
                                            author of DynamicValues.string("Ronin")
                                            text of Markdown("Free-text Ethnicity")
                                        }
                                    )
                                }
                            )
                        }
                    }
            }
        }
        return obsList
    }

    fun rcdmPatientToDemographics(subjectId: String, block: ObservationGenerator.() -> Unit): Observation {
        return observation {
            block.invoke(this)
            id of Id(UUID.randomUUID().toString())
            extension of setCTDMExtensions(subjectId)
            status of "Final"
            category of listOf(
                codeableConcept {
                    coding of listOf(
                        coding {
                            display of "Demographics"
                        }
                    )
                }
            )
            effective of DynamicValues.dateTime(OffsetDateTime.now(ZoneOffset.UTC).toString())
        }
    }

    fun rcdmPatientToDemographicsRace(subject: String, block: ObservationGenerator.() -> Unit): Observation {
        return rcdmPatientToDemographics(subject) {
            block.invoke(this)
            meta of rcdmHelper.setProfileMeta("Race")
            code of codeableConcept {
                coding of listOf(
                    coding {
                        system of CodeSystem.SNOMED_CT.uri
                        version of "2023-09-01"
                        code of "103579009"
                        display of "Race (observable entity)"
                    }
                )
            }
        }
    }

    fun rcdmPatientToDemographicsEthnicity(subject: String, block: ObservationGenerator.() -> Unit): Observation {
        return rcdmPatientToDemographics(subject) {
            block.invoke(this)
            meta of rcdmHelper.setProfileMeta("Ethnicity")
            code of codeableConcept {
                coding of listOf(
                    coding {
                        system of CodeSystem.SNOMED_CT.uri
                        version of "2023-09-01"
                        code of "186034007"
                        display of "Ethnicity / related nationality data (observable entity)"
                    }
                )
            }
        }
    }
}
