package com.sriniketh

import com.sriniketh.client.MediaWikiClient
import com.sriniketh.model.GetPageContentInput
import com.sriniketh.model.GetPageContentOutput
import com.sriniketh.model.SearchWikiInput
import com.sriniketh.model.SearchWikiOutput
import com.sriniketh.tools.GetPageContentTool
import com.sriniketh.tools.MediaWikiTool
import com.sriniketh.tools.SearchTool
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.utils.io.streams.asInput
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

class MediaWikiMCPServer(
    private val wikiClient: MediaWikiClient = MediaWikiClient(),
    private val searchTool: MediaWikiTool = SearchTool(),
    private val getPageContentTool: MediaWikiTool = GetPageContentTool()
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val server = Server(
        Implementation(
            name = "Stardew Valley Wiki MCP Server",
            version = "0.1.0"
        ),
        ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true)
            )
        )
    )

    suspend fun start() {
        logger.info { "Starting MediaWiki MCP Server..." }

        server.addTool(searchTool.createTool()) { request ->
            val input = Json.decodeFromJsonElement<SearchWikiInput>(request.arguments)
            logger.info { "Received search tool request for query: '${input.query}' with limit: ${input.limit}" }

            wikiClient.handleSearch(input.query, input.limit).fold(
                onSuccess = { results ->
                    logger.info { "Successfully processed search request, returning ${results.size} results" }
                    val response = buildJsonObject {
                        val result = SearchWikiOutput(
                            results = results,
                            totalResults = results.size,
                            query = input.query
                        )
                        put("search_results", Json.encodeToJsonElement(result))
                    }
                    CallToolResult(content = listOf(TextContent(Json.encodeToString(response))))
                },
                onFailure = { error ->
                    logger.error(error) { "Failed to process search request for query '${input.query}': ${error.message}" }
                    val response = buildJsonObject {
                        put("error", "Error occurred while searching the wiki: ${error.message}")
                    }
                    CallToolResult(content = listOf(TextContent(Json.encodeToString(response))))
                }
            )
        }

        server.addTool(getPageContentTool.createTool()) { request ->
            val input = Json.decodeFromJsonElement<GetPageContentInput>(request.arguments)
            logger.info { "Received get_page_content tool request for page title: '${input.title}'" }

            wikiClient.handleGetPageContent(input.title).fold(
                onSuccess = { pageContent ->
                    logger.info { "Successfully fetched content for page title: '${input.title}'" }
                    val htmlContent = pageContent.text["*"].orEmpty()
                    val cleanContent = htmlContent.cleanHtmlContent()
                    val result = GetPageContentOutput(
                        title = pageContent.title,
                        content = cleanContent,
                        url = "https://stardewvalleywiki.com/${pageContent.title.replace(" ", "_")}",
                        wordCount = cleanContent.split("\\s+".toRegex()).size
                    )
                    val response = buildJsonObject {
                        put("page_content", Json.encodeToJsonElement(result))
                    }
                    CallToolResult(content = listOf(TextContent(Json.encodeToString(response))))
                },
                onFailure = { error ->
                    logger.error(error) { "Failed to fetch content for page title '${input.title}': ${error.message}" }
                    val response = buildJsonObject {
                        put("error", "Error occurred while fetching page content: ${error.message}")
                    }
                    CallToolResult(content = listOf(TextContent(Json.encodeToString(response))))
                }
            )
        }

        logger.info { "Setting up transport and connecting server..." }
        val transport = StdioServerTransport(
            System.`in`.asInput(),
            System.out.asSink().buffered()
        )
        server.connect(transport)
        logger.info { "MediaWiki MCP Server connected and ready to handle requests" }
    }
}
