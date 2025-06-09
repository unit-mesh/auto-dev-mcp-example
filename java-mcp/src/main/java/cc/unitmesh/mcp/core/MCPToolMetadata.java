package cc.unitmesh.mcp.core;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * Metadata holder for MCP tools.
 * Contains all the information about an MCP tool including its configuration and runtime details.
 * 
 * @author AutoDev MCP Team
 */
public class MCPToolMetadata {
    
    private final String name;
    private final String description;
    private final String category;
    private final String version;
    private final String[] tags;
    private final boolean enabled;
    private final int priority;
    private final boolean requiresAuth;
    private final long timeoutMs;
    private final boolean cacheable;
    private final long cacheTtlSeconds;
    private final Method method;
    private final Object bean;
    private final Class<?> beanClass;
    
    public MCPToolMetadata(String name, String description, String category, String version,
                          String[] tags, boolean enabled, int priority, boolean requiresAuth,
                          long timeoutMs, boolean cacheable, long cacheTtlSeconds,
                          Method method, Object bean, Class<?> beanClass) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.version = version;
        this.tags = tags != null ? tags.clone() : new String[0];
        this.enabled = enabled;
        this.priority = priority;
        this.requiresAuth = requiresAuth;
        this.timeoutMs = timeoutMs;
        this.cacheable = cacheable;
        this.cacheTtlSeconds = cacheTtlSeconds;
        this.method = method;
        this.bean = bean;
        this.beanClass = beanClass;
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getVersion() { return version; }
    public String[] getTags() { return tags.clone(); }
    public boolean isEnabled() { return enabled; }
    public int getPriority() { return priority; }
    public boolean isRequiresAuth() { return requiresAuth; }
    public long getTimeoutMs() { return timeoutMs; }
    public boolean isCacheable() { return cacheable; }
    public long getCacheTtlSeconds() { return cacheTtlSeconds; }
    public Method getMethod() { return method; }
    public Object getBean() { return bean; }
    public Class<?> getBeanClass() { return beanClass; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MCPToolMetadata that = (MCPToolMetadata) o;
        return Objects.equals(name, that.name) && 
               Objects.equals(beanClass, that.beanClass) &&
               Objects.equals(method, that.method);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, beanClass, method);
    }
    
    @Override
    public String toString() {
        return "MCPToolMetadata{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", version='" + version + '\'' +
                ", tags=" + Arrays.toString(tags) +
                ", enabled=" + enabled +
                ", priority=" + priority +
                ", requiresAuth=" + requiresAuth +
                ", timeoutMs=" + timeoutMs +
                ", cacheable=" + cacheable +
                ", cacheTtlSeconds=" + cacheTtlSeconds +
                ", method=" + method.getName() +
                ", beanClass=" + beanClass.getSimpleName() +
                '}';
    }
}
