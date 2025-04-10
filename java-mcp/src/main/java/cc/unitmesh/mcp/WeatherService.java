package cc.unitmesh.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {
    @Tool(description = "Get weather forecast for a specific latitude/longitude")
    public String getWeatherForecastByLocation(double latitude, double longitude) {
        return String.format("Weather forecast for location (%f, %f): Sunny, 25°C", latitude, longitude);
    }

    public static void main(String[] args) {
        WeatherService client = new WeatherService();
        System.out.println(client.getWeatherForecastByLocation(47.6062, -122.3321));
    }
}
