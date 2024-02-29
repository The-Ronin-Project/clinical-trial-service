plugins {
    `maven-publish`
    alias(libs.plugins.interop.docker.integration)
    alias(libs.plugins.interop.junit)
    alias(libs.plugins.interop.spring.boot)
}

ext["kafka.version"] = "3.2.1"
dependencies {
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.13.0")
    implementation(project(":clinical-trial-models"))
    implementation(libs.bundles.ktor)
    implementation(libs.interop.fhir)
    implementation(libs.protobuf.java)
    implementation(libs.interop.common)
    implementation(libs.event.interop.resource.request)
    implementation(libs.interop.commonHttp)
    implementation(libs.interop.commonJackson)
    implementation(libs.interop.commonKtorm)
    implementation(libs.interop.datalake)
    implementation(libs.spring.kafka)
    implementation(ronincommon.kafka)
    implementation(productcommon.product.spring.audit)

    implementation(project(":clinical-trial-liquibase"))
    testImplementation(libs.interop.commonTestDb)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation("org.testcontainers:mysql")
    testImplementation(libs.bundles.data.generators)
    testImplementation(libs.interop.fhirGenerators)
    testImplementation(libs.interop.rcdm.fhir.roninGenerators)
    testImplementation(libs.ktor.client.mock)
    itImplementation(project(":clinical-trial-models"))
    itImplementation(project(":clinical-trial-client"))
    itImplementation(libs.interop.commonHttp)
    itImplementation(ronincommon.jwt.auth.test)
    itImplementation(ronincommon.wiremock)
    itImplementation(libs.interop.fhir)
    itImplementation(libs.interop.rcdm.fhir.roninGenerators)

    itImplementation(libs.spring.kafka)
    itImplementation(ronincommon.kafka)

    itImplementation(libs.ktorm.core)
    itImplementation(libs.ronin.test.data.generator)
    itImplementation(libs.interop.fhirGenerators)
    itImplementation(libs.event.interop.resource.request)
    itImplementation(platform(libs.testcontainers.bom))
    itImplementation("org.testcontainers:mysql")
    itImplementation(project)

    implementation(productcommon.product.spring.web.starter)
    implementation(productcommon.bundles.spring.data)
    implementation(productcommon.okhttp)
    implementation(productcommon.kotlin.coroutines.core)
    implementation(productcommon.kotlinlogging)
    implementation(productcommon.mysql.connector)

    testImplementation(productcommon.bundles.spring.test) {
        exclude(module = "mockito-core")
    }
    testImplementation(productcommon.bundles.testcontainers)
    testImplementation(productcommon.product.spring.jwt.auth.testutils)
    testImplementation(productcommon.kotlinx.coroutines.test)
}
