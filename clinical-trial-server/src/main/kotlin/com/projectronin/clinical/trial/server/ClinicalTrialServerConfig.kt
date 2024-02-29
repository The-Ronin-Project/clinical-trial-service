package com.projectronin.clinical.trial.server

import com.projectronin.clinical.trial.server.dataauthority.XDevConfig
import com.projectronin.interop.common.http.spring.HttpSpringConfig
import com.projectronin.interop.datalake.oci.client.OCIClient
import com.projectronin.kafka.spring.config.KafkaConfiguration
import com.projectronin.product.audit.config.AuditConfig
import okhttp3.OkHttpClient
import org.ktorm.database.Database
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

@Configuration
@ComponentScan("com.projectronin.product.common.config")
@Import(AuditConfig::class, KafkaConfiguration::class, OCIClient::class, HttpSpringConfig::class)
class ClinicalTrialServerConfig() {
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
        @Value("#{environment.CONEDA_DB_PASS}") pass: String,
    ): XDevConfig = XDevConfig(host, port, name, user, pass)

    @Bean("okHttpClient")
    open fun getHttpClient(): OkHttpClient {
        val defaultHttpConnectTimeout = 15000L
        val defaultHttpReadTimeout = 15000L
        return OkHttpClient.Builder()
            .connectTimeout(defaultHttpConnectTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(defaultHttpReadTimeout, TimeUnit.MILLISECONDS)
            .build()
    }
}
