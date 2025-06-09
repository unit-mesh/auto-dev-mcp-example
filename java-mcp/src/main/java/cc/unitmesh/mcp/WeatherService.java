package cc.unitmesh.mcp;

import cc.unitmesh.mcp.annotation.MCPTool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

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
    @Tool(description = "Get weather forecast for a specific latitude/longitude")
    public String getWeatherForecastByLocation(double latitude, double longitude) {
        return String.format("Weather forecast for location (%f, %f): Sunny, 25°C", latitude, longitude);
    }
}
