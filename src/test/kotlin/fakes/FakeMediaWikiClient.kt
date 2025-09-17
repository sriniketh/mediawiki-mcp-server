package fakes

import com.sriniketh.client.MediaWikiClient
import com.sriniketh.model.PageContent
import com.sriniketh.model.WikiPage

class FakeMediaWikiClient : MediaWikiClient {

    private var searchResult: Result<List<WikiPage>>? = null
    private var pageContentResult: Result<PageContent>? = null

    fun setSearchResults(result: Result<List<WikiPage>>) {
        searchResult = result
    }

    fun setPageContentResult(result: Result<PageContent>) {
        pageContentResult = result
    }

    override suspend fun handleSearch(query: String, limit: Int): Result<List<WikiPage>> {
        return searchResult ?: Result.success(
            listOf(
                WikiPage(title = "Default Test Page", snippet = "Default test snippet")
            )
        )
    }

    override suspend fun handleGetPageContent(pageTitle: String): Result<PageContent> {
        return pageContentResult ?: Result.success(
            PageContent(
                title = pageTitle,
                text = mapOf("*" to "Default fake content for $pageTitle.")
            )
        )
    }
}
