package cc.unitmesh.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider weatherTools(WeatherService weatherService, SqlService sqlService) {
		return MethodToolCallbackProvider.builder().toolObjects(weatherService, sqlService).build();
	}
}
