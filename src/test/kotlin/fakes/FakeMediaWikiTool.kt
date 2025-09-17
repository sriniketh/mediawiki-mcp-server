package fakes

import com.sriniketh.mcp.tools.MediaWikiTool
import io.modelcontextprotocol.kotlin.sdk.Tool

class FakeMediaWikiTool(private val name: String) : MediaWikiTool {

    override fun createTool(): Tool = Tool(
        name = name,
        title = "Fake Tool $name",
        description = "A fake tool for testing",
        inputSchema = Tool.Input(),
        outputSchema = null,
        annotations = null
    )
}
