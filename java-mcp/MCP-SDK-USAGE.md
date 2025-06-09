# MCP SDK 使用指南

这个文档介绍了如何使用封装后的 MCP SDK，让开发者能够更简洁地创建和管理 MCP 工具。

## 快速开始

### 1. 启用 MCP 功能

在你的 Spring Boot 应用主类上添加 `@EnableMCP` 注解：

```java
@SpringBootApplication
@EnableMCP(basePackages = "cc.unitmesh.mcp")
public class McpApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpApplication.class, args);
    }
}
```

### 2. 创建 MCP 工具

使用 `@MCPTool` 注解来标记方法作为 MCP 工具：

```java
@Service
public class WeatherService {
    
    @MCPTool(
        name = "get_weather_forecast",
        description = "Get weather forecast for a specific latitude/longitude",
        category = "weather",
        version = "1.0",
        tags = {"weather", "forecast", "location"},
        cacheable = true,
        cacheTtlSeconds = 300
    )
    public String getWeatherForecastByLocation(double latitude, double longitude) {
        return String.format("Weather forecast for location (%f, %f): Sunny, 25°C", latitude, longitude);
    }
}
```

## @EnableMCP 注解配置

### 基本配置

```java
@EnableMCP
```

### 指定扫描包

```java
@EnableMCP(basePackages = {"com.example.tools", "com.example.services"})
```

### 使用类型安全的包指定

```java
@EnableMCP(basePackageClasses = {WeatherService.class, DatabaseService.class})
```

### 禁用自动发现

```java
@EnableMCP(enableAutoDiscovery = false)
```

## @MCPTool 注解详解

### 基本属性

- **name**: 工具名称（可选，默认使用方法名）
- **description**: 工具描述（必需）
- **category**: 工具分类（默认 "general"）
- **version**: 工具版本（默认 "1.0"）

### 元数据属性

- **tags**: 标签数组，用于分类和搜索
- **enabled**: 是否启用（默认 true）
- **priority**: 优先级（默认 0，数值越高优先级越高）

### 安全和性能属性

- **requiresAuth**: 是否需要认证（默认 false）
- **timeoutMs**: 超时时间（毫秒，0 表示无超时）
- **cacheable**: 是否可缓存（默认 false）
- **cacheTtlSeconds**: 缓存 TTL（秒，默认 300）

## 完整示例

### 数据库工具服务

```java
@Service
public class SqlService {
    
    private final JdbcTemplate jdbcTemplate;
    
    public SqlService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @MCPTool(
        name = "query_sql",
        description = "Execute a select SQL query and return results",
        category = "database",
        version = "1.0",
        tags = {"sql", "query", "database"},
        timeoutMs = 30000,
        requiresAuth = true
    )
    public List<Map<String, Object>> queryBySql(String sql) {
        if (!sql.toLowerCase().startsWith("select")) {
            throw new RuntimeException("Only SELECT queries are allowed.");
        }
        return jdbcTemplate.queryForList(sql);
    }
    
    @MCPTool(
        name = "list_tables",
        description = "Return all table names in the database",
        category = "database",
        version = "1.0",
        tags = {"sql", "tables", "schema", "database"},
        cacheable = true,
        cacheTtlSeconds = 600
    )
    public String listAllTablesName() {
        List<Map<String, Object>> tableNames = jdbcTemplate.queryForList(
            "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()");
        return tableNames.stream()
            .map(e -> e.values().iterator().next().toString())
            .collect(Collectors.joining(","));
    }
}
```

### 文件操作工具服务

```java
@Service
public class FileService {
    
    @MCPTool(
        name = "read_file",
        description = "Read the contents of a text file",
        category = "file",
        version = "1.0",
        tags = {"file", "read", "io"},
        timeoutMs = 5000,
        cacheable = true,
        cacheTtlSeconds = 60
    )
    public String readFile(String filePath) {
        try {
            return Files.readString(Paths.get(filePath));
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }
    
    @MCPTool(
        name = "write_file",
        description = "Write content to a text file",
        category = "file",
        version = "1.0",
        tags = {"file", "write", "io", "create"},
        requiresAuth = true,
        timeoutMs = 10000,
        priority = 2
    )
    public String writeFile(String filePath, String content) {
        try {
            Files.writeString(Paths.get(filePath), content);
            return "Successfully wrote to file: " + filePath;
        } catch (IOException e) {
            return "Error writing file: " + e.getMessage();
        }
    }
}
```

## 与原有方式的对比

### 原有方式

```java
@SpringBootApplication
public class McpApplication {
    @Bean
    public ToolCallbackProvider weatherTools(WeatherService weatherService, SqlService sqlService) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherService, sqlService).build();
    }
}

@Service
public class WeatherService {
    @Tool(description = "Get weather forecast for a specific latitude/longitude")
    public String getWeatherForecastByLocation(double latitude, double longitude) {
        return String.format("Weather forecast for location (%f, %f): Sunny, 25°C", latitude, longitude);
    }
}
```

### 新的方式

```java
@SpringBootApplication
@EnableMCP
public class McpApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpApplication.class, args);
    }
}

@Service
public class WeatherService {
    @MCPTool(
        name = "get_weather_forecast",
        description = "Get weather forecast for a specific latitude/longitude",
        category = "weather",
        cacheable = true
    )
    public String getWeatherForecastByLocation(double latitude, double longitude) {
        return String.format("Weather forecast for location (%f, %f): Sunny, 25°C", latitude, longitude);
    }
}
```

## 优势

1. **更简洁**: 只需要 `@EnableMCP` 注解即可启用功能
2. **自动发现**: 自动扫描和注册 MCP 工具，无需手动配置 Bean
3. **丰富的元数据**: `@MCPTool` 提供更多配置选项
4. **类型安全**: 编译时检查，减少运行时错误
5. **易于维护**: 工具定义和配置集中在一处
6. **向后兼容**: 仍然支持原有的 Spring AI 工具系统

## 注意事项

1. 确保所有使用 `@MCPTool` 的类都被 Spring 管理（如使用 `@Service`、`@Component` 等注解）
2. `description` 属性是必需的，用于 AI 模型理解工具用途
3. 工具名称在同一应用中必须唯一
4. 使用 `requiresAuth = true` 的工具需要额外的安全配置
5. 缓存功能需要 Spring Cache 支持
