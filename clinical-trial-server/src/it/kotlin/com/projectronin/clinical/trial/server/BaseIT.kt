package com.projectronin.clinical.trial.server

import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.projectronin.clinical.trial.client.ClinicalTrialClient
import com.projectronin.clinical.trial.client.auth.ClinicalTrialAuthenticationService
import com.projectronin.clinical.trial.server.dataauthority.ClinicalTrialDataAuthorityDatabase
import com.projectronin.clinical.trial.server.dataauthority.XDevConfig
import com.projectronin.interop.common.http.spring.HttpSpringConfig
import com.projectronin.test.jwt.generateRandomRsa
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.ktorm.database.Database
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File

abstract class BaseIT {
    companion object {
        val docker =
            DockerComposeContainer(File(BaseIT::class.java.getResource("/docker-compose-it.yaml")!!.file))
                .waitingFor("clinical-trial-server", Wait.forLogMessage(".*Started ClinicalTrialServerKt.*", 1))
                .start()

        val key = generateRandomRsa()
        const val ISSUER = "http://ronin-auth:8080"

        @BeforeAll
        @JvmStatic
        fun setup() {
            configureFor(9999)
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
        }
    }

    protected val database = Database.connect(url = "jdbc:mysql://springuser:ThePassword@localhost:3306/clinical-trial-db")

    protected val ctdaDatabase =
        ClinicalTrialDataAuthorityDatabase(
            XDevConfig(
                "localhost",
                "33060",
                "clinical-trial-db",
                "springuser",
                "ThePassword",
            ),
        )

    protected val httpClient = HttpSpringConfig().getHttpClient()
    private val authUrl = "http://localhost:8081/clinical-trial/token"
    private val audience = "https://clinical-trial-service.dev.projectronin.io"
    private val authClientId = "id"
    private val authClientSecret = "secret"
    protected val authenticationService =
        ClinicalTrialAuthenticationService(
            httpClient,
            authUrl,
            audience,
            authClientId,
            authClientSecret,
            false,
        )

    protected val serverUrl = "http://localhost:8080"
    protected val client = ClinicalTrialClient(serverUrl, httpClient, authenticationService)
}
