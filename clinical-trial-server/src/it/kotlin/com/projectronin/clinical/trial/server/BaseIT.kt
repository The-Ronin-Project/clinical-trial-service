package com.projectronin.clinical.trial.server

import com.projectronin.interop.common.http.spring.HttpSpringConfig
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File

abstract class BaseIT {
    companion object {
        val docker =
            DockerComposeContainer(File(BaseIT::class.java.getResource("/docker-compose-it.yaml")!!.file))
                .waitingFor("clinical-trial-server", Wait.forLogMessage(".*Started ClinicalTrialServerKt.*", 1))
                .start()
    }

    // protected val database = Database.connect(url = "jdbc:mysql://springuser:ThePassword@localhost:3306/validation-db")

    protected val serverUrl = "http://localhost:8080"
    protected val httpClient = HttpSpringConfig().getHttpClient()
}
