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
        val properties = inputSchema.properties
        assert(properties["type"]?.jsonPrimitive?.content == "object")
        val props = properties["properties"]!!.jsonObject

        assert(props.containsKey("query"))
        val queryProperty = props["query"]!!
        assert(queryProperty.jsonObject["type"]?.jsonPrimitive?.content == "string")
        assert(queryProperty.jsonObject["description"]?.jsonPrimitive?.content == "Search query for the TestWiki")

        assert(props.containsKey("limit"))
        val limitProperty = props["limit"]!!
        assert(limitProperty.jsonObject["type"]?.jsonPrimitive?.content == "integer")
        assert(limitProperty.jsonObject["description"]?.jsonPrimitive?.content == "Maximum number of results to return (default: 5)")
        assert(limitProperty.jsonObject["default"]?.jsonPrimitive?.int == 5)

        val required = properties["required"]!!.jsonArray.map { it.jsonPrimitive.content }
        assert(required.contains("query"))
    }

    @Test
    fun `createTool returns tool correct outputSchema with two options`() {
        val tool = searchTool.createTool()
        val outputSchema = tool.outputSchema
        val properties = outputSchema?.properties
        val oneOfArray = properties?.get("oneOf")?.jsonArray
        assert(oneOfArray != null && oneOfArray.size == 2)

        val firstOption = oneOfArray!![0].jsonObject
        assert(firstOption["type"]?.jsonPrimitive?.content == "object")
        val props = firstOption["properties"]!!.jsonObject

        assert(props.containsKey("results"))
        val resultsProperty = props["results"]!!
        assert(resultsProperty.jsonObject["type"]?.jsonPrimitive?.content == "array")
        val items = resultsProperty.jsonObject["items"]!!.jsonObject
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

        val firstOptionRequired = firstOption["required"]!!.jsonArray.map { it.jsonPrimitive.content }
        assert(firstOptionRequired.contains("results"))

        val secondOption = oneOfArray[1].jsonObject
        assert(secondOption["type"]?.jsonPrimitive?.content == "object")
        val secondOptionProps = secondOption["properties"]!!.jsonObject

        assert(secondOptionProps.containsKey("error"))
        val errorProperty = secondOptionProps["error"]!!
        assert(errorProperty.jsonObject["type"]?.jsonPrimitive?.content == "string")
        assert(errorProperty.jsonObject["description"]?.jsonPrimitive?.content == "Error message in case of failures while searching TestWiki")

        val secondOptionRequired = secondOption["required"]!!.jsonArray.map { it.jsonPrimitive.content }
        assert(secondOptionRequired.contains("error"))
    }
}
