package com.sriniketh.tools

import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GetPageContentTool : MediaWikiTool {

    override fun createTool(): Tool = Tool(
        name = "get_page_content",
        title = "Get Wiki Page Content",
        description = "Get the full content of a specific Stardew Valley wiki page",
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                put("page_title", buildJsonObject {
                    put("type", "string")
                    put("description", "The exact title of the wiki page to retrieve")
                })
            },
            required = listOf("page_title")
        ),
        outputSchema = Tool.Output(
            properties = buildJsonObject {
                put("title", buildJsonObject {
                    put("type", "string")
                    put("description", "The title of the wiki page")
                })
                put("content", buildJsonObject {
                    put("type", "string")
                    put("description", "The full text content of the page (HTML stripped)")
                })
                put("url", buildJsonObject {
                    put("type", "string")
                    put("format", "uri")
                    put("description", "Direct URL to the wiki page")
                })
                put("word_count", buildJsonObject {
                    put("type", "integer")
                    put("description", "Number of words in the content")
                })
            },
            required = listOf("title", "content", "url", "word_count")
        ),
        annotations = null
    )
}
