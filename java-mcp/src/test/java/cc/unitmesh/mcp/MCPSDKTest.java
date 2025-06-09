package cc.unitmesh.mcp;

import cc.unitmesh.mcp.core.MCPToolMetadata;
import cc.unitmesh.mcp.core.MCPToolRegistry;
import cc.unitmesh.mcp.core.MCPToolCallbackProvider;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the new MCP SDK functionality.
 * 
 * @author AutoDev MCP Team
 */
@SpringBootTest
@SpringJUnitConfig
public class MCPSDKTest {
    
    @Autowired
    private MCPToolRegistry toolRegistry;

    @Autowired
    private MCPToolCallbackProvider callbackProvider;
    
    @Test
    public void testToolRegistryInitialization() {
        assertNotNull(toolRegistry, "MCPToolRegistry should be initialized");
        assertTrue(toolRegistry.getToolCount() > 0, "Should have registered tools");
    }
    
    @Test
    public void testWeatherToolRegistration() {
        Optional<MCPToolMetadata> weatherTool = toolRegistry.getTool("get_weather_forecast");
        assertTrue(weatherTool.isPresent(), "Weather tool should be registered");
        
        MCPToolMetadata metadata = weatherTool.get();
        assertEquals("get_weather_forecast", metadata.getName());
        assertEquals("weather", metadata.getCategory());
        assertEquals("1.0", metadata.getVersion());
        assertTrue(metadata.isCacheable());
        assertEquals(300, metadata.getCacheTtlSeconds());
    }
    
    @Test
    public void testSqlToolsRegistration() {
        // Test query_sql tool
        Optional<MCPToolMetadata> queryTool = toolRegistry.getTool("query_sql");
        assertTrue(queryTool.isPresent(), "SQL query tool should be registered");
        
        MCPToolMetadata queryMetadata = queryTool.get();
        assertEquals("database", queryMetadata.getCategory());
        assertTrue(queryMetadata.isRequiresAuth());
        assertEquals(30000, queryMetadata.getTimeoutMs());
        
        // Test list_tables tool
        Optional<MCPToolMetadata> listTool = toolRegistry.getTool("list_tables");
        assertTrue(listTool.isPresent(), "List tables tool should be registered");
        
        MCPToolMetadata listMetadata = listTool.get();
        assertEquals("database", listMetadata.getCategory());
        assertTrue(listMetadata.isCacheable());
        assertEquals(600, listMetadata.getCacheTtlSeconds());
        
        // Test get_table_schema tool
        Optional<MCPToolMetadata> schemaTool = toolRegistry.getTool("get_table_schema");
        assertTrue(schemaTool.isPresent(), "Table schema tool should be registered");
        
        MCPToolMetadata schemaMetadata = schemaTool.get();
        assertEquals("database", schemaMetadata.getCategory());
        assertTrue(schemaMetadata.isCacheable());
        assertEquals(1800, schemaMetadata.getCacheTtlSeconds());
    }
    
    @Test
    public void testToolsByCategory() {
        List<MCPToolMetadata> weatherTools = toolRegistry.getToolsByCategory("weather");
        assertFalse(weatherTools.isEmpty(), "Should have weather tools");
        
        List<MCPToolMetadata> databaseTools = toolRegistry.getToolsByCategory("database");
        assertFalse(databaseTools.isEmpty(), "Should have database tools");
        assertTrue(databaseTools.size() >= 3, "Should have at least 3 database tools");
    }
    
    @Test
    public void testToolsByTag() {
        List<MCPToolMetadata> sqlTools = toolRegistry.getToolsByTag("sql");
        assertFalse(sqlTools.isEmpty(), "Should have SQL tools");
        
        List<MCPToolMetadata> databaseTools = toolRegistry.getToolsByTag("database");
        assertFalse(databaseTools.isEmpty(), "Should have database tools");
    }
    
    @Test
    public void testToolSearch() {
        List<MCPToolMetadata> weatherResults = toolRegistry.searchTools("weather");
        assertFalse(weatherResults.isEmpty(), "Should find weather-related tools");
        
        List<MCPToolMetadata> sqlResults = toolRegistry.searchTools("sql");
        assertFalse(sqlResults.isEmpty(), "Should find SQL-related tools");
    }
    
    @Test
    public void testCallbackProviderIntegration() {
        assertNotNull(callbackProvider, "MCPToolCallbackProvider should be initialized");
        assertTrue(callbackProvider.getCallbackCount() > 0, "Should have tool callbacks");
        
        // Test specific tool callbacks
        Optional<ToolCallback> weatherCallback = callbackProvider.getToolCallback("get_weather_forecast");
        assertTrue(weatherCallback.isPresent(), "Weather tool callback should exist");
        
        Optional<ToolCallback> queryCallback = callbackProvider.getToolCallback("query_sql");
        assertTrue(queryCallback.isPresent(), "SQL query tool callback should exist");
    }
    
    @Test
    public void testAllToolsHaveCallbacks() {
        Collection<MCPToolMetadata> allTools = toolRegistry.getAllTools();
        for (MCPToolMetadata tool : allTools) {
            // Note: We check if the tool exists in the callback provider's map
            // since the delegate provider handles the actual tool callbacks
            assertTrue(callbackProvider.hasToolCallback(tool.getName()),
                "Tool " + tool.getName() + " should have a callback");
        }
    }
    
    @Test
    public void testCategories() {
        var categories = toolRegistry.getCategories();
        assertTrue(categories.contains("weather"), "Should have weather category");
        assertTrue(categories.contains("database"), "Should have database category");
    }
    
    @Test
    public void testToolMetadataProperties() {
        Optional<MCPToolMetadata> tool = toolRegistry.getTool("get_weather_forecast");
        assertTrue(tool.isPresent());
        
        MCPToolMetadata metadata = tool.get();
        assertNotNull(metadata.getDescription());
        assertNotNull(metadata.getTags());
        assertNotNull(metadata.getMethod());
        assertNotNull(metadata.getBean());
        assertNotNull(metadata.getBeanClass());
        assertTrue(metadata.isEnabled());
    }
}
