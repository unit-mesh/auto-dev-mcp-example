package cc.unitmesh.mcp.core;

import cc.unitmesh.mcp.annotation.MCPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for managing MCP tools.
 * Handles registration, discovery, and retrieval of MCP tools.
 * 
 * @author AutoDev MCP Team
 */
@Component
public class MCPToolRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPToolRegistry.class);
    
    private final Map<String, MCPToolMetadata> tools = new ConcurrentHashMap<>();
    private final Map<String, List<MCPToolMetadata>> toolsByCategory = new ConcurrentHashMap<>();
    
    /**
     * Register an MCP tool from a bean and method.
     */
    public void registerTool(Object bean, Method method, MCPTool annotation) {
        String toolName = StringUtils.hasText(annotation.name()) ? 
            annotation.name() : method.getName();
        
        if (!annotation.enabled()) {
            logger.debug("Skipping disabled tool: {}", toolName);
            return;
        }
        
        MCPToolMetadata metadata = new MCPToolMetadata(
            toolName,
            annotation.description(),
            annotation.category(),
            annotation.version(),
            annotation.tags(),
            annotation.enabled(),
            annotation.priority(),
            annotation.requiresAuth(),
            annotation.timeoutMs(),
            annotation.cacheable(),
            annotation.cacheTtlSeconds(),
            method,
            bean,
            bean.getClass()
        );
        
        // Check for duplicate tool names
        if (tools.containsKey(toolName)) {
            MCPToolMetadata existing = tools.get(toolName);
            logger.warn("Tool name conflict detected: '{}'. Existing: {}.{}, New: {}.{}", 
                toolName,
                existing.getBeanClass().getSimpleName(), existing.getMethod().getName(),
                metadata.getBeanClass().getSimpleName(), metadata.getMethod().getName());
            
            // Keep the one with higher priority
            if (metadata.getPriority() > existing.getPriority()) {
                logger.info("Replacing tool '{}' with higher priority version", toolName);
                unregisterTool(toolName);
            } else {
                logger.info("Keeping existing tool '{}' with higher or equal priority", toolName);
                return;
            }
        }
        
        tools.put(toolName, metadata);
        
        // Add to category index
        toolsByCategory.computeIfAbsent(annotation.category(), k -> new ArrayList<>())
            .add(metadata);
        
        logger.info("Registered MCP tool: {} [{}] - {}", 
            toolName, annotation.category(), annotation.description());
    }
    
    /**
     * Unregister a tool by name.
     */
    public void unregisterTool(String toolName) {
        MCPToolMetadata removed = tools.remove(toolName);
        if (removed != null) {
            // Remove from category index
            List<MCPToolMetadata> categoryTools = toolsByCategory.get(removed.getCategory());
            if (categoryTools != null) {
                categoryTools.remove(removed);
                if (categoryTools.isEmpty()) {
                    toolsByCategory.remove(removed.getCategory());
                }
            }
            logger.info("Unregistered MCP tool: {}", toolName);
        }
    }
    
    /**
     * Get a tool by name.
     */
    public Optional<MCPToolMetadata> getTool(String toolName) {
        return Optional.ofNullable(tools.get(toolName));
    }
    
    /**
     * Get all registered tools.
     */
    public Collection<MCPToolMetadata> getAllTools() {
        return new ArrayList<>(tools.values());
    }
    
    /**
     * Get tools by category.
     */
    public List<MCPToolMetadata> getToolsByCategory(String category) {
        return toolsByCategory.getOrDefault(category, Collections.emptyList())
            .stream()
            .collect(Collectors.toList());
    }
    
    /**
     * Get all categories.
     */
    public Set<String> getCategories() {
        return new HashSet<>(toolsByCategory.keySet());
    }
    
    /**
     * Get tools by tag.
     */
    public List<MCPToolMetadata> getToolsByTag(String tag) {
        return tools.values().stream()
            .filter(tool -> Arrays.asList(tool.getTags()).contains(tag))
            .collect(Collectors.toList());
    }
    
    /**
     * Search tools by name pattern.
     */
    public List<MCPToolMetadata> searchTools(String namePattern) {
        String pattern = namePattern.toLowerCase();
        return tools.values().stream()
            .filter(tool -> tool.getName().toLowerCase().contains(pattern) ||
                          tool.getDescription().toLowerCase().contains(pattern))
            .collect(Collectors.toList());
    }
    
    /**
     * Get tool count.
     */
    public int getToolCount() {
        return tools.size();
    }
    
    /**
     * Check if a tool exists.
     */
    public boolean hasTool(String toolName) {
        return tools.containsKey(toolName);
    }
    
    /**
     * Clear all tools.
     */
    public void clear() {
        tools.clear();
        toolsByCategory.clear();
        logger.info("Cleared all MCP tools from registry");
    }
}
