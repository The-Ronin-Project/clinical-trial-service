plugins {
    alias(libs.plugins.interop.junit)
    alias(libs.plugins.interop.spring.framework)
}

dependencies {
    implementation(platform(libs.spring.boot.parent))

    api(project(":clinical-trial-models"))
    implementation(libs.bundles.jackson)
    implementation(libs.bundles.ktor)
    implementation(libs.interop.commonHttp)
    implementation(libs.interop.commonJackson)

    testImplementation(libs.mockk)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.mockwebserver)
}
