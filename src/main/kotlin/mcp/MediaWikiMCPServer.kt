package com.sriniketh.mcp

import com.sriniketh.utils.cleanHtmlContent
import com.sriniketh.client.MediaWikiClient
import com.sriniketh.client.MediaWikiClientImpl
import com.sriniketh.model.GetPageContentInput
import com.sriniketh.model.GetPageContentOutput
import com.sriniketh.model.SearchWikiInput
import com.sriniketh.model.SearchWikiOutput
import com.sriniketh.mcp.tools.GetPageContentTool
import com.sriniketh.mcp.tools.MediaWikiTool
import com.sriniketh.mcp.tools.SearchTool
import com.sriniketh.utils.BuildConfigProvider
import com.sriniketh.utils.BuildConfigProviderImpl
import com.sriniketh.utils.EnvConfigProvider
import com.sriniketh.utils.EnvConfigProviderImpl
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.Url
import io.ktor.utils.io.streams.asInput
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.shared.Transport
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.put

class MediaWikiMCPServer(
    private val wikiClient: MediaWikiClient = MediaWikiClientImpl(),
    private val transport: Transport = stdioServerTransport(),
    private val searchTool: MediaWikiTool = SearchTool(),
    private val getPageContentTool: MediaWikiTool = GetPageContentTool(),
    private val envConfigProvider: EnvConfigProvider = EnvConfigProviderImpl(),
    buildConfigProvider: BuildConfigProvider = BuildConfigProviderImpl()
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val server = Server(
        Implementation(
            name = "${envConfigProvider.wikiName()} MCP Server",
            version = buildConfigProvider.appVersion()
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
                    val apiUrl = Url(envConfigProvider.apiUrl())
                    val result = GetPageContentOutput(
                        title = pageContent.title,
                        content = cleanContent,
                        url = "${apiUrl.protocol}://${apiUrl.host}/${pageContent.title.replace(" ", "_")}",
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
        server.connect(transport)
        logger.info { "MediaWiki MCP Server connected and ready to handle requests" }
    }
}

private fun stdioServerTransport(): StdioServerTransport = StdioServerTransport(
    System.`in`.asInput(),
    System.out.asSink().buffered()
)
