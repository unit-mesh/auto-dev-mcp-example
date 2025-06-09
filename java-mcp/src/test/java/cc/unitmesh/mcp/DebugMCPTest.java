package cc.unitmesh.mcp;

import cc.unitmesh.mcp.core.MCPToolCallbackProvider;
import cc.unitmesh.mcp.core.MCPToolRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Debug test to understand what's happening with tool callbacks.
 */
@SpringBootTest
public class DebugMCPTest {
    
    @Autowired
    private MCPToolRegistry toolRegistry;
    
    @Autowired
    private MCPToolCallbackProvider callbackProvider;
    
    @Test
    public void debugToolCallbacks() {
        System.out.println("=== Debug Tool Callbacks ===");
        
        // Check registry
        System.out.println("Tool count in registry: " + toolRegistry.getToolCount());
        toolRegistry.getAllTools().forEach(tool -> 
            System.out.println("  - " + tool.getName() + " (" + tool.getBeanClass().getSimpleName() + ")"));
        
        // Check callback provider
        System.out.println("\nCallback count: " + callbackProvider.getCallbackCount());
        FunctionCallback[] callbacks = callbackProvider.getToolCallbacks();
        System.out.println("Actual callbacks: " + callbacks.length);
        
        for (FunctionCallback callback : callbacks) {
            System.out.println("  - Callback: " + callback.getClass().getSimpleName());
            if (callback instanceof org.springframework.ai.tool.ToolCallback toolCallback) {
                System.out.println("    Name: " + toolCallback.getName());
                System.out.println("    Description: " + toolCallback.getDescription());
            }
        }
        
        // Test specific tool lookup
        var weatherCallback = callbackProvider.getToolCallback("get_weather_forecast");
        System.out.println("\nWeather callback found: " + weatherCallback.isPresent());
        
        var tableCallback = callbackProvider.getToolCallback("get_table_schema");
        System.out.println("Table schema callback found: " + tableCallback.isPresent());
    }
}
