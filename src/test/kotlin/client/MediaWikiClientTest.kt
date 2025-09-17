package client

import com.sriniketh.client.MediaWikiClientImpl
import com.sriniketh.model.GetPageContentParsedResult
import com.sriniketh.model.PageContent
import com.sriniketh.model.SearchWikiQuery
import com.sriniketh.model.SearchWikiResult
import com.sriniketh.utils.BuildConfigProvider
import com.sriniketh.utils.EnvConfigProvider
import fakes.FakeBuildConfigProvider
import fakes.FakeEnvConfigProvider
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.ContentType
import io.ktor.http.headersOf
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MediaWikiClientTest {

    private val envConfigProvider: EnvConfigProvider = FakeEnvConfigProvider()
    private val buildConfigProvider: BuildConfigProvider = FakeBuildConfigProvider()
    private val mockEngine = MediaWikiMockEngine()
    private val client = MediaWikiClientImpl(mockEngine.getMockEngine(), envConfigProvider, buildConfigProvider)

    @Test
    fun `handleSearch returns list of WikiPage when request is successful`() = runTest {
        val result = client.handleSearch("test", 10)
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Title1", result.getOrNull()?.get(0)?.title)
        assertEquals("Title2", result.getOrNull()?.get(1)?.title)
    }

    @Test
    fun `handleSearch returns failure when request fails`() = runTest {
        mockEngine.shouldRequestFail = true
        val result = client.handleSearch("test", 10)
        assertTrue(result.isFailure)
    }

    @Test
    fun `getPageContent returns PageContent when request is successful`() = runTest {
        val result = client.handleGetPageContent("Title1")
        assertTrue(result.isSuccess)
        val pageContent = result.getOrNull()
        assertEquals("Title1", pageContent?.title)
        assertEquals("<p>Sample HTML content</p>", pageContent?.text?.get("*"))
    }

    @Test
    fun `getPageContent returns failure when request fails`() = runTest {
        mockEngine.shouldRequestFail = true
        val result = client.handleGetPageContent("Title1")
        assertTrue(result.isFailure)
    }

    private class MediaWikiMockEngine {
        var shouldRequestFail: Boolean = false
        fun getMockEngine() = MockEngine { request ->
            val headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            if (shouldRequestFail) {
                respond(content = "", status = HttpStatusCode.InternalServerError)
            } else if (request.url.parameters["action"] == "query") {
                respond(
                    content = Json.encodeToString(
                        SearchWikiResult(
                            query = SearchWikiQuery(
                                search = listOf(
                                    com.sriniketh.model.WikiPage(title = "Title1"),
                                    com.sriniketh.model.WikiPage(title = "Title2")
                                )
                            )
                        )
                    ),
                    status = HttpStatusCode.OK,
                    headers = headers
                )
            } else if (request.url.parameters["action"] == "parse") {
                respond(
                    content = Json.encodeToString(
                        GetPageContentParsedResult(
                            parse = PageContent(
                                title = "Title1",
                                text = mapOf("*" to "<p>Sample HTML content</p>")
                            )
                        )
                    ),
                    status = HttpStatusCode.OK,
                    headers = headers
                )
            } else {
                respond(content = "")
            }
        }
    }
}
