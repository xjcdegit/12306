package com.springaiquickstart.tools;

import com.springaiquickstart.mcp.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 计算器工具
 * 提供基本的数学计算功能
 */
@Component
public class CalculatorTool implements Tool {

    @Override
    public String getName() {
        return "calculator";
    }

    @Override
    public String getDescription() {
        return "执行基本的数学计算，支持加减乘除和幂运算";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> expression = new HashMap<>();
        expression.put("type", "string");
        expression.put("description", "数学表达式，例如: 2+3, 5*6, 10/2, 2^3");
        properties.put("expression", expression);
        
        params.put("properties", properties);
        params.put("required", java.util.Arrays.asList("expression"));
        
        return params;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> parameters) {
        String expression = (String) parameters.get("expression");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            double value = evaluateExpression(expression);
            result.put("expression", expression);
            result.put("result", value);
            result.put("status", "success");
        } catch (Exception e) {
            result.put("expression", expression);
            result.put("error", e.getMessage());
            result.put("status", "error");
        }
        
        return result;
    }

    /**
     * 计算表达式
     */
    private double evaluateExpression(String expression) {
        expression = expression.replaceAll("\\s+", "");
        
        // 处理幂运算
        if (expression.contains("^")) {
            String[] parts = expression.split("\\^");
            if (parts.length == 2) {
                double base = Double.parseDouble(parts[0]);
                double exponent = Double.parseDouble(parts[1]);
                return Math.pow(base, exponent);
            }
        }
        
        // 处理加减乘除
        return evaluateSimpleExpression(expression);
    }

    /**
     * 计算简单表达式
     */
    private double evaluateSimpleExpression(String expression) {
        // 先处理乘除
        while (expression.contains("*") || expression.contains("/")) {
            int mulIndex = expression.indexOf("*");
            int divIndex = expression.indexOf("/");
            
            int index = -1;
            char operator = ' ';
            
            if (mulIndex != -1 && divIndex != -1) {
                if (mulIndex < divIndex) {
                    index = mulIndex;
                    operator = '*';
                } else {
                    index = divIndex;
                    operator = '/';
                }
            } else if (mulIndex != -1) {
                index = mulIndex;
                operator = '*';
            } else {
                index = divIndex;
                operator = '/';
            }
            
            // 找到操作数
            int leftStart = findNumberStart(expression, index - 1);
            int rightEnd = findNumberEnd(expression, index + 1);
            
            double left = Double.parseDouble(expression.substring(leftStart, index));
            double right = Double.parseDouble(expression.substring(index + 1, rightEnd));
            
            double value = operator == '*' ? left * right : left / right;
            
            expression = expression.substring(0, leftStart) + value + expression.substring(rightEnd);
        }
        
        // 处理加减
        while (expression.contains("+") || (expression.lastIndexOf("-") > 0)) {
            int addIndex = expression.indexOf("+");
            int subIndex = expression.lastIndexOf("-");
            
            int index = -1;
            char operator = ' ';
            
            if (addIndex != -1 && subIndex != -1) {
                if (addIndex < subIndex) {
                    index = addIndex;
                    operator = '+';
                } else {
                    index = subIndex;
                    operator = '-';
                }
            } else if (addIndex != -1) {
                index = addIndex;
                operator = '+';
            } else if (subIndex > 0) {
                index = subIndex;
                operator = '-';
            } else {
                break;
            }
            
            int leftStart = findNumberStart(expression, index - 1);
            int rightEnd = findNumberEnd(expression, index + 1);
            
            double left = Double.parseDouble(expression.substring(leftStart, index));
            double right = Double.parseDouble(expression.substring(index + 1, rightEnd));
            
            double value = operator == '+' ? left + right : left - right;
            
            expression = expression.substring(0, leftStart) + value + expression.substring(rightEnd);
        }
        
        return Double.parseDouble(expression);
    }

    private int findNumberStart(String expression, int fromIndex) {
        while (fromIndex > 0 && (Character.isDigit(expression.charAt(fromIndex - 1)) || expression.charAt(fromIndex - 1) == '.')) {
            fromIndex--;
        }
        return fromIndex;
    }

    private int findNumberEnd(String expression, int fromIndex) {
        while (fromIndex < expression.length() && (Character.isDigit(expression.charAt(fromIndex)) || expression.charAt(fromIndex) == '.')) {
            fromIndex++;
        }
        return fromIndex;
    }
}
