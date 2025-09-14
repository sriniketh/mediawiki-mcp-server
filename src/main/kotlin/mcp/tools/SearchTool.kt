package com.sriniketh.mcp.tools

import com.sriniketh.utils.EnvConfigProvider
import com.sriniketh.utils.EnvConfigProviderImpl
import io.modelcontextprotocol.kotlin.sdk.Tool
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
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("query", buildJsonObject {
                        put("type", "string")
                        put("description", "Search query for the ${envConfigProvider.wikiName()}")
                    })
                    put("limit", buildJsonObject {
                        put("type", "integer")
                        put("description", "Maximum number of results to return (default: 5)")
                        put("default", 5)
                    })
                })
                put("required", buildJsonArray {
                    add("query")
                })
            }
        ),
        outputSchema = Tool.Output(
            properties = buildJsonObject {
                put("oneOf", buildJsonArray {
                    add(buildJsonObject {
                        put("type", "object")
                        put("properties", buildJsonObject {
                            put("results", buildJsonObject {
                                put("type", "array")
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
                                        add("snippet")
                                        add("sectiontitle")
                                        add("categorysnippet")
                                        add("wordcount")
                                    })
                                })
                            })
                        })
                        put("required", buildJsonArray {
                            add("results")
                        })
                    })
                    add(buildJsonObject {
                        put("type", "object")
                        put("properties", buildJsonObject {
                            put("error", buildJsonObject {
                                put("type", "string")
                                put(
                                    "description",
                                    "Error message in case of failures while searching ${envConfigProvider.wikiName()}"
                                )
                            })
                        })
                        put("required", buildJsonArray {
                            add("error")
                        })
                    })
                })
            }
        ),
        annotations = null
    )
}
