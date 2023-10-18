package com.projectronin.clinical.trial.server.dataauthority

import com.mysql.cj.xdevapi.Collection
import com.projectronin.interop.common.jackson.JacksonUtil
import com.projectronin.interop.fhir.generators.primitives.of
import com.projectronin.interop.fhir.generators.resources.patient
import com.projectronin.interop.fhir.r4.resource.Patient
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PatientDAOTest : BaseMySQLTest() {

    private lateinit var collection: Collection
    private lateinit var dao: PatientDAO

    @BeforeAll
    fun initTest() {
        collection = createCollection(Patient::class.simpleName!!)
        val database = mockk<ClinicalTrialDataAuthorityDatabase>()
        every { database.createCollection(Patient::class.java) } returns ClinicalTrialDataAuthorityDatabase.SafeCollection(
            "resource",
            collection
        )
        every { database.run(any(), captureLambda<Collection.() -> Any>()) } answers {
            val collection = firstArg<ClinicalTrialDataAuthorityDatabase.SafeCollection>()
            val lamdba = secondArg<Collection.() -> Any>()
            lamdba.invoke(collection.collection)
        }
        dao = PatientDAO(database)
    }

    @Test
    fun `return all test`() {
        val testPat1 = patient { id of "TestPat1" }
        collection.add(JacksonUtil.writeJsonValue(testPat1)).execute()

        val testPat2 = patient { }
        collection.add(JacksonUtil.writeJsonValue(testPat2)).execute()

        val output = dao.getAll()
        assertTrue(output.size >= 2)
        assertTrue(output.find { it.id!!.value == "TestPat1" } == testPat1)
    }
}
