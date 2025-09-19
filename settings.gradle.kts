plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
rootProject.name = "mediawiki_mcp_server"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
