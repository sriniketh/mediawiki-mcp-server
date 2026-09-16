package com.sriniketh.mcp.tools

import com.sriniketh.utils.EnvConfigProvider
import com.sriniketh.utils.EnvConfigProviderImpl
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SearchTool(
    private val envConfigProvider: EnvConfigProvider = EnvConfigProviderImpl()
) : MediaWikiTool {

    override fun createTool(): Tool = Tool(
        name = "search_wiki",
        title = "Search ${envConfigProvider.wikiName()}",
        description = "Search the ${envConfigProvider.wikiName()} for information about the topic",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                put("query", buildJsonObject {
                    put("type", "string")
                    put("description", "Search query for the ${envConfigProvider.wikiName()}")
                })
                put("limit", buildJsonObject {
                    put("type", "integer")
                    put("description", "Maximum number of results to return (default: 5)")
                    put("default", 5)
                })
            },
            required = listOf("query")
        ),
        outputSchema = ToolSchema(
            properties = buildJsonObject {
                put("results", buildJsonObject {
                    put("type", "array")
                    put("description", "Search results for the query.")
                    put("items", buildJsonObject {
                        put("type", "object")
                        put("properties", buildJsonObject {
                            put("title", buildJsonObject {
                                put("type", "string")
                                put("description", "Title of the ${envConfigProvider.wikiName()} page")
                            })
                            put("snippet", buildJsonObject {
                                put("type", "string")
                                put("description", "A brief snippet from the page content")
                            })
                            put("sectiontitle", buildJsonObject {
                                put("type", "string")
                                put("description", "Title of the section where the snippet was found")
                            })
                            put("categorysnippet", buildJsonObject {
                                put("type", "string")
                                put("description", "Categories associated with the page")
                            })
                            put("wordcount", buildJsonObject {
                                put("type", "integer")
                                put("description", "Number of words in the page")
                            })
                        })
                        put("required", buildJsonArray {
                            add("title")
                        })
                    })
                })
                put("totalResults", buildJsonObject {
                    put("type", "integer")
                    put("description", "Total number of results found for the query")
                })
                put("query", buildJsonObject {
                    put("type", "string")
                    put("description", "The search query that was executed")
                })
            },
            required = listOf("results", "totalResults", "query")
        ),
        annotations = null
    )
}
