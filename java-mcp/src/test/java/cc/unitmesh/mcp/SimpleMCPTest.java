package cc.unitmesh.mcp;

import cc.unitmesh.mcp.core.MCPToolRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple test to verify basic MCP functionality.
 */
@SpringBootTest
public class SimpleMCPTest {
    
    @Autowired
    private MCPToolRegistry toolRegistry;
    
    @Test
    public void testBasicSetup() {
        assertNotNull(toolRegistry, "MCPToolRegistry should be initialized");
        System.out.println("Tool count: " + toolRegistry.getToolCount());
        System.out.println("All tools: " + toolRegistry.getAllTools());
    }
    
    @Test
    public void testWeatherServiceExists() {
        assertTrue(toolRegistry.getToolCount() > 0, "Should have some tools registered");
        
        var weatherTool = toolRegistry.getTool("get_weather_forecast");
        if (weatherTool.isPresent()) {
            System.out.println("Found weather tool: " + weatherTool.get());
        } else {
            System.out.println("Weather tool not found. Available tools:");
            toolRegistry.getAllTools().forEach(tool -> 
                System.out.println("  - " + tool.getName() + " (" + tool.getCategory() + ")"));
        }
    }
}
