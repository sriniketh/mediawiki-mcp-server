package com.sriniketh.tools

import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SearchTool : MediaWikiTool {

    override fun createTool(): Tool = Tool(
        name = "search_wiki",
        title = "Search Stardew Valley Wiki",
        description = "Search the Stardew Valley wiki for information about items, characters, locations, and gameplay mechanics",
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                put("type", "object")
                put("properties", buildJsonObject {
                    put("query", buildJsonObject {
                        put("type", "string")
                        put("description", "Search query for the Stardew Valley wiki")
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
                                        })
                                        put("snippet", buildJsonObject {
                                            put("type", "string")
                                        })
                                        put("sectiontitle", buildJsonObject {
                                            put("type", "string")
                                        })
                                        put("categorysnippet", buildJsonObject {
                                            put("type", "string")
                                        })
                                        put("wordcount", buildJsonObject {
                                            put("type", "integer")
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
                                put("description", "Error message")
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
