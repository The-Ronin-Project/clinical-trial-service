package com.projectronin.clinical.one.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main Spring Boot application for the Clinical One Server
 */

@SpringBootApplication
class ValidationServer

fun main(args: Array<String>) {
    runApplication<ValidationServer>(*args)
}
