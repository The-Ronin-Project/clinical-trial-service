package com.projectronin.clinical.trial.server.transform

import com.projectronin.interop.datalake.oci.client.OCIClient
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
        every { getObjectFromINFX("ValueSets/v2/published/10f8c49a-635b-4928-aee6-f6e47c2e7c50") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/daf6a5fc-5705-400b-abd0-852e060c9325") } returns valueSetJSON2
        every { getObjectFromINFX("ValueSets/v2/published/474a2e8e-14ca-46fb-a955-04c00f9dab7d") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/7e11ab3e-54f9-447f-8a29-c7dbdf3aff73") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/bf688200-19eb-441f-b8b8-cfcd7f2c000e") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/a68a0a01-c6ba-4de0-a9cf-fcccf9b1eeee") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/ca5545b1-64d9-4476-9206-8a9b1ba603b9") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/53abdce0-9e4d-4fa3-97b0-426775ceb70c") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/f1ef1444-1c66-477a-9221-9a1590da8a34") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/1a056f49-0e24-4eca-93bf-32cf3678141d") } returns valueSetJSON1
    }
    private val service = DataDictionaryService(ociClient)

    @Test
    fun `csv and value set extraction works`() {
        service.init()
        val test1 = service.getDataDictionaryByCode("http://loinc.org", "37581-6")!!
        assertNotNull(test1.find { it.valueSetUuid == "10f8c49a-635b-4928-aee6-f6e47c2e7c50" })
        assertNotNull(test1.find { it.valueSetUuid == "daf6a5fc-5705-400b-abd0-852e060c9325" })
        val test2 = service.getDataDictionaryByCode("http://loinc.org", "84302-9")!!
        assertNotNull(test2.find { it.valueSetUuid == "10f8c49a-635b-4928-aee6-f6e47c2e7c50" })
        assertNotNull(test2.find { it.valueSetUuid == "daf6a5fc-5705-400b-abd0-852e060c9325" })
        val test3 = service.getDataDictionaryByCode("http://loinc.org", "36188-1")!!
        assertNotNull(test3.find { it.valueSetUuid == "10f8c49a-635b-4928-aee6-f6e47c2e7c50" })
        assertNull(test3.find { it.valueSetUuid == "daf6a5fc-5705-400b-abd0-852e060c9325" })
    }

    @Test
    fun `value set version extraction works`() {
        service.init()
        val test1 = service.getValueSetUuidVersionByDisplay("Birth Date")
        assertEquals(test1!!.first, "10f8c49a-635b-4928-aee6-f6e47c2e7c50")
        assertEquals(test1.second, "1")
    }

    @Test
    fun `retry works`() {
        every { ociClient.getObjectFromINFX("Registries/v1/data dictionary/prod/38efb390-497f-4b49-9619-a45d33048a3a") } throws Exception()
        service.load(5)
        every { ociClient.getObjectFromINFX("Registries/v1/data dictionary/prod/38efb390-497f-4b49-9619-a45d33048a3a") } returns registryCSV
    }
}
