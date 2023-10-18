package com.projectronin.clinical.trial.server

import com.projectronin.clinical.trial.server.dataauthority.XDevConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ClinicalTrialServerConfig {

    @Bean
    fun xdevConfig(
        @Value("#{environment.CONEDA_DB_HOST}") host: String,
        @Value("#{environment.CONEDA_DB_PORT}") port: String,
        @Value("#{environment.CONEDA_DB_NAME}") name: String,
        @Value("#{environment.CONEDA_DB_USER}") user: String,
        @Value("#{environment.CONEDA_DB_PASS}") pass: String
    ): XDevConfig =
        XDevConfig(host, port, name, user, pass)
}
