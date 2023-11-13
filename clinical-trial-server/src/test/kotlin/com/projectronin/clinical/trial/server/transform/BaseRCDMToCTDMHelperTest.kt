package com.projectronin.clinical.trial.server.transform

import com.projectronin.interop.datalake.oci.client.OCIClient
import com.projectronin.interop.fhir.r4.datatype.primitive.FHIRString
import com.projectronin.interop.fhir.r4.datatype.primitive.Uri
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class BaseRCDMToCTDMHelperTest {
    private val registryCSV = File(DataDictionaryServiceTest::class.java.getResource("/transform/registryExample.csv")!!.file).readText()
    private val valueSetJSON1 =
        File(DataDictionaryServiceTest::class.java.getResource("/transform/valueSetExample1.json")!!.file).readText()
    private val ociClient = mockk<OCIClient> {
        every { getObjectFromINFX("Registries/v1/data dictionary/prod/38efb390-497f-4b49-9619-a45d33048a3a") } returns registryCSV
        every { getObjectFromINFX("ValueSets/v2/published/10f8c49a-635b-4928-aee6-f6e47c2e7c50") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/daf6a5fc-5705-400b-abd0-852e060c9325") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/474a2e8e-14ca-46fb-a955-04c00f9dab7d") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/7e11ab3e-54f9-447f-8a29-c7dbdf3aff73") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/bf688200-19eb-441f-b8b8-cfcd7f2c000e") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/a68a0a01-c6ba-4de0-a9cf-fcccf9b1eeee") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/ca5545b1-64d9-4476-9206-8a9b1ba603b9") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/53abdce0-9e4d-4fa3-97b0-426775ceb70c") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/f1ef1444-1c66-477a-9221-9a1590da8a34") } returns valueSetJSON1
        every { getObjectFromINFX("ValueSets/v2/published/1a056f49-0e24-4eca-93bf-32cf3678141d") } returns valueSetJSON1
    }
    private val service = BaseRCDMToCTDMHelper(ociClient)

    @Test
    fun `set extensions`() {
        val extensions = setCTDMExtensions("subjectId")
        assertEquals(extensions.size, 2)
        assertEquals(extensions[0].url, Uri("https://projectronin.io/fhir/StructureDefinition/subjectId"))
        assertEquals(extensions[0].value!!.value, FHIRString("subjectId"))
        assertEquals(extensions[1].url, Uri("https://projectronin.io/fhir/StructureDefinition/DataTransformTimestamp"))
    }

    @Test
    fun `test setting Meta`() {
        val meta = service.setProfileMeta("Birth Date")
        assertEquals(meta!!.tag[0].system!!.value, "10f8c49a-635b-4928-aee6-f6e47c2e7c50")
        assertEquals(meta.tag[0].version!!.value, "1")
        assertEquals(meta.tag[0].display!!.value, "Birth Date")
    }
}
