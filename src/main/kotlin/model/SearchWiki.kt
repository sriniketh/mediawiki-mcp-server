package com.sriniketh.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchWikiInput(
    val query: String,
    val limit: Int = 5
)

@Serializable
data class SearchWikiOutput(
    val results: List<WikiPage>,
    val totalResults: Int,
    val query: String
)

@Serializable
data class SearchWikiResult(
    val query: SearchWikiQuery
)

@Serializable
data class SearchWikiQuery(
    val search: List<WikiPage>
)

@Serializable
data class WikiPage(
    val title: String,
    val snippet: String? = null,
    @SerialName("sectiontitle")
    val sectionTitle: String? = null,
    @SerialName("categorysnippet")
    val categorySnippet: String? = null,
    @SerialName("wordcount")
    val wordCount: Int? = null
)
