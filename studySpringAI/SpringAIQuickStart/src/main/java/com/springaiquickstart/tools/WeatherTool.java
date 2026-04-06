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

    // 中英文城市名映射
    private static final Map<String, String> CITY_NAME_MAP = new HashMap<>();
    static {
        CITY_NAME_MAP.put("北京", "Beijing");
        CITY_NAME_MAP.put("上海", "Shanghai");
        CITY_NAME_MAP.put("广州", "Guangzhou");
        CITY_NAME_MAP.put("深圳", "Shenzhen");
        CITY_NAME_MAP.put("杭州", "Hangzhou");
        CITY_NAME_MAP.put("南京", "Nanjing");
        CITY_NAME_MAP.put("成都", "Chengdu");
        CITY_NAME_MAP.put("武汉", "Wuhan");
        CITY_NAME_MAP.put("西安", "Xi'an");
        CITY_NAME_MAP.put("重庆", "Chongqing");
        CITY_NAME_MAP.put("天津", "Tianjin");
        CITY_NAME_MAP.put("苏州", "Suzhou");
        CITY_NAME_MAP.put("郑州", "Zhengzhou");
        CITY_NAME_MAP.put("长沙", "Changsha");
        CITY_NAME_MAP.put("沈阳", "Shenyang");
        CITY_NAME_MAP.put("青岛", "Qingdao");
        CITY_NAME_MAP.put("宁波", "Ningbo");
        CITY_NAME_MAP.put("东莞", "Dongguan");
        CITY_NAME_MAP.put("无锡", "Wuxi");
        CITY_NAME_MAP.put("佛山", "Foshan");
        CITY_NAME_MAP.put("合肥", "Hefei");
        CITY_NAME_MAP.put("大连", "Dalian");
        CITY_NAME_MAP.put("福州", "Fuzhou");
        CITY_NAME_MAP.put("厦门", "Xiamen");
        CITY_NAME_MAP.put("哈尔滨", "Harbin");
        CITY_NAME_MAP.put("济南", "Jinan");
        CITY_NAME_MAP.put("温州", "Wenzhou");
        CITY_NAME_MAP.put("南宁", "Nanning");
        CITY_NAME_MAP.put("长春", "Changchun");
        CITY_NAME_MAP.put("泉州", "Quanzhou");
        CITY_NAME_MAP.put("石家庄", "Shijiazhuang");
        CITY_NAME_MAP.put("贵阳", "Guiyang");
        CITY_NAME_MAP.put("南昌", "Nanchang");
        CITY_NAME_MAP.put("金华", "Jinhua");
        CITY_NAME_MAP.put("常州", "Changzhou");
        CITY_NAME_MAP.put("珠海", "Zhuhai");
        CITY_NAME_MAP.put("惠州", "Huizhou");
        CITY_NAME_MAP.put("嘉兴", "Jiaxing");
        CITY_NAME_MAP.put("南通", "Nantong");
        CITY_NAME_MAP.put("中山", "Zhongshan");
        CITY_NAME_MAP.put("保定", "Baoding");
        CITY_NAME_MAP.put("兰州", "Lanzhou");
        CITY_NAME_MAP.put("台州", "Taizhou");
        CITY_NAME_MAP.put("徐州", "Xuzhou");
        CITY_NAME_MAP.put("太原", "Taiyuan");
        CITY_NAME_MAP.put("绍兴", "Shaoxing");
        CITY_NAME_MAP.put("烟台", "Yantai");
        CITY_NAME_MAP.put("海口", "Haikou");
        CITY_NAME_MAP.put("乌鲁木齐", "Urumqi");
        CITY_NAME_MAP.put("呼和浩特", "Hohhot");
        CITY_NAME_MAP.put("银川", "Yinchuan");
        CITY_NAME_MAP.put("西宁", "Xining");
        CITY_NAME_MAP.put("拉萨", "Lhasa");
        CITY_NAME_MAP.put("昆明", "Kunming");
        CITY_NAME_MAP.put("香港", "Hong Kong");
        CITY_NAME_MAP.put("澳门", "Macau");
        CITY_NAME_MAP.put("台北", "Taipei");
        CITY_NAME_MAP.put("高雄", "Kaohsiung");
    }

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
        city.put("description", "城市名称，支持中文或英文，例如：北京、Beijing、上海、Shanghai");
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
        String cityInput = (String) parameters.get("city");
        String dateInput = (String) parameters.getOrDefault("date", "今天");
        
        // 转换中文城市名为英文
        String city = convertCityName(cityInput);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 解析日期
            LocalDate targetDate = parseDateInput(dateInput);
            LocalDate today = LocalDate.now();
            long daysDiff = ChronoUnit.DAYS.between(today, targetDate);
            
            // 根据日期差选择不同的API
            if (daysDiff == 0) {
                // 今天：使用当前天气API
                result = getCurrentWeather(city, targetDate, cityInput);
            } else if (daysDiff > 0 && daysDiff <= 5) {
                // 未来1-5天：使用预报API
                result = getForecastWeather(city, targetDate, daysDiff, cityInput);
            } else if (daysDiff < 0) {
                // 历史日期：历史API需要付费，返回提示
                result.put("city", cityInput);
                result.put("date", targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                result.put("status", "error");
                result.put("message", "历史天气API错误: 需要付费订阅才能查询历史天气数据（OpenWeatherMap History API需要付费订阅）");
            } else {
                // 超过5天：无法查询
                result.put("city", cityInput);
                result.put("date", targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                result.put("status", "error");
                result.put("message", "无法查询超过5天的天气预报");
            }
        } catch (Exception e) {
            result.put("city", cityInput);
            result.put("status", "error");
            result.put("message", "天气查询失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 转换城市名：中文转英文，英文保持不变
     */
    private String convertCityName(String cityInput) {
        if (cityInput == null || cityInput.trim().isEmpty()) {
            return cityInput;
        }
        
        String trimmedCity = cityInput.trim();
        
        // 如果是中文城市名，转换为英文
        if (CITY_NAME_MAP.containsKey(trimmedCity)) {
            return CITY_NAME_MAP.get(trimmedCity);
        }
        
        // 去掉常见的"市"后缀再尝试匹配
        if (trimmedCity.endsWith("市")) {
            String cityWithoutSuffix = trimmedCity.substring(0, trimmedCity.length() - 1);
            if (CITY_NAME_MAP.containsKey(cityWithoutSuffix)) {
                return CITY_NAME_MAP.get(cityWithoutSuffix);
            }
        }
        
        // 直接返回原值（假设是英文城市名）
        return trimmedCity;
    }

    /**
     * 解析日期输入
     * 支持中文日期和相对日期
     */
    private LocalDate parseDateInput(String dateInput) {
        LocalDate today = LocalDate.now();
        
        if (dateInput == null || dateInput.trim().isEmpty()) {
            return today;
        }
        
        String input = dateInput.trim();
        
        // 处理中文日期
        switch (input) {
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
        if (input.matches("\\d+天后")) {
            int days = Integer.parseInt(input.replace("天后", ""));
            return today.plusDays(days);
        }
        if (input.matches("\\d+天前")) {
            int days = Integer.parseInt(input.replace("天前", ""));
            return today.minusDays(days);
        }
        
        // 处理标准日期格式（YYYY-MM-DD）
        try {
            return LocalDate.parse(input, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            // 默认返回今天
            return today;
        }
    }

    /**
     * 获取当前天气
     */
    private Map<String, Object> getCurrentWeather(String city, LocalDate date, String originalCityName) throws IOException, InterruptedException {
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
            result.put("city", originalCityName);
            result.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            result.put("temperature", root.path("main").path("temp").asDouble() + "°C");
            result.put("condition", root.path("weather").get(0).path("description").asText());
            result.put("humidity", root.path("main").path("humidity").asInt() + "%");
            result.put("wind", root.path("wind").path("speed").asDouble() + " m/s");
            result.put("status", "success");
            
            return result;
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("city", originalCityName);
            result.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            result.put("status", "error");
            result.put("message", "城市未找到或API错误，HTTP状态码: " + response.statusCode());
            result.put("raw_response", response.body());
            return result;
        }
    }

    /**
     * 获取未来天气预报
     */
    private Map<String, Object> getForecastWeather(String city, LocalDate targetDate, long daysDiff, String originalCityName) throws IOException, InterruptedException {
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
                        result.put("city", originalCityName);
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
            
            // 如果没找到12点的数据，使用第一个可用的数据
            for (JsonNode item : list) {
                String dateTime = item.path("dt_txt").asText();
                if (dateTime.startsWith(targetDateStr)) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("city", originalCityName);
                    result.put("date", targetDateStr);
                    result.put("temperature", item.path("main").path("temp").asDouble() + "°C");
                    result.put("condition", item.path("weather").get(0).path("description").asText());
                    result.put("humidity", item.path("main").path("humidity").asInt() + "%");
                    result.put("wind", item.path("wind").path("speed").asDouble() + " m/s");
                    result.put("status", "success");
                    return result;
                }
            }
            
            // 没有找到数据
            Map<String, Object> result = new HashMap<>();
            result.put("city", originalCityName);
            result.put("date", targetDateStr);
            result.put("status", "error");
            result.put("message", "未找到该日期的天气预报数据");
            return result;
        } else {
            Map<String, Object> result = new HashMap<>();
            result.put("city", originalCityName);
            result.put("date", targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            result.put("status", "error");
            result.put("message", "城市未找到或API错误，HTTP状态码: " + response.statusCode());
            result.put("raw_response", response.body());
            return result;
        }
    }
}
