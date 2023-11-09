package com.projectronin.clinical.trial.server

import com.fasterxml.jackson.databind.JsonNode
import com.projectronin.interop.common.auth.Authentication
import com.projectronin.interop.common.http.spring.HttpSpringConfig
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import kotlinx.coroutines.runBlocking
import org.ktorm.database.Database
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File
import java.time.Instant

abstract class BaseIT {
    companion object {
        val docker =
            DockerComposeContainer(File(BaseIT::class.java.getResource("/docker-compose-it.yaml")!!.file))
                .waitingFor("clinical-trial-server", Wait.forLogMessage(".*Started ClinicalTrialServerKt.*", 1))
                .start()
    }

    protected val database = Database.connect(url = "jdbc:mysql://springuser:ThePassword@localhost:3306/clinical-trial-db")

    protected val serverUrl = "http://localhost:8080"
    protected val httpClient = HttpSpringConfig().getHttpClient()
    protected val authUrl = "http://localhost:8081/clinical-trial/token"
    data class FormBasedAuthentication(override val accessToken: String) : Authentication {
        override val tokenType: String = "Bearer"
        override val expiresAt: Instant? = null
        override val refreshToken: String? = null
        override val scope: String? = null
    }
    protected fun getAuth(): Authentication = runBlocking {
        val json: JsonNode = httpClient.submitForm(
            url = authUrl,
            formParameters = Parameters.build {
                append("grant_type", "client_credentials")
                append("client_id", "id")
                append("client_secret", "secret")
            }
        ).body()
        val accessToken = json.get("access_token").asText()
        FormBasedAuthentication(accessToken)
    }
}
