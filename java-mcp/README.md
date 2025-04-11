# Java MCP Example

Resources:

- [Spring AI - Model Context Protocol (MCP)](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-overview.html)
- [GitHub Java SDK](https://github.com/modelcontextprotocol/java-sdk)
- [Java MCP Server](https://modelcontextprotocol.io/sdk/java/mcp-server)

## 1. Setup Dependencies

```xml
<dependencies>
	<dependency>
		<groupId>org.springframework.ai</groupId>
		<artifactId>spring-ai-mcp-server-spring-boot-starter</artifactId>
	</dependency>

	<dependency>
		<groupId>io.modelcontextprotocol.sdk</groupId>
		<artifactId>mcp</artifactId>
		<version>0.8.1</version>
	</dependency>

	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-test</artifactId>
		<scope>test</scope>
	</dependency>
</dependencies>
```

## 2. Create ToolCallbackProvider


```java
@SpringBootApplication
public class McpApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider weatherTools(WeatherService weatherService) {
		return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
	}
}
```

## 3. Create tool

```java
@Service
public class WeatherService {
    @Tool(description = "Get weather forecast for a specific latitude/longitude")
    public String getWeatherForecastByLocation(double latitude, double longitude) {
        return String.format("Weather forecast for location (%f, %f): Sunny, 25°C", latitude, longitude);
    }
}
```