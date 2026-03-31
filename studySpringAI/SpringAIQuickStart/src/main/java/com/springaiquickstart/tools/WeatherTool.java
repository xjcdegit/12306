package com.springaiquickstart.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springaiquickstart.mcp.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 天气查询工具
 * 
 * 使用OpenWeatherMap API获取真实天气数据
 * API文档：https://openweathermap.org/api
 * 
 * 使用前需要：
 * 1. 注册OpenWeatherMap账号：https://openweathermap.org/
 * 2. 创建应用获取API Key
 * 3. 在application.yml中配置：weather.api.key=你的key
 * 
 * 功能说明：
 * - 今天：使用Current Weather API（免费）
 * - 未来1-5天：使用5 Day Forecast API（免费）
 * - 历史日期：使用History API（需要付费订阅）
 * 
 * 支持的日期格式：
 * - YYYY-MM-DD（如：2026-03-31）
 * - 中文日期（如：今天、明天、后天）
 * - 相对日期（如：3天后、2天前）
 */
@Component
public class WeatherTool implements Tool {

    @Value("${weather.api.key:0e3f0ff06cbb5f22699a3d61ada8a61b}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "weather_query";
    }

    @Override
    public String getDescription() {
        return "查询指定城市的天气信息，支持今天和未来5天的天气查询";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        
        Map<String, Object> city = new HashMap<>();
        city.put("type", "string");
        city.put("description", "城市名称，例如：北京、上海、广州");
        properties.put("city", city);
        
        Map<String, Object> date = new HashMap<>();
        date.put("type", "string");
        date.put("description", "查询日期，支持：今天、明天、后天、YYYY-MM-DD格式");
        properties.put("date", date);
        
        params.put("properties", properties);
        params.put("required", Arrays.asList("city"));
        
        return params;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> parameters) {
        String city = (String) parameters.get("city");
        String dateInput = (String) parameters.getOrDefault("date", "今天");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 解析日期
            LocalDate targetDate = parseDateInput(dateInput);
            LocalDate today = LocalDate.now();
            long daysDiff = ChronoUnit.DAYS.between(today, targetDate);
            
            // 根据日期差选择不同的API
            if (daysDiff == 0) {
                // 今天：使用当前天气API
                result = getCurrentWeather(city, targetDate);
            } else if (daysDiff > 0 && daysDiff <= 5) {
                // 未来1-5天：使用预报API
                result = getForecastWeather(city, targetDate, daysDiff);
            } else if (daysDiff < 0) {
                // 历史日期：历史API需要付费，返回提示
                result.put("city", city);
                result.put("date", targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                result.put("status", "error");
                result.put("message", "历史天气API错误: 需要付费订阅才能查询历史天气数据（OpenWeatherMap History API需要付费订阅）");
            } else {
                // 超过5天：无法查询
                result.put("city", city);
                result.put("date", targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                result.put("status", "error");
                result.put("message", "无法查询超过5天的天气预报");
            }
        } catch (Exception e) {
            result.put("city", city);
            result.put("status", "error");
            result.put("message", "天气查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 解析日期输入
     * 支持中文日期和相对日期
     */
    private LocalDate parseDateInput(String dateInput) {
        LocalDate today = LocalDate.now();
        
        // 处理中文日期
        switch (dateInput) {
            case "今天":
            case "今日":
                return today;
            case "明天":
            case "明日":
                return today.plusDays(1);
            case "后天":
                return today.plusDays(2);
            case "大后天":
                return today.plusDays(3);
        }
        
        // 处理相对日期（如：3天后、2天前）
        if (dateInput.matches("\\d+天后")) {
            int days = Integer.parseInt(dateInput.replace("天后", ""));
            return today.plusDays(days);
        }
        if (dateInput.matches("\\d+天前")) {
            int days = Integer.parseInt(dateInput.replace("天前", ""));
            return today.minusDays(days);
        }
        
        // 处理标准日期格式（YYYY-MM-DD）
        try {
            return LocalDate.parse(dateInput, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            // 默认返回今天
            return today;
        }
    }

    /**
     * 获取当前天气
     */
    private Map<String, Object> getCurrentWeather(String city, LocalDate date) throws IOException, InterruptedException {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = String.format("https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric&lang=zh_cn", 
            encodedCity, apiKey);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            
            Map<String, Object> result = new HashMap<>();
            result.put("city", root.path("name").asText());
            result.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            result.put("temperature", root.path("main").path("temp").asDouble() + "°C");
            result.put("condition", root.path("weather").get(0).path("description").asText());
            result.put("humidity", root.path("main").path("humidity").asInt() + "%");
            result.put("wind", root.path("wind").path("speed").asDouble() + " m/s");
            result.put("status", "success");
            
            return result;
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("city", city);
            result.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            result.put("status", "error");
            result.put("message", "城市未找到或API错误");
            return result;
        }
    }

    /**
     * 获取未来天气预报
     */
    private Map<String, Object> getForecastWeather(String city, LocalDate targetDate, long daysDiff) throws IOException, InterruptedException {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = String.format("https://api.openweathermap.org/data/2.5/forecast?q=%s&appid=%s&units=metric&lang=zh_cn", 
            encodedCity, apiKey);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode list = root.path("list");
            
            // 查找目标日期的天气数据
            String targetDateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            for (JsonNode item : list) {
                String dateTime = item.path("dt_txt").asText();
                if (dateTime.startsWith(targetDateStr)) {
                    // 找到目标日期的数据，使用中午12点的数据
                    if (dateTime.contains("12:00:00")) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("city", root.path("city").path("name").asText());
                        result.put("date", targetDateStr);
                        result.put("temperature", item.path("main").path("temp").asDouble() + "°C");
                        result.put("condition", item.path("weather").get(0).path("description").asText());
                        result.put("humidity", item.path("main").path("humidity").asInt() + "%");
                        result.put("wind", item.path("wind").path("speed").asDouble() + " m/s");
                        result.put("status", "success");
                        
                        return result;
                    }
                }
            }
            
            // 如果没有找到12点的数据，返回第一个匹配的数据
            for (JsonNode item : list) {
                String dateTime = item.path("dt_txt").asText();
                if (dateTime.startsWith(targetDateStr)) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("city", root.path("city").path("name").asText());
                    result.put("date", targetDateStr);
                    result.put("temperature", item.path("main").path("temp").asDouble() + "°C");
                    result.put("condition", item.path("weather").get(0).path("description").asText());
                    result.put("humidity", item.path("main").path("humidity").asInt() + "%");
                    result.put("wind", item.path("wind").path("speed").asDouble() + " m/s");
                    result.put("status", "success");
                    
                    return result;
                }
            }
            
            // 没有找到目标日期的数据
            Map<String, Object> result = new HashMap<>();
            result.put("city", city);
            result.put("date", targetDateStr);
            result.put("status", "error");
            result.put("message", "无法获取" + daysDiff + "天后的天气数据");
            return result;
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("city", city);
            result.put("date", targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            result.put("status", "error");
            result.put("message", "城市未找到或API错误");
            return result;
        }
    }
}
