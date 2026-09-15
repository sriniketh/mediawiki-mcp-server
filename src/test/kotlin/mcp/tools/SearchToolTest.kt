package mcp.tools

import com.sriniketh.mcp.tools.SearchTool
import com.sriniketh.utils.EnvConfigProvider
import fakes.FakeEnvConfigProvider
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test

class SearchToolTest {

    private val envConfigProvider: EnvConfigProvider = FakeEnvConfigProvider()
    private val searchTool = SearchTool(envConfigProvider)

    @Test
    fun `createTool returns tool with correct tool name, title, description and annotations`() {
        val tool = searchTool.createTool()
        assert(tool.name == "search_wiki")
        assert(tool.title == "Search TestWiki")
        assert(tool.description == "Search the TestWiki for information about the topic")
        assert(tool.annotations == null)
    }

    @Test
    fun `createTool returns tool with correct inputSchema`() {
        val tool = searchTool.createTool()
        val inputSchema = tool.inputSchema
        val properties = inputSchema.properties!!

        assert(properties.keys == setOf("query", "limit"))

        val queryProperty = properties["query"]!!
        assert(queryProperty.jsonObject["type"]?.jsonPrimitive?.content == "string")
        assert(queryProperty.jsonObject["description"]?.jsonPrimitive?.content == "Search query for the TestWiki")

        val limitProperty = properties["limit"]!!
        assert(limitProperty.jsonObject["type"]?.jsonPrimitive?.content == "integer")
        assert(limitProperty.jsonObject["description"]?.jsonPrimitive?.content == "Maximum number of results to return (default: 5)")
        assert(limitProperty.jsonObject["default"]?.jsonPrimitive?.int == 5)

        assert(inputSchema.required == listOf("query"))
    }

    @Test
    fun `createTool returns tool correct outputSchema with results and error fields`() {
        val tool = searchTool.createTool()
        val outputSchema = tool.outputSchema
        val properties = outputSchema?.properties!!

        assert(properties.keys == setOf("results", "error"))

        val resultsProperty = properties["results"]!!.jsonObject
        assert(resultsProperty["type"]?.jsonPrimitive?.content == "array")
        val items = resultsProperty["items"]!!.jsonObject
        assert(items["type"]?.jsonPrimitive?.content == "object")
        val itemProps = items["properties"]!!.jsonObject
        val expectedItemProperties =
            listOf("title", "snippet", "sectiontitle", "categorysnippet", "wordcount")
        for (property in expectedItemProperties) {
            assert(itemProps.containsKey(property))
        }
        val titleProperty = itemProps["title"]!!
        assert(titleProperty.jsonObject["type"]?.jsonPrimitive?.content == "string")
        assert(titleProperty.jsonObject["description"]?.jsonPrimitive?.content == "Title of the TestWiki page")

        val snippetProperty = itemProps["snippet"]!!
        assert(snippetProperty.jsonObject["type"]?.jsonPrimitive?.content == "string")
        assert(snippetProperty.jsonObject["description"]?.jsonPrimitive?.content == "A brief snippet from the page content")

        val sectionTitleProperty = itemProps["sectiontitle"]!!
        assert(sectionTitleProperty.jsonObject["type"]?.jsonPrimitive?.content == "string")
        assert(sectionTitleProperty.jsonObject["description"]?.jsonPrimitive?.content == "Title of the section where the snippet was found")

        val categorySnippetProperty = itemProps["categorysnippet"]!!
        assert(categorySnippetProperty.jsonObject["type"]?.jsonPrimitive?.content == "string")
        assert(categorySnippetProperty.jsonObject["description"]?.jsonPrimitive?.content == "Categories associated with the page")

        val wordCountProperty = itemProps["wordcount"]!!
        assert(wordCountProperty.jsonObject["type"]?.jsonPrimitive?.content == "integer")
        assert(wordCountProperty.jsonObject["description"]?.jsonPrimitive?.content == "Number of words in the page")

        val required = items["required"]!!.jsonArray.map { it.jsonPrimitive.content }
        for (property in expectedItemProperties) {
            assert(required.contains(property))
        }

        val errorProperty = properties["error"]!!.jsonObject
        assert(errorProperty["type"]?.jsonPrimitive?.content == "string")
        assert(
            errorProperty["description"]?.jsonPrimitive?.content ==
                "Error message in case of failures while searching TestWiki. Present when the search fails."
        )
    }
}
