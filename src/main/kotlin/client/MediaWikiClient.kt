package com.sriniketh.client

import com.sriniketh.model.GetPageContentParsedResult
import com.sriniketh.model.PageContent
import com.sriniketh.model.SearchWikiResult
import com.sriniketh.model.WikiPage
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class MediaWikiClient {

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val BASE_URL = "https://stardewvalleywiki.com/mediawiki/api.php"
    }

    private val client: HttpClient = HttpClient {
        defaultRequest {
            url(BASE_URL)
            headers {
                append("User-Agent", "StardewValleyWikiMCP/0.1.0")
                append("Accept", "application/json")
            }
            contentType(ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                }
            )
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    suspend fun handleSearch(query: String, limit: Int): Result<List<WikiPage>> {
        logger.info { "Starting search for query: '$query' with limit: $limit" }
        return runCatching {
            client.get {
                parameter("action", "query")
                parameter("list", "search")
                parameter("srsearch", query)
                parameter("srlimit", limit.toString())
                parameter("srwhat", "text")
                parameter("srprop", "snippet|wordcount|sectiontitle|categorysnippet")
                parameter("format", "json")
            }.body<SearchWikiResult>()
        }.map { response ->
            logger.info { "Search returned ${response.query.search.size} results for query: '$query'" }
            response.query.search
        }.onFailure { error ->
            logger.error(error) { "Search failed for query: '$query', error: ${error.message}" }
        }
    }

    suspend fun handleGetPageContent(pageTitle: String): Result<PageContent> {
        logger.info { "Fetching content for page title: '$pageTitle'" }
        return runCatching {
            client.get {
                parameter("action", "parse")
                parameter("page", pageTitle)
                parameter("prop", "text")
                parameter("format", "json")
                parameter("disablelimitreport", true)
                parameter("disableeditsection", true)
            }.body<GetPageContentParsedResult>()
        }.map { response ->
            logger.info { "Successfully fetched content for page title: '$pageTitle'" }
            response.parse
        }.onFailure { error ->
            logger.error(error) { "Failed to fetch content for page title: '$pageTitle', error: ${error.message}" }
        }
    }
}
