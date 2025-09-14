package com.sriniketh.utils

import io.github.oshai.kotlinlogging.KotlinLogging

interface EnvConfigProvider {
    fun wikiName(): String
    fun apiUrl(): String
}

class EnvConfigProviderImpl : EnvConfigProvider {

    private val logger = KotlinLogging.logger {}

    override fun wikiName(): String = requireEnv("WIKI_NAME")
    override fun apiUrl(): String = requireEnv("WIKI_API_URL")

    private fun requireEnv(key: String): String =
        runCatching { System.getenv(key) }.getOrElse {
            logger.error { "Environment variable $key is not configured" }
            throw IllegalStateException("Environment variable $key is required")
        }
}
