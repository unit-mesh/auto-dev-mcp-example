package cc.unitmesh.mcp.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP-specific implementation of ToolCallbackProvider.
 * Creates MCPMethodToolCallback instances directly from MCPToolMetadata.
 *
 * @author AutoDev MCP Team
 */
@Component("toolCallbackProvider")
public class MCPToolCallbackProvider implements ToolCallbackProvider {

    private static final Logger logger = LoggerFactory.getLogger(MCPToolCallbackProvider.class);

    private final MCPToolRegistry toolRegistry;
    private final Map<String, MCPMethodToolCallback> toolCallbacks = new ConcurrentHashMap<>();

    public MCPToolCallbackProvider(MCPToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
        logger.info("MCPToolCallbackProvider initialized");
    }
    
    /**
     * Create tool callbacks from registered MCP tools.
     */
    private void createToolCallbacks() {
        toolCallbacks.clear();

        Collection<MCPToolMetadata> tools = toolRegistry.getAllTools();
        for (MCPToolMetadata metadata : tools) {
            if (metadata.isEnabled()) {
                MCPMethodToolCallback callback = new MCPMethodToolCallback(metadata);
                toolCallbacks.put(metadata.getName(), callback);
                logger.debug("Created tool callback for: {}", metadata.getName());
            }
        }

        logger.info("Created {} MCP tool callbacks", toolCallbacks.size());
    }

    /**
     * Ensure tool callbacks are initialized.
     */
    private void ensureCallbacksInitialized() {
        if (toolCallbacks.isEmpty() && toolRegistry.getToolCount() > 0) {
            synchronized (this) {
                if (toolCallbacks.isEmpty() && toolRegistry.getToolCount() > 0) {
                    createToolCallbacks();
                }
            }
        }
    }

    /**
     * Get a specific tool callback by name.
     * This is a convenience method not part of the interface.
     */
    public Optional<ToolCallback> getToolCallback(String toolName) {
        ensureCallbacksInitialized();
        MCPMethodToolCallback callback = toolCallbacks.get(toolName);
        return Optional.ofNullable(callback);
    }

    @Override
    public FunctionCallback[] getToolCallbacks() {
        ensureCallbacksInitialized();
        return toolCallbacks.values().toArray(new FunctionCallback[0]);
    }
    
    /**
     * Refresh tool callbacks when registry changes.
     */
    public void refreshToolCallbacks() {
        synchronized (this) {
            createToolCallbacks();
        }
        logger.info("MCP tool callbacks refreshed");
    }

    /**
     * Add a new tool callback.
     */
    public void addToolCallback(MCPToolMetadata metadata) {
        if (metadata.isEnabled()) {
            MCPMethodToolCallback callback = new MCPMethodToolCallback(metadata);
            toolCallbacks.put(metadata.getName(), callback);
            logger.info("Added tool callback for: {}", metadata.getName());
        }
    }

    /**
     * Remove a tool callback.
     */
    public void removeToolCallback(String toolName) {
        MCPMethodToolCallback removed = toolCallbacks.remove(toolName);
        if (removed != null) {
            logger.info("Removed tool callback for: {}", toolName);
        }
    }

    /**
     * Get tool callback count.
     */
    public int getCallbackCount() {
        ensureCallbacksInitialized();
        return toolCallbacks.size();
    }

    /**
     * Check if a tool callback exists.
     */
    public boolean hasToolCallback(String toolName) {
        return getToolCallback(toolName).isPresent();
    }

    /**
     * Get all tool callbacks as a map (for compatibility).
     */
    public Map<String, ToolCallback> getToolCallbacksAsMap() {
        ensureCallbacksInitialized();
        Map<String, ToolCallback> result = new HashMap<>();
        for (Map.Entry<String, MCPMethodToolCallback> entry : toolCallbacks.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
