package com.projectronin.clinical.trial.server.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.ZoneId
import java.time.ZonedDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class FhirDatesTest {
    @Test
    fun `parseFhirDateTime test`() {
        var date = ZonedDateTime.of(2020, 1, 1, 1, 23, 45, 0, ZoneId.of("UTC"))
        Assertions.assertEquals(date, parseFhirDateTime("2020-01-01T01:23:45"))

        date = ZonedDateTime.of(2020, 1, 1, 1, 23, 0, 0, ZoneId.of("UTC"))
        Assertions.assertEquals(date, parseFhirDateTime("2020-01-01T01:23:00"))

        date = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
        Assertions.assertEquals(date, parseFhirDateTime("2020-01-01"))

        date = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
        Assertions.assertEquals(date, parseFhirDateTime("2020-01"))

        date = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"))
        Assertions.assertEquals(date, parseFhirDateTime("2020"))

        date = ZonedDateTime.of(2020, 1, 1, 1, 23, 45, 0, ZoneId.of("-05:00"))
        Assertions.assertEquals(date, parseFhirDateTime("2020-01-01T01:23:45-05:00"))

        date = ZonedDateTime.of(2020, 1, 1, 1, 23, 45, 0, ZoneId.of("Z"))
        Assertions.assertEquals(date, parseFhirDateTime("2020-01-01T01:23:45Z"))

        Assertions.assertNull(parseFhirDateTime("2022-0123"))
    }
}
