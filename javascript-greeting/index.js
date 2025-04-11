var McpServer = require("@modelcontextprotocol/sdk/server/mcp.js").McpServer;
var ResourceTemplate = require("@modelcontextprotocol/sdk/server/mcp.js").ResourceTemplate;
var StdioServerTransport = require("@modelcontextprotocol/sdk/server/stdio.js").StdioServerTransport;
var z = require("zod").z;

// Create an MCP server
var server = new McpServer({
    name: "Demo",
    version: "1.0.0"
});

// Add an addition tool
server.tool("add",
    { a: z.number(), b: z.number() },
    function(params) {
        var a = params.a;
        var b = params.b;
        return Promise.resolve({
            content: [{ type: "text", text: String(a + b) }]
        });
    }
);

// Add a dynamic greeting resource
server.resource(
    "greeting",
    new ResourceTemplate("greeting://{name}", { list: undefined }),
    function(uri, params) {
        var name = params.name;
        return Promise.resolve({
            contents: [{
                uri: uri.href,
                text: "Hello, " + name + "!"
            }]
        });
    }
);

// Start receiving messages on stdin and sending messages on stdout
var transport = new StdioServerTransport();
server.connect(transport).then(function() {
    console.log("Server connected");
}).catch(function(error) {
    console.error("Error connecting server:", error);
});
