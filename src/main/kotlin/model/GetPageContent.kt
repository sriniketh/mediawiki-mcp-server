package com.sriniketh.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPageContentInput(
    @SerialName("page_title")
    val title: String
)

@Serializable
data class GetPageContentOutput(
    val title: String,
    val content: String,
    val url: String,
    @SerialName("word_count")
    val wordCount: Int
)

@Serializable
data class GetPageContentParsedResult(
    val parse: PageContent
)

@Serializable
data class PageContent(
    val title: String,
    val text: Map<String, String>
)
