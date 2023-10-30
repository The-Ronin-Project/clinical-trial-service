
plugins {
    `maven-publish`
    alias(libs.plugins.interop.docker.integration)
    alias(libs.plugins.interop.junit)
    alias(libs.plugins.interop.spring.boot)
}

dependencies {
    implementation(platform(libs.spring.boot.parent))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    implementation(libs.mysql.connector.java)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.interop.fhir)
    implementation(libs.protobuf.java)
    implementation(libs.interop.common)
    implementation(libs.interop.commonJackson)
    implementation(libs.interop.commonKtorm)
    implementation(libs.ronin.test.data.generator)
    implementation(libs.spring.boot.kafka)
    implementation(libs.ronin.kafka)

    runtimeOnly(project(":clinical-trial-liquibase"))
    runtimeOnly(libs.liquibase.core)
    runtimeOnly(libs.mysql.connector.java)

    testImplementation(libs.interop.commonTestDb)
    testImplementation(libs.mockk)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:mysql")
    testImplementation(libs.interop.fhirGenerators)

    itImplementation(libs.interop.commonHttp)
    itImplementation(libs.interop.fhir)

    itImplementation(libs.ronin.kafka)
    itImplementation(libs.ktorm.core)
    itImplementation(libs.ronin.test.data.generator)
    itImplementation(libs.interop.fhirGenerators)

    itImplementation(platform(libs.testcontainers.bom))
    itImplementation("org.testcontainers:mysql")
    itImplementation(project)
}
