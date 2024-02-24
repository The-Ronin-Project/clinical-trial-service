package com.projectronin.clinical.trial.server.dataauthority

import com.mysql.cj.xdevapi.Collection
import com.projectronin.clinical.trial.server.transform.setCTDMExtensions
import com.projectronin.interop.common.jackson.JacksonUtil
import com.projectronin.interop.fhir.generators.primitives.of
import com.projectronin.interop.fhir.generators.resources.observation
import com.projectronin.interop.fhir.r4.datatype.Coding
import com.projectronin.interop.fhir.r4.datatype.DynamicValue
import com.projectronin.interop.fhir.r4.datatype.DynamicValueType
import com.projectronin.interop.fhir.r4.datatype.Meta
import com.projectronin.interop.fhir.r4.datatype.Period
import com.projectronin.interop.fhir.r4.datatype.primitive.DateTime
import com.projectronin.interop.fhir.r4.datatype.primitive.FHIRString
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import com.projectronin.interop.fhir.r4.resource.Observation
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.ZoneId
import java.time.ZonedDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ObservationDAOTest : BaseMySQLTest() {
    private lateinit var collection: Collection
    private lateinit var dao: ObservationDAO

    @BeforeAll
    fun initTest() {
        collection = createCollection(Observation::class.simpleName!!)
        val database = mockk<ClinicalTrialDataAuthorityDatabase>()
        every { database.createCollection(Observation::class.java) } returns
            ClinicalTrialDataAuthorityDatabase.SafeCollection(
                "resource",
                collection,
            )
        every { database.run(any(), captureLambda<Collection.() -> Any>()) } answers {
            val collection = firstArg<ClinicalTrialDataAuthorityDatabase.SafeCollection>()
            val lamdba = secondArg<Collection.() -> Any>()
            lamdba.invoke(collection.collection)
        }
        dao = ObservationDAO(database)
    }

    @BeforeEach
    fun clean() {
        collection.remove("_id IS NOT NULL").execute()
    }

    @Test
    fun `getAll test`() {
        val testObs1 = observation { id of "TestObservation1" }
        collection.add(JacksonUtil.writeJsonValue(testObs1)).execute()

        val testObs2 = observation { }
        collection.add(JacksonUtil.writeJsonValue(testObs2)).execute()

        val output = dao.getAll()
        assertEquals(2, output.size)
        assertEquals(testObs1, output.find { it.id!!.value == "TestObservation1" })
    }

    @Test
    fun `findById test`() {
        val testObs1 = observation { id of "TestObservation1" }
        collection.add(JacksonUtil.writeJsonValue(testObs1)).execute()

        val output = dao.findById("TestObservation1")
        assertEquals(testObs1, output)
    }

    @Test
    fun `insert test`() {
        assertTrue(dao.getAll().isEmpty())
        val testObs1 = observation { id of "TestObservation1" }

        dao.insert(testObs1)
        assertEquals(1, dao.getAll().size)
    }

    @Test
    fun `update test`() {
        val testObs = observation { id of Id("TestObservation1") }
        collection.add(JacksonUtil.writeJsonValue(testObs)).execute()
        assertEquals(1, dao.getAll().size)

        val expectedExtensionUri = "https://projectronin.io/fhir/StructureDefinition/DataUpdateTimestamp"
        val testObsUpdate =
            observation {
                id of Id("TestObservation1")
                meta of Meta(tag = listOf(Coding(display = FHIRString("vital"))))
            }
        dao.update(testObsUpdate)
        val result = dao.getAll()
        assertEquals(1, result.size)
        assertEquals(testObsUpdate.id, result.first().id)
        assertEquals(testObsUpdate.meta, result.first().meta)
        assertNotNull(result.first().extension)
        assertEquals(expectedExtensionUri, result.first().extension.first().url?.value)
        assertEquals(DynamicValueType.DATE_TIME, result.first().extension.first().value?.type)
        assertInstanceOf(DateTime::class.java, result.first().extension.first().value?.value)
    }

    @Test
    fun `delete test`() {
        val testObs1 = observation { id of "TestObservation1" }
        collection.add(JacksonUtil.writeJsonValue(testObs1)).execute()
        assertEquals(1, dao.getAll().size)

        dao.delete("TestObservation1")
        assertEquals(0, dao.getAll().size)
    }

    @Test
    fun `search date filtering test`() {
        val testObs1 =
            observation {
                id of "TestObservation1"
                effective of DynamicValue(DynamicValueType.DATE_TIME, DateTime("2022-01-01T00:00:00"))
            }
        collection.add(JacksonUtil.writeJsonValue(testObs1)).execute()

        val testObs2 = observation { }
        collection.add(JacksonUtil.writeJsonValue(testObs2)).execute()

        val testObs3 =
            observation {
                id of "TestObservation3"
                effective of
                    DynamicValue(
                        DynamicValueType.PERIOD,
                        Period(start = DateTime("2022-01-01"), end = DateTime("2022-02-03")),
                    )
            }
        collection.add(JacksonUtil.writeJsonValue(testObs3)).execute()

        // No query params returns anything with an id
        assertEquals(2, dao.search().size)

        val jan1 = ZonedDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
        val jan2 = ZonedDateTime.of(2022, 1, 2, 1, 1, 1, 0, ZoneId.of("UTC"))
        val mar20 = ZonedDateTime.of(2022, 3, 20, 0, 0, 0, 0, ZoneId.of("UTC"))
        assertEquals(0, dao.search(fromDate = jan2).size)

        assertEquals(2, dao.search(fromDate = jan1).size)

        assertEquals(1, dao.search(toDate = jan2).size)

        assertEquals(1, dao.search(toDate = jan1).size)

        assertEquals(0, dao.search(fromDate = jan2, toDate = mar20).size)

        assertEquals(1, dao.search(fromDate = jan1, toDate = jan2).size)

        assertEquals(2, dao.search(fromDate = jan1, toDate = mar20).size)
    }

    @Test
    fun `search test`() {
        val testObs1 =
            observation {
                id of "TestObservation1"
                extension of setCTDMExtensions(subjectId = "subject1")
                effective of DynamicValue(DynamicValueType.DATE_TIME, DateTime("2022-01-01"))
                meta of
                    Meta(
                        tag =
                            listOf(
                                Coding(system = Uri("1"), display = FHIRString("lab")),
                            ),
                    )
            }
        collection.add(JacksonUtil.writeJsonValue(testObs1)).execute()

        val testObs2 = observation { }
        collection.add(JacksonUtil.writeJsonValue(testObs2)).execute()

        val testObs3 =
            observation {
                id of "TestObservation3"
                extension of setCTDMExtensions(subjectId = "subject1")
                effective of
                    DynamicValue(
                        DynamicValueType.PERIOD,
                        Period(start = DateTime("2022-01-01"), end = DateTime("2022-02-03")),
                    )
                meta of
                    Meta(
                        tag =
                            listOf(
                                Coding(system = Uri("2"), display = FHIRString("imaging")),
                            ),
                    )
            }
        collection.add(JacksonUtil.writeJsonValue(testObs3)).execute()

        val testObs4 =
            observation {
                id of "TestObservation4"
                extension of setCTDMExtensions(subjectId = "subject1")
                effective of DynamicValue(DynamicValueType.DATE_TIME, DateTime("2022-01-01"))
                meta of
                    Meta(
                        tag =
                            listOf(
                                Coding(system = Uri("3"), display = FHIRString("vital")),
                            ),
                    )
            }
        collection.add(JacksonUtil.writeJsonValue(testObs4)).execute()

        val testObs5 =
            observation {
                id of "TestObservation5"
                extension of setCTDMExtensions(subjectId = "subject2")
                effective of DynamicValue(DynamicValueType.DATE_TIME, DateTime("2022-03-01"))
                meta of
                    Meta(
                        tag =
                            listOf(
                                Coding(system = Uri("4"), display = FHIRString("other")),
                            ),
                    )
            }
        collection.add(JacksonUtil.writeJsonValue(testObs5)).execute()

        val res = dao.search(subjectId = "subject1", valueSetIds = listOf("2", "3"))
        assertEquals(2, res.size)

        val result = dao.search(subjectId = "subject1")
        assertEquals(3, result.size)

        val result1 = dao.search(subjectId = "subject2")
        assertEquals(1, result1.size)

        val result2 = dao.search(valueSetIds = listOf("1"))
        assertEquals(1, result2.size)

        val result3 = dao.search(valueSetIds = listOf("3"))
        assertEquals(1, result3.size)

        val result4 = dao.search(subjectId = "subject2", valueSetIds = listOf("1"))
        assertEquals(0, result4.size)

        val feb5 = ZonedDateTime.of(2022, 2, 5, 0, 1, 1, 0, ZoneId.of("UTC"))
        val result5 = dao.search(subjectId = "subject1", valueSetIds = listOf("2"), toDate = feb5)
        assertEquals(1, result5.size)
    }

    @Test
    fun `search test - large valueset values`() {
        val idSet = (1..60).toList().map { it.toString() }
        val testObs1 =
            observation {
                id of "TestObservation1"
                extension of setCTDMExtensions(subjectId = "subject1")
                effective of DynamicValue(DynamicValueType.DATE_TIME, DateTime("2022-01-01"))
                meta of
                    Meta(
                        tag =
                            listOf(
                                Coding(system = Uri("1"), display = FHIRString("lab")),
                            ),
                    )
            }
        collection.add(JacksonUtil.writeJsonValue(testObs1)).execute()
        val testObs2 =
            observation {
                id of "TestObservation2"
                extension of setCTDMExtensions(subjectId = "subject1")
                effective of DynamicValue(DynamicValueType.DATE_TIME, DateTime("2022-01-01"))
                meta of
                    Meta(
                        tag =
                            listOf(
                                Coding(system = Uri("55"), display = FHIRString("lab")),
                            ),
                    )
            }
        collection.add(JacksonUtil.writeJsonValue(testObs2)).execute()
        val testObs3 =
            observation {
                id of "TestObservation3"
                extension of setCTDMExtensions(subjectId = "subject1")
                effective of DynamicValue(DynamicValueType.DATE_TIME, DateTime("2022-01-01"))
                meta of
                    Meta(
                        tag =
                            listOf(
                                Coding(system = Uri("75"), display = FHIRString("lab")),
                            ),
                    )
            }
        collection.add(JacksonUtil.writeJsonValue(testObs3)).execute()
        val result = dao.search(valueSetIds = idSet)
        assertEquals(2, result.size)
    }
}
