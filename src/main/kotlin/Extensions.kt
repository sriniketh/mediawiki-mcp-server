package com.sriniketh

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.Jsoup

private val logger = KotlinLogging.logger {}

fun String.cleanHtmlContent(): String {
    if (this.isBlank()) return ""

    return try {
        Jsoup.parse(this)
            .select("body")
            .text()
            .replace("\\s+".toRegex(), " ")
            .trim()
    } catch (exception: Exception) {
        logger.warn(exception) { "Failed to parse HTML with Jsoup, falling back to regex cleaning" }
        this.replace("<[^>]*>".toRegex(), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}
