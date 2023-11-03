package com.projectronin.clinical.trial.server.transform

import com.projectronin.interop.datalake.oci.client.OCIClient
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

internal class DataDictionaryServiceTest {

    private val registryCSV = File(DataDictionaryServiceTest::class.java.getResource("/transform/registryExample.csv")!!.file).readText()
    private val valueSetJSON1 =
        File(DataDictionaryServiceTest::class.java.getResource("/transform/valueSetExample1.json")!!.file).readText()
    private val valueSetJSON2 =
        File(DataDictionaryServiceTest::class.java.getResource("/transform/valueSetExample2.json")!!.file).readText()
    private val ociClient = mockk<OCIClient> {
        every { getObjectFromINFX("Registries/v1/data dictionary/prod/38efb390-497f-4b49-9619-a45d33048a3a") } returns registryCSV
        every { getObjectFromINFX("ValueSets/v2/published/DataDictionary1") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/DataDictionary2") } returns valueSetJSON2
    }
    private val service = DataDictionaryService(ociClient)

    @Test
    fun `csv and value set extraction works`() {
        service.init()
        val test1 = service.getDataDictionaryByCode("http://loinc.org", "37581-6")!!
        assertTrue(test1.contains("DataDictionary1"))
        assertTrue(test1.contains("DataDictionary2"))
        val test2 = service.getDataDictionaryByCode("http://loinc.org", "84302-9")!!
        assertTrue(test2.contains("DataDictionary1"))
        assertTrue(test2.contains("DataDictionary2"))
        val test3 = service.getDataDictionaryByCode("http://loinc.org", "36188-1")!!
        assertTrue(test3.contains("DataDictionary1"))
        assertFalse(test3.contains("DataDictionary2"))
    }
}
