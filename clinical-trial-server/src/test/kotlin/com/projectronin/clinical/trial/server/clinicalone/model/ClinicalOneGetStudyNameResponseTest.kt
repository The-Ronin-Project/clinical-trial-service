package com.projectronin.clinical.trial.server.clinicalone.model

import com.fasterxml.jackson.module.kotlin.readValue
import com.projectronin.interop.common.jackson.JacksonManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClinicalOneGetStudyNameResponseTest {
    @Test
    fun `serialize`() {
        val studyNameResponseResult =
            ClinicalOneGetStudyNameResponse.ClinicalOneGetStudyNameResponseResult(
                identity = "test",
                id = "test",
                version = "1.0.0.1",
                versionStart = "2023-10-19T20:12:44.429Z",
                versionEnd = "2023-10-30T16:46:41.991Z",
                studyTitle = "Ronin POC",
                studyDescription = "Ronin POC",
                contractCode = null,
                versionTitle = "Ronin POC",
                studyPhase = "2",
                therapeuticArea = "test",
                openLabelBlinded = "test",
                studyStatus = "ACTIVE",
                versionPreviousStatus = "TEST",
                versionStatus = "ARCHIVED",
                versionType = "INITIAL",
                hasObjects = 1,
                draftVersionsCount = 0,
                testingVersionsCount = 0,
                approvedVersionsCount = 0,
                archivedVersionsCount = 0,
                everApprovedCount = 0,
                modes = listOf("test"),
                customerRefNo = "test",
                description = "test",
                singleKitSettingId = "test",
                sowNo = "test",
                sponsorId = "test",
                studyStatusId = 123,
                useBlindingGroups = false,
                useLabelGroups = false,
                useShippinggroups = false,
                previousState = "ACTIVE",
                studyState = "ACTIVE",
                lastModified = "2024-01-16T16:45:48.565Z",
                systemCodeListId = "test",
                companies = "test",
                companyLabels = "test",
            )
        val studyNameResponse =
            ClinicalOneGetStudyNameResponse(
                status = "test",
                result = listOf(studyNameResponseResult),
                errorData = "test",
                version = 1,
            )
        val json = JacksonManager.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(studyNameResponse)
        val expectedJson =
            """
            {
              "status" : "test",
              "result" : [ {
                "identity" : "test",
                "id" : "test",
                "version" : "1.0.0.1",
                "versionStart" : "2023-10-19T20:12:44.429Z",
                "versionEnd" : "2023-10-30T16:46:41.991Z",
                "studyTitle" : "Ronin POC",
                "studyDescription" : "Ronin POC",
                "versionTitle" : "Ronin POC",
                "studyPhase" : "2",
                "therapeuticArea" : "test",
                "openLabelBlinded" : "test",
                "studyStatus" : "ACTIVE",
                "versionPreviousStatus" : "TEST",
                "versionStatus" : "ARCHIVED",
                "versionType" : "INITIAL",
                "hasObjects" : 1,
                "draftVersionsCount" : 0,
                "testingVersionsCount" : 0,
                "approvedVersionsCount" : 0,
                "archivedVersionsCount" : 0,
                "everApprovedCount" : 0,
                "modes" : [ "test" ],
                "customerRefNo" : "test",
                "description" : "test",
                "singleKitSettingId" : "test",
                "sowNo" : "test",
                "sponsorId" : "test",
                "studyStatusId" : 123,
                "useBlindingGroups" : false,
                "useLabelGroups" : false,
                "useShippinggroups" : false,
                "previousState" : "ACTIVE",
                "studyState" : "ACTIVE",
                "lastModified" : "2024-01-16T16:45:48.565Z",
                "systemCodeListId" : "test",
                "companies" : "test",
                "companyLabels" : "test"
              } ],
              "errorData" : "test",
              "version" : 1
            }
            """.trimIndent()
        val deserialized = JacksonManager.objectMapper.readValue<ClinicalOneGetStudyNameResponse>(expectedJson)
        assertEquals(studyNameResponse, deserialized)
        assertEquals(json, expectedJson)
    }
}
