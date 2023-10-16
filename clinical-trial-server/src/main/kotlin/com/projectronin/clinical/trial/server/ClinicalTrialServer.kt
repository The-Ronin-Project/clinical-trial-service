package com.projectronin.clinical.trial.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main Spring Boot application for the Clinical Trial Server
 */

@SpringBootApplication
class ClinicalTrialServer

fun main(args: Array<String>) {
    runApplication<ClinicalTrialServer>(*args)
}
