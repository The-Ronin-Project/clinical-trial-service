package com.projectronin.clinical.trial.server.util

private const val RONIN_FHIR_ID_DELIMITER = "-"

fun String.tenantIdFromRoninFhirId(): String {
    val tokens = this.split(RONIN_FHIR_ID_DELIMITER)
    if (tokens.size < 2) throw IllegalStateException("Ronin FHIR Id is not of form \"tenant-fhirid\".")
    return tokens.first()
}
