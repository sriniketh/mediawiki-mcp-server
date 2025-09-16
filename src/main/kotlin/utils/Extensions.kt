package com.sriniketh.utils

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.Jsoup

private val logger = KotlinLogging.logger {}

fun String.cleanHtmlContent(): String {
    if (this.isBlank()) {
        logger.warn { "Html content is empty" }
        return ""
    }

    return Jsoup.parse(this)
        .select("body")
        .text()
        .replace("\\s+".toRegex(), " ")
        .trim()
}
