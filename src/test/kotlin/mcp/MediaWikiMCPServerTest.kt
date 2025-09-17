package mcp

import com.sriniketh.mcp.MediaWikiMCPServer
import com.sriniketh.model.PageContent
import com.sriniketh.model.WikiPage
import fakes.FakeBuildConfigProvider
import fakes.FakeEnvConfigProvider
import fakes.FakeMediaWikiClient
import fakes.FakeMediaWikiTool
import fakes.FakeTransport
import io.modelcontextprotocol.kotlin.sdk.ClientCapabilities
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ListToolsRequest
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.ClientOptions
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MediaWikiMCPServerTest {

    @Test
    fun `server is created with correct tools`() = runTest {
        val (server, client, clientTransport) = createClientServerWithLinkedTransport(
            fakeSearchTool = FakeMediaWikiTool("search_wiki"),
            fakeGetPageContentTool = FakeMediaWikiTool("get_page_content")
        )
        server.start()
        client.connect(clientTransport)
        val tools = client.listTools(
            request = ListToolsRequest()
        )
        assertEquals(2, tools.tools.size)
        val toolNames = tools.tools.map { it.name }
        assertTrue(toolNames.contains("search_wiki"))
        assertTrue(toolNames.contains("get_page_content"))
    }

    @Test
    fun `server responds with successful tool result when client triggers search query and wikiclient returns successful response`() =
        runTest {
            val fakeResponse = listOf(
                WikiPage(
                    title = "Test Page",
                    snippet = "Test snippet about the query"
                ),
                WikiPage(
                    title = "Another Page",
                    snippet = "Another snippet"
                )
            )
            val fakeMediaWikiClient = FakeMediaWikiClient()
            fakeMediaWikiClient.setSearchResults(Result.success(fakeResponse))
            val (server, client, clientTransport) = createClientServerWithLinkedTransport(fakeMediaWikiClient)

            server.start()
            client.connect(clientTransport)
            val result = client.callTool(
                "search_wiki",
                buildJsonObject {
                    put("query", "test query")
                    put("limit", 10)
                }
            )

            assertEquals(1, result!!.content.size)
            val content = result.content[0] as TextContent
            val responseJson = Json.parseToJsonElement(content.text!!).jsonObject
            assertTrue(responseJson.containsKey("search_results"))

            val searchResults = Json.decodeFromJsonElement<JsonObject>(responseJson["search_results"]!!)
            val resultsArray = searchResults["results"]!!.jsonArray
            assertEquals(2, resultsArray.size)
            val firstResult = resultsArray[0].jsonObject
            assertEquals("Test Page", firstResult["title"]!!.jsonPrimitive.content)
            assertEquals("Test snippet about the query", firstResult["snippet"]!!.jsonPrimitive.content)
            val secondResult = resultsArray[1].jsonObject
            assertEquals("Another Page", secondResult["title"]!!.jsonPrimitive.content)
            assertEquals("Another snippet", secondResult["snippet"]!!.jsonPrimitive.content)
            assertEquals("test query", searchResults["query"]!!.jsonPrimitive.content)
            assertEquals(2, searchResults["totalResults"]!!.jsonPrimitive.int)
        }

    @Test
    fun `server responds with failure tool result when client triggers search query but wikiclient returns failure`() =
        runTest {
            val fakeMediaWikiClient = FakeMediaWikiClient()
            fakeMediaWikiClient.setSearchResults(Result.failure(Exception("Search failed")))
            val (server, client, clientTransport) = createClientServerWithLinkedTransport(fakeMediaWikiClient)

            server.start()
            client.connect(clientTransport)
            val result = client.callTool(
                "search_wiki",
                buildJsonObject {
                    put("query", "failing query")
                    put("limit", 10)
                }
            )

            assertEquals(1, result!!.content.size)
            val content = result.content[0] as TextContent
            val responseJson = Json.parseToJsonElement(content.text!!).jsonObject
            assertTrue(responseJson.containsKey("error"))

            val errorMessage = responseJson["error"]!!.jsonPrimitive.content
            assertTrue(errorMessage.contains("Error occurred while searching the wiki"))
            assertTrue(errorMessage.contains("Search failed"))
        }

    @Test
    fun `server responds with successful tool result when client triggers get page content query and wikiclient returns successful response`() =
        runTest {
            val testPageContent = PageContent(
                title = "Test Page",
                text = mapOf("*" to "<p>This is the content of the test page</p>")
            )
            val fakeMediaWikiClient = FakeMediaWikiClient()
            fakeMediaWikiClient.setPageContentResult(Result.success(testPageContent))
            val (server, client, clientTransport) = createClientServerWithLinkedTransport(fakeMediaWikiClient)

            server.start()
            client.connect(clientTransport)
            val result = client.callTool(
                "get_page_content",
                buildJsonObject {
                    put("page_title", "Test Page")
                }
            )

            assertEquals(1, result!!.content.size)
            val content = result.content[0] as TextContent
            val responseJson = Json.parseToJsonElement(content.text!!).jsonObject
            assertTrue(responseJson.containsKey("page_content"))

            val pageContent = Json.decodeFromJsonElement<JsonObject>(responseJson["page_content"]!!)
            assertEquals("Test Page", pageContent["title"]!!.jsonPrimitive.content)
            assertEquals("This is the content of the test page", pageContent["content"]!!.jsonPrimitive.content)
            assertNotNull(pageContent["url"]!!.jsonPrimitive.content)
            assertEquals(8, pageContent["word_count"]!!.jsonPrimitive.int)
        }

    @Test
    fun `server responds with failure tool result when client triggers get page content query but wikiclient returns failure`() =
        runTest {
            val fakeMediaWikiClient = FakeMediaWikiClient()
            fakeMediaWikiClient.setPageContentResult(Result.failure(Exception("Page not found")))
            val (server, client, clientTransport) = createClientServerWithLinkedTransport(fakeMediaWikiClient)

            server.start()
            client.connect(clientTransport)
            val result = client.callTool(
                "get_page_content",
                buildJsonObject {
                    put("page_title", "Nonexistent Page")
                }
            )

            assertEquals(1, result!!.content.size)
            val content = result.content[0] as TextContent
            val responseJson = Json.parseToJsonElement(content.text!!).jsonObject
            assertTrue(responseJson.containsKey("error"))

            val errorMessage = responseJson["error"]!!.jsonPrimitive.content
            assertTrue(errorMessage.contains("Error occurred while fetching page content"))
            assertTrue(errorMessage.contains("Page not found"))
        }

    private fun createClientServerWithLinkedTransport(
        fakeMediaWikiClient: FakeMediaWikiClient = FakeMediaWikiClient(),
        fakeSearchTool: FakeMediaWikiTool = FakeMediaWikiTool("search_wiki"),
        fakeGetPageContentTool: FakeMediaWikiTool = FakeMediaWikiTool("get_page_content")
    ): Triple<MediaWikiMCPServer, Client, FakeTransport> {
        val (serverTransport, clientTransport) = FakeTransport.createLinkedPair()
        val server = MediaWikiMCPServer(
            wikiClient = fakeMediaWikiClient,
            transport = serverTransport,
            searchTool = fakeSearchTool,
            getPageContentTool = fakeGetPageContentTool,
            envConfigProvider = FakeEnvConfigProvider(),
            buildConfigProvider = FakeBuildConfigProvider()
        )
        val client = Client(
            Implementation("Test Client", "1.0.0"),
            ClientOptions(capabilities = ClientCapabilities())
        )
        return Triple(server, client, clientTransport)
    }
}
