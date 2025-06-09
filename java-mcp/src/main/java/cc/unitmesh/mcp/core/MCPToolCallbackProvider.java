package cc.unitmesh.mcp.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * MCP-specific implementation of ToolCallbackProvider.
 * Delegates to MethodToolCallbackProvider for actual tool management.
 *
 * @author AutoDev MCP Team
 */
@Component("toolCallbackProvider")
public class MCPToolCallbackProvider implements ToolCallbackProvider {

    private static final Logger logger = LoggerFactory.getLogger(MCPToolCallbackProvider.class);

    private final MCPToolRegistry toolRegistry;
    private volatile MethodToolCallbackProvider delegateProvider;

    public MCPToolCallbackProvider(MCPToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
        // Delay initialization until tools are registered
        this.delegateProvider = null;
    }
    
    /**
     * Create delegate provider from registered MCP tools.
     */
    private MethodToolCallbackProvider createDelegateProvider() {
        // Collect all tool objects from registry
        var toolObjects = toolRegistry.getAllTools().stream()
            .map(MCPToolMetadata::getBean)
            .distinct()
            .toArray();

        if (toolObjects.length == 0) {
            logger.warn("No MCP tools found in registry");
            return MethodToolCallbackProvider.builder().build();
        }

        logger.info("Creating delegate provider with {} tool objects", toolObjects.length);
        return MethodToolCallbackProvider.builder()
            .toolObjects(toolObjects)
            .build();
    }
    
    /**
     * Ensure delegate provider is initialized.
     */
    private MethodToolCallbackProvider getDelegateProvider() {
        if (delegateProvider == null) {
            synchronized (this) {
                if (delegateProvider == null) {
                    delegateProvider = createDelegateProvider();
                }
            }
        }
        return delegateProvider;
    }

    /**
     * Get a specific tool callback by name.
     * This is a convenience method not part of the interface.
     */
    public Optional<ToolCallback> getToolCallback(String toolName) {
        // First try to find by MCP tool name
        var mcpTool = toolRegistry.getTool(toolName);
        if (mcpTool.isPresent()) {
            String methodName = mcpTool.get().getMethod().getName();
            // Search by method name
            for (FunctionCallback callback : getDelegateProvider().getToolCallbacks()) {
                if (callback instanceof ToolCallback toolCallback &&
                    toolCallback.getName().equals(methodName)) {
                    return Optional.of(toolCallback);
                }
            }
        }

        // Fallback: search by exact name match
        for (FunctionCallback callback : getDelegateProvider().getToolCallbacks()) {
            if (callback instanceof ToolCallback toolCallback &&
                toolCallback.getName().equals(toolName)) {
                return Optional.of(toolCallback);
            }
        }
        return Optional.empty();
    }

    @Override
    public FunctionCallback[] getToolCallbacks() {
        return getDelegateProvider().getToolCallbacks();
    }
    
    /**
     * Refresh tool callbacks when registry changes.
     * This recreates the delegate provider with updated tools.
     */
    public void refreshToolCallbacks() {
        synchronized (this) {
            delegateProvider = null; // Force recreation on next access
        }
        logger.info("MCP tool callbacks refreshed");
    }

    /**
     * Get tool callback count.
     */
    public int getCallbackCount() {
        return getDelegateProvider().getToolCallbacks().length;
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
    public java.util.Map<String, ToolCallback> getToolCallbacksAsMap() {
        java.util.Map<String, ToolCallback> result = new java.util.HashMap<>();
        for (FunctionCallback callback : getDelegateProvider().getToolCallbacks()) {
            if (callback instanceof ToolCallback toolCallback) {
                result.put(toolCallback.getName(), toolCallback);
            }
        }
        return result;
    }
}
