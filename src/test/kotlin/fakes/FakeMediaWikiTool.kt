package fakes

import com.sriniketh.mcp.tools.MediaWikiTool
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema

class FakeMediaWikiTool(private val name: String) : MediaWikiTool {

    override fun createTool(): Tool = Tool(
        name = name,
        title = "Fake Tool $name",
        description = "A fake tool for testing",
        inputSchema = ToolSchema(),
        outputSchema = null,
        annotations = null
    )
}
