package com.projectronin.clinical.trial.server.util

import com.projectronin.interop.fhir.r4.datatype.Period
import com.projectronin.interop.fhir.r4.datatype.primitive.DateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField

val possibleFormats =
    listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm",
        "yyyy-MM-dd",
        "yyyy-MM",
        "yyyy",
    )

val formatter =
    DateTimeFormatterBuilder()
        .append(DateTimeFormatter.ofPattern(possibleFormats.joinToString(separator = "") { "[$it]" }))
        .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
        .toFormatter()

fun parseFhirDateTime(fhirDateTime: String): ZonedDateTime? {
    return try {
        ZonedDateTime.parse(fhirDateTime, formatter)
    } catch (e: DateTimeParseException) {
        try {
            val dateTime = LocalDateTime.parse(fhirDateTime, formatter)
            ZonedDateTime.of(dateTime, ZoneId.of("UTC"))
        } catch (e: DateTimeParseException) {
            try {
                val date = LocalDate.parse(fhirDateTime, formatter)
                ZonedDateTime.of(date.atStartOfDay(), ZoneId.of("UTC"))
            } catch (e: Exception) {
                null
            }
        }
    }
}

fun DateTime.getEffectiveDateTime(): ZonedDateTime? {
    return this.value?.let { parseFhirDateTime(it) }
}

fun Period.getEffectivePeriod(): Pair<ZonedDateTime?, ZonedDateTime?> {
    val start = this.start?.let { start -> start.value?.let { parseFhirDateTime(it) } }
    val end = this.end?.let { end -> end.value?.let { parseFhirDateTime(it) } }
    return Pair(start, end)
}
