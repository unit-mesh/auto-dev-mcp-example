package cc.unitmesh.mcp.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * MCP-specific implementation of ToolCallback for methods annotated with @MCPTool.
 * This class is inspired by Spring AI's MethodToolCallback but works directly with MCPToolMetadata.
 * 
 * @author AutoDev MCP Team
 */
public class MCPMethodToolCallback implements ToolCallback {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPMethodToolCallback.class);
    
    private final MCPToolMetadata metadata;
    private final ObjectMapper objectMapper;
    
    public MCPMethodToolCallback(MCPToolMetadata metadata) {
        Assert.notNull(metadata, "MCPToolMetadata must not be null");
        this.metadata = metadata;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public String getName() {
        return metadata.getName();
    }
    
    @Override
    public String getDescription() {
        return metadata.getDescription();
    }
    
    @Override
    public String getInputTypeSchema() {
        // Return a JSON schema string for the tool parameters
        String schema = generateJsonSchema();
        // Ensure we never return null or empty string
        return (schema != null && !schema.trim().isEmpty()) ? schema : "{}";
    }

    @Override
    public ToolDefinition getToolDefinition() {
        // Return tool definition object
        return createToolDefinition();
    }
    
    @Override
    public String call(String arguments) {
        try {
            logger.debug("Calling MCP tool '{}' with arguments: {}", getName(), arguments);
            
            // Parse arguments from JSON
            Map<String, Object> args = parseArguments(arguments);
            
            // Invoke the method
            Object result = invokeMethod(args);
            
            // Convert result to string
            String response = convertResultToString(result);
            
            logger.debug("MCP tool '{}' returned: {}", getName(), response);
            return response;
            
        } catch (Exception e) {
            logger.error("Error calling MCP tool '{}': {}", getName(), e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Parse JSON arguments into a map.
     */
    private Map<String, Object> parseArguments(String arguments) throws JsonProcessingException {
        if (arguments == null || arguments.trim().isEmpty() || "{}".equals(arguments.trim())) {
            return new HashMap<>();
        }
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(arguments, Map.class);
            return result != null ? result : new HashMap<>();
        } catch (JsonProcessingException e) {
            logger.warn("Failed to parse arguments as JSON: {}", arguments);
            throw e;
        }
    }
    
    /**
     * Invoke the method with the parsed arguments.
     */
    private Object invokeMethod(Map<String, Object> args) throws Exception {
        Method method = metadata.getMethod();
        Object bean = metadata.getBean();
        Parameter[] parameters = method.getParameters();
        
        // Prepare method arguments
        Object[] methodArgs = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            String paramName = param.getName();
            Class<?> paramType = param.getType();
            
            Object value = args.get(paramName);
            methodArgs[i] = convertArgument(value, paramType, paramName);
        }
        
        // Make method accessible if needed
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        
        // Invoke the method
        return method.invoke(bean, methodArgs);
    }
    
    /**
     * Convert argument to the required parameter type.
     */
    private Object convertArgument(Object value, Class<?> targetType, String paramName) {
        if (value == null) {
            if (targetType.isPrimitive()) {
                throw new IllegalArgumentException("Cannot pass null to primitive parameter: " + paramName);
            }
            return null;
        }
        
        // If types match, return as-is
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        
        // Handle common type conversions
        if (targetType == String.class) {
            return value.toString();
        } else if (targetType == int.class || targetType == Integer.class) {
            return convertToInteger(value, paramName);
        } else if (targetType == long.class || targetType == Long.class) {
            return convertToLong(value, paramName);
        } else if (targetType == double.class || targetType == Double.class) {
            return convertToDouble(value, paramName);
        } else if (targetType == float.class || targetType == Float.class) {
            return convertToFloat(value, paramName);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return convertToBoolean(value, paramName);
        }
        
        // For complex types, try JSON conversion
        try {
            String json = objectMapper.writeValueAsString(value);
            return objectMapper.readValue(json, targetType);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert argument '" + paramName + 
                "' from " + value.getClass().getSimpleName() + " to " + targetType.getSimpleName(), e);
        }
    }
    
    private Integer convertToInteger(Object value, String paramName) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert parameter '" + paramName + "' to integer: " + value);
        }
    }
    
    private Long convertToLong(Object value, String paramName) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert parameter '" + paramName + "' to long: " + value);
        }
    }
    
    private Double convertToDouble(Object value, String paramName) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert parameter '" + paramName + "' to double: " + value);
        }
    }
    
    private Float convertToFloat(Object value, String paramName) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        try {
            return Float.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cannot convert parameter '" + paramName + "' to float: " + value);
        }
    }
    
    private Boolean convertToBoolean(Object value, String paramName) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String str = value.toString().toLowerCase();
        if ("true".equals(str) || "1".equals(str)) {
            return true;
        } else if ("false".equals(str) || "0".equals(str)) {
            return false;
        }
        throw new IllegalArgumentException("Cannot convert parameter '" + paramName + "' to boolean: " + value);
    }
    
    /**
     * Convert method result to string.
     */
    private String convertResultToString(Object result) throws JsonProcessingException {
        if (result == null) {
            return "null";
        } else if (result instanceof String) {
            return (String) result;
        } else {
            // Convert complex objects to JSON
            return objectMapper.writeValueAsString(result);
        }
    }
    
    /**
     * Generate JSON schema for tool parameters.
     */
    private String generateJsonSchema() {
        try {
            Method method = metadata.getMethod();
            Parameter[] parameters = method.getParameters();

            Map<String, Object> schema = new HashMap<>();
            schema.put("type", "object");

            Map<String, Object> properties = new HashMap<>();
            for (Parameter param : parameters) {
                Map<String, Object> paramSchema = new HashMap<>();
                Class<?> paramType = param.getType();

                if (paramType == String.class) {
                    paramSchema.put("type", "string");
                } else if (paramType == int.class || paramType == Integer.class) {
                    paramSchema.put("type", "integer");
                } else if (paramType == long.class || paramType == Long.class) {
                    paramSchema.put("type", "integer");
                } else if (paramType == double.class || paramType == Double.class) {
                    paramSchema.put("type", "number");
                } else if (paramType == float.class || paramType == Float.class) {
                    paramSchema.put("type", "number");
                } else if (paramType == boolean.class || paramType == Boolean.class) {
                    paramSchema.put("type", "boolean");
                } else {
                    paramSchema.put("type", "object");
                }

                properties.put(param.getName(), paramSchema);
            }

            schema.put("properties", properties);

            String result = objectMapper.writeValueAsString(schema);
            // Ensure we return a valid non-empty JSON object
            return (result != null && !result.trim().isEmpty()) ? result : "{\"type\":\"object\",\"properties\":{}}";
        } catch (Exception e) {
            logger.warn("Failed to generate JSON schema for tool: {}", getName(), e);
            return "{\"type\":\"object\",\"properties\":{}}";
        }
    }

    /**
     * Create tool definition object.
     */
    private ToolDefinition createToolDefinition() {
        try {
            String inputSchema = getInputTypeSchema();
            return ToolDefinition.builder()
                .name(getName())
                .description(getDescription())
                .inputSchema(inputSchema)
                .build();
        } catch (Exception e) {
            logger.warn("Failed to create tool definition for tool: {}", getName(), e);
            // Return a basic tool definition with minimal schema
            return ToolDefinition.builder()
                .name(getName())
                .description(getDescription())
                .inputSchema("{\"type\":\"object\",\"properties\":{}}")
                .build();
        }
    }

    /**
     * Get the tool metadata.
     */
    public MCPToolMetadata getMetadata() {
        return metadata;
    }
}
