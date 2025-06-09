package cc.unitmesh.mcp.config;

import cc.unitmesh.mcp.annotation.EnableMCP;
import cc.unitmesh.mcp.annotation.MCPTool;
import cc.unitmesh.mcp.core.MCPToolCallbackProvider;
import cc.unitmesh.mcp.core.MCPToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Auto-configuration for MCP (Model Context Protocol) functionality.
 * 
 * This configuration class is automatically imported when @EnableMCP is used.
 * It sets up the necessary beans and scans for MCP tools.
 * 
 * @author AutoDev MCP Team
 */
@Configuration
public class MCPAutoConfiguration implements ImportAware {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPAutoConfiguration.class);
    
    private AnnotationAttributes enableMCPAttributes;
    
    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableMCPAttributes = AnnotationAttributes.fromMap(
            importMetadata.getAnnotationAttributes(EnableMCP.class.getName(), false));
        
        if (this.enableMCPAttributes == null) {
            throw new IllegalArgumentException(
                "@EnableMCP is not present on importing class " + importMetadata.getClassName());
        }
    }
    
    @Bean
    @ConditionalOnMissingBean
    public MCPToolRegistry mcpToolRegistry() {
        return new MCPToolRegistry();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public MCPToolCallbackProvider mcpToolCallbackProvider(MCPToolRegistry toolRegistry) {
        return new MCPToolCallbackProvider(toolRegistry);
    }
    

    
    @Bean
    public MCPToolScanner mcpToolScanner(MCPToolRegistry toolRegistry, 
                                        MCPToolCallbackProvider callbackProvider) {
        Set<String> basePackages = getBasePackages();
        boolean autoDiscovery = enableMCPAttributes.getBoolean("enableAutoDiscovery");
        
        return new MCPToolScanner(toolRegistry, callbackProvider, basePackages, autoDiscovery);
    }
    
    /**
     * Get base packages to scan for MCP tools.
     */
    private Set<String> getBasePackages() {
        Set<String> basePackages = new HashSet<>();
        
        // Add explicitly specified base packages
        String[] packages = enableMCPAttributes.getStringArray("basePackages");
        if (packages != null) {
            basePackages.addAll(Arrays.asList(packages));
        }
        
        // Add base package classes
        Class<?>[] basePackageClasses = enableMCPAttributes.getClassArray("basePackageClasses");
        if (basePackageClasses != null) {
            for (Class<?> clazz : basePackageClasses) {
                basePackages.add(ClassUtils.getPackageName(clazz));
            }
        }
        
        // If no base packages specified, use the package of the @EnableMCP class
        if (basePackages.isEmpty()) {
            // This will be set by the ImportAware mechanism
            // For now, we'll use a default package scanning approach
            basePackages.add(""); // Empty string means scan all packages
        }
        
        return basePackages;
    }
    
    /**
     * Bean post processor to scan for MCP tools.
     */
    public static class MCPToolScanner implements BeanPostProcessor {
        
        private static final Logger logger = LoggerFactory.getLogger(MCPToolScanner.class);
        
        private final MCPToolRegistry toolRegistry;
        private final MCPToolCallbackProvider callbackProvider;
        private final Set<String> basePackages;
        private final boolean autoDiscovery;
        
        public MCPToolScanner(MCPToolRegistry toolRegistry, 
                             MCPToolCallbackProvider callbackProvider,
                             Set<String> basePackages, 
                             boolean autoDiscovery) {
            this.toolRegistry = toolRegistry;
            this.callbackProvider = callbackProvider;
            this.basePackages = basePackages;
            this.autoDiscovery = autoDiscovery;
        }
        
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (!autoDiscovery) {
                return bean;
            }
            
            Class<?> beanClass = bean.getClass();
            
            // Check if bean is in the packages we want to scan
            if (!shouldScanBean(beanClass)) {
                return bean;
            }
            
            // Scan for @MCPTool annotated methods
            Method[] methods = beanClass.getDeclaredMethods();
            for (Method method : methods) {
                MCPTool mcpTool = method.getAnnotation(MCPTool.class);
                if (mcpTool != null) {
                    try {
                        toolRegistry.registerTool(bean, method, mcpTool);
                        // Refresh callbacks to include the new tool
                        callbackProvider.refreshToolCallbacks();
                    } catch (Exception e) {
                        logger.error("Failed to register MCP tool: {}.{}", 
                            beanClass.getSimpleName(), method.getName(), e);
                    }
                }
            }
            
            return bean;
        }
        
        /**
         * Check if a bean should be scanned based on base packages.
         */
        private boolean shouldScanBean(Class<?> beanClass) {
            if (basePackages.isEmpty() || basePackages.contains("")) {
                return true; // Scan all packages
            }
            
            String beanPackage = ClassUtils.getPackageName(beanClass);
            return basePackages.stream()
                .anyMatch(basePackage -> 
                    StringUtils.hasText(basePackage) && 
                    beanPackage.startsWith(basePackage));
        }
    }
}
