# CLAUDE.md

## Project

MediaWiki MCP Server — a Kotlin MCP server that exposes MediaWiki API content (search + page retrieval) to LLM clients via the Model Context Protocol. Configurable to work with any MediaWiki-based site (Wikipedia, Stardew Valley Wiki, etc.) via environment variables.

## Commands

```bash
./gradlew build          # Build + test
./gradlew test           # Run tests only
docker build -t mediawiki-mcp-server .
docker run -i -e WIKI_NAME="Wikipedia" -e WIKI_API_URL="https://en.wikipedia.org/w/api.php" mediawiki-mcp-server
```

## Architecture

```
src/main/kotlin/
├── Application.kt                  # Entry point: creates server, calls start(), awaits cancellation
├── client/
│   └── MediaWikiClient.kt         # Interface + Ktor impl for MediaWiki HTTP API calls
├── mcp/
│   ├── MediaWikiMCPServer.kt      # MCP server: registers tools, handles requests, wires client
│   └── tools/
│       ├── MediaWikiTool.kt       # Interface for tool definitions
│       ├── SearchTool.kt          # search_wiki tool definition
│       └── GetPageContentTool.kt  # get_page_content tool definition
├── model/
│   ├── SearchWiki.kt              # Search request/response models
│   └── GetPageContent.kt          # Page content request/response models
└── utils/
    ├── BuildConfigProvider.kt     # App version from gradle buildConfig
    ├── EnvConfigProvider.kt       # WIKI_NAME and WIKI_API_URL env vars
    └── Extensions.kt              # String.cleanHtmlContent() using Jsoup
```

## Key Technologies

- Kotlin 2.3 / JVM 17
- MCP Kotlin SDK (`io.modelcontextprotocol:kotlin-sdk`)
- Ktor client (CIO engine) for HTTP
- kotlinx-serialization for JSON
- Jsoup for HTML-to-text
- Logback + kotlin-logging
- Gradle with version catalog (`gradle/libs.versions.toml`)

## Environment Variables (required)

- `WIKI_NAME` — display name for tool descriptions (e.g., "Wikipedia")
- `WIKI_API_URL` — full MediaWiki API URL (e.g., "https://en.wikipedia.org/w/api.php")

## Code Patterns

- **Constructor injection with defaults** — all classes accept dependencies as constructor params with default prod implementations (e.g., `engine: HttpClientEngine = CIO.create()`). This makes testing easy without a DI framework.
- **Interface-based abstractions** — `MediaWikiClient`, `MediaWikiTool`, `EnvConfigProvider`, `BuildConfigProvider` all have interfaces. Implementations are suffixed with `Impl`.
- **Result<T> error handling** — API calls use `runCatching` and `.fold(onSuccess, onFailure)`. Tool responses always return `CallToolResult` with either success data or an error JSON object.
- **kotlinx.serialization** — all models use `@Serializable`. Use `@SerialName` for JSON field name mapping (e.g., `page_title` -> `title`).
- **MCP transport** — uses stdio (`StdioServerTransport`) for MCP communication. Logs go to stderr to avoid interfering with stdio MCP messages.

## Testing

- **Fakes over mocks** — test doubles are hand-written fakes in `src/test/kotlin/fakes/`. No mocking framework.
- **Ktor MockEngine** — `MediaWikiClientTest` uses Ktor's `MockEngine` to simulate HTTP responses.
- **FakeTransport** — `FakeTransport.createLinkedPair()` creates bidirectional in-memory transports for MCP server integration tests with a real MCP `Client`.
- **Test naming** — backtick-quoted descriptive names (e.g., `` `handleSearch returns list of WikiPage when request is successful` ``).
- **JUnit 5** — configured via `useJUnitPlatform()`.
- **Coroutines** — tests use `kotlinx.coroutines.test.runTest`.

## Gotchas

- The MediaWiki API returns HTML content with the key `"*"` in the text map — see `pageContent.text["*"]` in `MediaWikiMCPServer.kt`.
- `SearchTool` nests its input schema differently from `GetPageContentTool` — it wraps properties inside an extra `"properties"` object with explicit `"type": "object"` and `"required"` array.
- Logging to stdout will break MCP stdio communication. All logging must go to stderr (configured in `logback.xml`).
- The Docker build produces a fat JAR (`mediawiki_mcp_server-all.jar`) via the Ktor Gradle plugin.
