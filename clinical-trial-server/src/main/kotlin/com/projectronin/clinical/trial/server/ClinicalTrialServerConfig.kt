package com.projectronin.clinical.trial.server

import com.projectronin.clinical.trial.server.dataauthority.XDevConfig
import com.projectronin.interop.common.http.spring.HttpSpringConfig
import org.ktorm.database.Database
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import javax.sql.DataSource

@Configuration
@Import(HttpSpringConfig::class)
class ClinicalTrialServerConfig {

    /**
     * The database!
     */
    @Bean
    fun database(dataSource: DataSource): Database = Database.connectWithSpringSupport(dataSource)

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
