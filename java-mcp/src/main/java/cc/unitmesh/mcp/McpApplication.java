package cc.unitmesh.mcp;

import cc.unitmesh.mcp.annotation.EnableMCP;
import cc.unitmesh.mcp.core.MCPToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
@EnableMCP(basePackages = "cc.unitmesh.mcp")
public class McpApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpApplication.class, args);
	}
}
