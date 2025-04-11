# AutoDev MCP Examples

## Java


- [Spring AI - Model Context Protocol (MCP)](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html)
- [GitHub Java SDK](https://github.com/modelcontextprotocol/java-sdk)
- [Java MCP Server](https://modelcontextprotocol.io/sdk/java/mcp-server)


## Python Example

```
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/Volumes/source/ai/autocrud"
      ],
      "disabled": true
    },
    "echo": {
      "command": "uv",
      "args": [
        "run",
        "--directory",
        "/Users/phodal/source/ai/autodev-mcp-test/python-sqlite3/",
        "--with",
        "mcp",
        "mcp",
        "run",
        "/Users/phodal/source/ai/autodev-mcp-test/python-sqlite3/server.py"
      ]
    },
    "weather": {
      "command": "java",
      "args": [
        "-jar",
        "/Volumes/source/ai/autodev-mcp-test/java-mcp/target/mcp-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```