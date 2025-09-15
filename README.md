# MediaWiki MCP Server

A MediaWiki [MCP](https://modelcontextprotocol.io/docs/getting-started/intro) server written in Kotlin. Provides access
to content on MediaWiki-based websites through MCP and helps ground answers.

Uses the official [Kotlin MCP SDK](https://github.com/modelcontextprotocol/kotlin-sdk) for the server
and [ktor](https://ktor.io/)
client for HTTP requests.

## Tools available

- `search_wiki`: Search for pages on the wiki
- `get_page_content`: Retrieve the content of a specific page

## Build Steps

### Environment Variables

- `WIKI_NAME`: Display name for the wiki used in tools (e.g., "Wikipedia", "Wikibooks")
- `WIKI_API_URL`: Full URL to the MediaWiki API endpoint (e.g., "https://en.wikipedia.org/w/api.php")

### Build

```bash
docker build -t mediawiki-mcp-server .
```

### Running with application

```bash
docker run -i -e WIKI_NAME="Stardew Valley Wiki" -e WIKI_API_URL="https://stardewvalleywiki.com/mediawiki/api.php" mediawiki-mcp-server
```

### Example MCP Configuration

The following can be added to your Claude/LM Studio MCP configuration in order to spin up the containers:

```json
{
  "mcpServers": {
    "stardew-mediawiki": {
      "command": "docker",
      "args": [
        "run",
        "--name",
        "stardew-wiki-mcp",
        "--rm",
        "-i",
        "-e",
        "WIKI_NAME=Stardew Valley Wiki",
        "-e",
        "WIKI_API_URL=https://stardewvalleywiki.com/mediawiki/api.php",
        "mediawiki-mcp-server"
      ]
    },
    "mario-mediawiki": {
      "command": "docker",
      "args": [
        "run",
        "--name",
        "mario-wiki-mcp",
        "--rm",
        "-i",
        "-e",
        "WIKI_NAME=Mario Wiki",
        "-e",
        "WIKI_API_URL=https://www.mariowiki.com/api.php",
        "mediawiki-mcp-server"
      ]
    }
  }
}
```

## Logging

The server uses Logback for logging with the following configuration:

- **Console output**: Logs are written to stderr for MCP compatibility
- **File logging**: Logs are saved to `logs/mediawiki-mcp-server.log` inside the container with rotation and retention
  policies

## License

```
   Copyright 2025 Sriniketh Ramachandran

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
