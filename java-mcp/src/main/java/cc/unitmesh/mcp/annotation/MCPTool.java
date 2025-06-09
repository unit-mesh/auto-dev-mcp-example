package cc.unitmesh.mcp.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark a method as an MCP (Model Context Protocol) tool.
 * 
 * This annotation extends the functionality of Spring AI's @Tool annotation
 * with MCP-specific features and metadata.
 * 
 * Example usage:
 * <pre>
 * {@code
 * @Service
 * public class WeatherService {
 *     
 *     @MCPTool(
 *         name = "get_weather",
 *         description = "Get weather forecast for a specific location",
 *         category = "weather",
 *         version = "1.0"
 *     )
 *     public String getWeatherForecast(double latitude, double longitude) {
 *         return String.format("Weather forecast for location (%f, %f): Sunny, 25°C", latitude, longitude);
 *     }
 * }
 * }
 * </pre>
 * 
 * @author AutoDev MCP Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MCPTool {
    
    /**
     * The name of the tool. If not specified, the method name will be used.
     */
    String name() default "";
    
    /**
     * Description of what the tool does.
     * This is required and will be used by AI models to understand the tool's purpose.
     */
    String description();
    
    /**
     * Category of the tool for organization purposes.
     * Examples: "weather", "database", "file", "network", etc.
     */
    String category() default "general";
    
    /**
     * Version of the tool.
     */
    String version() default "1.0";
    
    /**
     * Tags for additional metadata and filtering.
     */
    String[] tags() default {};
    
    /**
     * Whether this tool is enabled.
     * Can be used to temporarily disable tools without removing the annotation.
     */
    boolean enabled() default true;
    
    /**
     * Priority of the tool when multiple tools are available.
     * Higher values indicate higher priority.
     */
    int priority() default 0;
    
    /**
     * Whether this tool requires authentication or special permissions.
     */
    boolean requiresAuth() default false;
    
    /**
     * Maximum execution time in milliseconds.
     * 0 means no timeout.
     */
    long timeoutMs() default 0;
    
    /**
     * Whether to cache the results of this tool.
     */
    boolean cacheable() default false;
    
    /**
     * Cache TTL in seconds if cacheable is true.
     */
    long cacheTtlSeconds() default 300;
}
