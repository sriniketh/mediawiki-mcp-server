package mcp.tools

import com.sriniketh.mcp.tools.GetPageContentTool
import com.sriniketh.utils.EnvConfigProvider
import fakes.FakeEnvConfigProvider
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test

class GetPageContentToolTest {

    private val envConfigProvider: EnvConfigProvider = FakeEnvConfigProvider()
    private val getPageContentTool = GetPageContentTool(envConfigProvider)

    @Test
    fun `createTool returns tool with correct tool name, title, description and annotations`() {
        val tool = getPageContentTool.createTool()
        assert(tool.name == "get_page_content")
        assert(tool.title == "Get Wiki Page Content")
        assert(tool.description == "Get the full content of a specific TestWiki page")
        assert(tool.annotations == null)
    }

    @Test
    fun `createTool returns tool with correct inputSchema`() {
        val tool = getPageContentTool.createTool()
        val inputSchema = tool.inputSchema
        assert(inputSchema.properties.containsKey("page_title"))
        val pageTitleProperty = inputSchema.properties["page_title"]!!
        assert(pageTitleProperty.jsonObject["type"]?.jsonPrimitive?.content == "string")
        assert(pageTitleProperty.jsonObject["description"]?.jsonPrimitive?.content == "The exact title of the TestWiki page to retrieve")
        assert(inputSchema.required?.contains("page_title") == true)
    }

    @Test
    fun `createTool returns tool with correct outputSchema`() {
        val tool = getPageContentTool.createTool()
        val outputSchema = tool.outputSchema
        val expectedProperties = listOf("title", "content", "url", "word_count")
        for (property in expectedProperties) {
            assert(outputSchema?.properties?.containsKey(property) == true)
        }
        val titleProperty = outputSchema?.properties["title"]!!
        assert(titleProperty.jsonObject["type"]?.jsonPrimitive?.content == "string")
        assert(titleProperty.jsonObject["description"]?.jsonPrimitive?.content == "The title of the TestWiki page")

        val contentProperty = outputSchema.properties["content"]!!
        assert(contentProperty.jsonObject["type"]?.jsonPrimitive?.content == "string")
        assert(contentProperty.jsonObject["description"]?.jsonPrimitive?.content == "The full text content of the page (HTML stripped)")

        val urlProperty = outputSchema.properties["url"]!!
        assert(urlProperty.jsonObject["type"]?.jsonPrimitive?.content == "string")
        assert(urlProperty.jsonObject["format"]?.jsonPrimitive?.content == "uri")
        assert(urlProperty.jsonObject["description"]?.jsonPrimitive?.content == "Direct URL to the TestWiki page")

        val wordCountProperty = outputSchema.properties["word_count"]!!
        assert(wordCountProperty.jsonObject["type"]?.jsonPrimitive?.content == "integer")
        assert(wordCountProperty.jsonObject["description"]?.jsonPrimitive?.content == "Number of words in the content")

        assert(outputSchema.required?.containsAll(expectedProperties) == true)
    }
}
