package cc.unitmesh.mcp.annotation;

import cc.unitmesh.mcp.config.MCPAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable MCP (Model Context Protocol) functionality in Spring Boot application.
 * 
 * This annotation should be placed on the main application class or any configuration class
 * to enable automatic discovery and registration of MCP tools.
 * 
 * Example usage:
 * <pre>
 * {@code
 * @SpringBootApplication
 * @EnableMCP
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * }
 * </pre>
 * 
 * @author AutoDev MCP Team
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MCPAutoConfiguration.class)
public @interface EnableMCP {
    
    /**
     * Base packages to scan for MCP tools.
     * If empty, will scan from the package of the class that declares this annotation.
     */
    String[] basePackages() default {};
    
    /**
     * Base package classes to scan for MCP tools.
     * Alternative to basePackages() for type-safe package specification.
     */
    Class<?>[] basePackageClasses() default {};
    
    /**
     * Whether to enable automatic tool discovery.
     * Default is true.
     */
    boolean enableAutoDiscovery() default true;
}
