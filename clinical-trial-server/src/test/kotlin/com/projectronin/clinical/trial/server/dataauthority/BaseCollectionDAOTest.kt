package com.projectronin.clinical.trial.server.dataauthority

import com.mysql.cj.xdevapi.Collection
import com.projectronin.interop.common.jackson.JacksonUtil
import com.projectronin.interop.fhir.generators.primitives.of
import com.projectronin.interop.fhir.generators.resources.observation
import com.projectronin.interop.fhir.r4.datatype.Coding
import com.projectronin.interop.fhir.r4.datatype.DynamicValue
import com.projectronin.interop.fhir.r4.datatype.DynamicValueType
import com.projectronin.interop.fhir.r4.datatype.Extension
import com.projectronin.interop.fhir.r4.datatype.Meta
import com.projectronin.interop.fhir.r4.datatype.primitive.DateTime
import com.projectronin.interop.fhir.r4.datatype.primitive.FHIRString
import com.projectronin.interop.fhir.r4.datatype.primitive.Id
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import com.projectronin.interop.fhir.r4.resource.Observation
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.ZoneId
import java.time.ZonedDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BaseCollectionDAOTest : BaseMySQLTest() {
    private lateinit var baseCollectionDAO: BaseCollectionDAO<Observation>
    private lateinit var collection: Collection

    @BeforeAll
    fun setup() {
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
        baseCollectionDAO = BaseCollectionDAO(database, Observation::class.java)
    }

    @BeforeEach
    fun clean() {
        collection.remove("_id IS NOT NULL").execute()
    }

    @Test
    fun `getAll works`() {
        val testObs1 = observation { id of "TestObservation1" }
        collection.add(JacksonUtil.writeJsonValue(testObs1)).execute()

        val testObs2 = observation { }
        collection.add(JacksonUtil.writeJsonValue(testObs2)).execute()

        val output = baseCollectionDAO.getAll()
        Assertions.assertEquals(2, output.size)
        Assertions.assertEquals(testObs1, output.find { it.id!!.value == "TestObservation1" })
    }

    @Test
    fun `findById works`() {
        val testObs1 = observation { id of "TestObservation1" }
        collection.add(JacksonUtil.writeJsonValue(testObs1)).execute()

        val output = baseCollectionDAO.findById("TestObservation1")
        Assertions.assertEquals(testObs1, output)
    }

    @Test
    fun `find by update since finds no resources when none are updated and parameter is null`() {
        Assertions.assertTrue(baseCollectionDAO.getAll().isEmpty())
        val testObservations =
            listOf(
                observation {
                    id of "TestObservation1"
                },
                observation {
                    id of "TestObservation2"
                },
            )
        testObservations.forEach { observation ->
            collection.add(JacksonUtil.writeJsonValue(observation)).execute()
        }

        val observations = baseCollectionDAO.findUpdated(null)

        Assertions.assertEquals(0, observations.size)
    }

    @Test
    fun `find by updated since finds resources when only updated resources exist and parameter is null`() {
        Assertions.assertTrue(baseCollectionDAO.getAll().isEmpty())
        val testObservations =
            listOf(
                observation {
                    id of "TestObservation1"
                    extension of
                        listOf(
                            Extension(
                                url = Uri(ExtensionUrls.DATA_UPDATE_TIMESTAMP_URL),
                                value = DynamicValue(DynamicValueType.DATE_TIME, DateTime("2022-01-01T00:00:00")),
                            ),
                        )
                },
                observation {
                    id of "TestObservation2"
                    extension of
                        listOf(
                            Extension(
                                url = Uri(ExtensionUrls.DATA_UPDATE_TIMESTAMP_URL),
                                value = DynamicValue(DynamicValueType.DATE_TIME, DateTime("2023-01-01T00:00:00")),
                            ),
                        )
                },
            )
        testObservations.forEach { observation ->
            collection.add(JacksonUtil.writeJsonValue(observation)).execute()
        }

        val observations = baseCollectionDAO.findUpdated(null)

        Assertions.assertEquals(2, observations.size)
    }

    @Test
    fun `find by updated since test finds updated resources with mixed update status and parameter is null`() {
        Assertions.assertTrue(baseCollectionDAO.getAll().isEmpty())
        val testObservations =
            listOf(
                observation {
                    id of "TestObservation1"
                },
                observation {
                    id of "TestObservation2"
                    extension of
                        listOf(
                            Extension(
                                url = Uri(ExtensionUrls.DATA_UPDATE_TIMESTAMP_URL),
                                value = DynamicValue(DynamicValueType.DATE_TIME, DateTime("2022-01-01T00:00:00")),
                            ),
                        )
                },
                observation {
                    id of "TestObservation3"
                    extension of
                        listOf(
                            Extension(
                                url = Uri(ExtensionUrls.DATA_UPDATE_TIMESTAMP_URL),
                                value = DynamicValue(DynamicValueType.DATE_TIME, DateTime("2023-01-01T00:00:00")),
                            ),
                        )
                },
            )

        testObservations.forEach { observation ->
            collection.add(JacksonUtil.writeJsonValue(observation)).execute()
        }

        val observations = baseCollectionDAO.findUpdated(null)

        Assertions.assertEquals(2, observations.size)
    }

    @Test
    fun `find by updated since finds no resources with none updated with non-null parameter`() {
        Assertions.assertTrue(baseCollectionDAO.getAll().isEmpty())
        val testObservations =
            listOf(
                observation {
                    id of "TestObservation1"
                },
                observation {
                    id of "TestObservation2"
                },
            )
        testObservations.forEach { observation ->
            collection.add(JacksonUtil.writeJsonValue(observation)).execute()
        }

        val observations = baseCollectionDAO.findUpdated(ZonedDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC")))

        Assertions.assertEquals(0, observations.size)
    }

    @Test
    fun `find by updated since finds correct resources when only updated resources exist with non-null parameter`() {
        Assertions.assertTrue(baseCollectionDAO.getAll().isEmpty())
        val testObservations =
            listOf(
                observation {
                    id of "TestObservation1"
                    extension of
                        listOf(
                            Extension(
                                url = Uri(ExtensionUrls.DATA_UPDATE_TIMESTAMP_URL),
                                value = DynamicValue(DynamicValueType.DATE_TIME, DateTime("2022-01-01T00:00:00")),
                            ),
                        )
                },
                observation {
                    id of "TestObservation2"
                    extension of
                        listOf(
                            Extension(
                                url = Uri(ExtensionUrls.DATA_UPDATE_TIMESTAMP_URL),
                                value = DynamicValue(DynamicValueType.DATE_TIME, DateTime("2023-01-01T00:00:00")),
                            ),
                        )
                },
            )
        testObservations.forEach { observation ->
            collection.add(JacksonUtil.writeJsonValue(observation)).execute()
        }

        val observations = baseCollectionDAO.findUpdated(ZonedDateTime.of(2022, 6, 1, 0, 0, 0, 0, ZoneId.of("UTC")))

        Assertions.assertEquals(1, observations.size)
        Assertions.assertEquals("TestObservation2", observations[0].id?.value)
    }

    @Test
    fun `find by updated since test finds updated resources with mixed update status with non-null parameter`() {
        Assertions.assertTrue(baseCollectionDAO.getAll().isEmpty())
        val testObservations =
            listOf(
                observation {
                    id of "TestObservation1"
                },
                observation {
                    id of "TestObservation1"
                    extension of
                        listOf(
                            Extension(
                                url = Uri(ExtensionUrls.DATA_UPDATE_TIMESTAMP_URL),
                                value = DynamicValue(DynamicValueType.DATE_TIME, DateTime("2022-01-01T00:00:00")),
                            ),
                        )
                },
                observation {
                    id of "TestObservation2"
                    extension of
                        listOf(
                            Extension(
                                url = Uri(ExtensionUrls.DATA_UPDATE_TIMESTAMP_URL),
                                value = DynamicValue(DynamicValueType.DATE_TIME, DateTime("2023-01-01T00:00:00")),
                            ),
                        )
                },
            )

        testObservations.forEach { observation ->
            collection.add(JacksonUtil.writeJsonValue(observation)).execute()
        }

        val observations = baseCollectionDAO.findUpdated(ZonedDateTime.of(2022, 6, 1, 0, 0, 0, 0, ZoneId.of("UTC")))

        Assertions.assertEquals(1, observations.size)
        Assertions.assertEquals("TestObservation2", observations[0].id?.value)
    }

    @Test
    fun `insert works`() {
        Assertions.assertTrue(baseCollectionDAO.getAll().isEmpty())
        val testObs1 = observation { id of "TestObservation1" }

        baseCollectionDAO.insert(testObs1)
        Assertions.assertEquals(1, baseCollectionDAO.getAll().size)
    }

    @Test
    fun `update works`() {
        val testObs = observation { id of Id("TestObservation1") }
        collection.add(JacksonUtil.writeJsonValue(testObs)).execute()
        Assertions.assertEquals(1, baseCollectionDAO.getAll().size)

        val expectedExtensionUri = "https://projectronin.io/fhir/StructureDefinition/DataUpdateTimestamp"
        val testObsUpdate =
            observation {
                id of Id("TestObservation1")
                meta of Meta(tag = listOf(Coding(display = FHIRString("vital"))))
            }
        baseCollectionDAO.update(testObsUpdate)
        val result = baseCollectionDAO.getAll()
        Assertions.assertEquals(1, result.size)
        Assertions.assertEquals(testObsUpdate.id, result.first().id)
        Assertions.assertEquals(testObsUpdate.meta, result.first().meta)
        Assertions.assertNotNull(result.first().extension)
        Assertions.assertEquals(expectedExtensionUri, result.first().extension.first().url?.value)
        Assertions.assertEquals(DynamicValueType.DATE_TIME, result.first().extension.first().value?.type)
        Assertions.assertInstanceOf(DateTime::class.java, result.first().extension.first().value?.value)
    }

    @Test
    fun `delete works`() {
        val testObs1 = observation { id of "TestObservation1" }
        collection.add(JacksonUtil.writeJsonValue(testObs1)).execute()
        Assertions.assertEquals(1, baseCollectionDAO.getAll().size)

        baseCollectionDAO.delete("TestObservation1")
        Assertions.assertEquals(0, baseCollectionDAO.getAll().size)
    }
}
