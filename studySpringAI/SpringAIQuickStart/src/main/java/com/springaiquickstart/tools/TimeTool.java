package com.springaiquickstart.tools;

import com.springaiquickstart.mcp.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 时间查询工具
 * 提供当前实际时间和日期信息
 */
@Component
public class TimeTool implements Tool {

    @Override
    public String getName() {
        System.out.println("========== TimeTool.getName() 被调用 ==========");
        return "time_query";
    }

    @Override
    public String getDescription() {
        System.out.println("========== TimeTool.getDescription() 被调用 ==========");
        return "获取当前实际时间和日期信息，包括年月日、时分秒、星期几等详细信息";
    }

    @Override
    public Map<String, Object> getParameters() {
        System.out.println("========== TimeTool.getParameters() 被调用 ==========");
        
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        params.put("properties", new HashMap<>());
        params.put("required", java.util.Collections.emptyList());
        
        System.out.println("返回参数定义: " + params);
        return params;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> parameters) {
        System.out.println("========================================");
        System.out.println("TimeTool.execute() 开始执行");
        System.out.println("接收到的参数: " + parameters);
        System.out.println("========================================");
        
        try {
            // 获取实际当前时间
            LocalDateTime now = LocalDateTime.now();
            System.out.println("获取到的实际系统时间: " + now);
            
            Map<String, Object> result = new HashMap<>();
            
            // 格式化时间信息
            String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String currentDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String currentDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // 存储时间信息
            result.put("currentTime", currentTime);
            result.put("currentDate", currentDate);
            result.put("currentDateTime", currentDateTime);
            result.put("year", now.getYear());
            result.put("month", now.getMonthValue());
            result.put("day", now.getDayOfMonth());
            result.put("hour", now.getHour());
            result.put("minute", now.getMinute());
            result.put("second", now.getSecond());
            
            // 星期信息
            String[] weekDays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
            String dayOfWeek = weekDays[now.getDayOfWeek().getValue() - 1];
            result.put("dayOfWeek", dayOfWeek);
            
            // 时间戳
            long timestamp = System.currentTimeMillis();
            result.put("timestamp", timestamp);
            
            // 状态信息
            result.put("status", "success");
            result.put("message", "时间查询成功 - 使用实际系统时间");
            
            // 输出详细信息
            System.out.println("========================================");
            System.out.println("时间查询结果 (实际系统时间):");
            System.out.println("  当前时间: " + currentTime);
            System.out.println("  当前日期: " + currentDate);
            System.out.println("  完整日期时间: " + currentDateTime);
            System.out.println("  年: " + now.getYear());
            System.out.println("  月: " + now.getMonthValue());
            System.out.println("  日: " + now.getDayOfMonth());
            System.out.println("  时: " + now.getHour());
            System.out.println("  分: " + now.getMinute());
            System.out.println("  秒: " + now.getSecond());
            System.out.println("  星期: " + dayOfWeek);
            System.out.println("  时间戳: " + timestamp);
            System.out.println("========================================");
            
            return result;
            
        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("TimeTool执行出错: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================================");
            
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "error");
            errorResult.put("message", "时间查询失败: " + e.getMessage());
            errorResult.put("error", e.getClass().getName());
            return errorResult;
        }
    }
}
