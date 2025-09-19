plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "mediawiki_mcp_server"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
