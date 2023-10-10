
plugins {
    `maven-publish`
    alias(libs.plugins.interop.junit)
    alias(libs.plugins.interop.spring.boot)
}

dependencies {
    implementation(platform(libs.spring.boot.parent))

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
}
