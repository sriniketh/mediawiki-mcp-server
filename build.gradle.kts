plugins {
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
}

group = "com.sriniketh"
version = "0.1.0"

buildConfig {
    buildConfigField("String", "APP_VERSION", "\"$version\"")
}

dependencies {
    implementation(libs.jsoup)
    implementation(libs.kotlin.logging)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.logback.classic)
    implementation(libs.mcp.sdk)

    testImplementation(libs.kotlin.test.junit)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("com.sriniketh.ApplicationKt")
}
