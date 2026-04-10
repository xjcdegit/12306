package com.springai.rag.skill;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SkillRouter {

    private static final Logger log = LoggerFactory.getLogger(SkillRouter.class);

    @Value("${skill.path:classpath:skillls}")
    private String skillPath;

    private final Map<String, SkillInfo> skills = new HashMap<>();
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @PostConstruct
    public void loadSkills() {
        log.info("========================================");
        log.info("[SKILL] 开始加载Skills...");
        log.info("[SKILL] Skill路径: {}", skillPath);
        
        try {
            String pattern = skillPath.replace("classpath:", "classpath*:") + "/*.md";
            log.info("[SKILL] 搜索模式: {}", pattern);
            
            Resource[] resources = resolver.getResources(pattern);
            log.info("[SKILL] 发现 {} 个资源文件", resources.length);
            
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                log.info("[SKILL] 处理文件: {}", filename);
                
                if (filename != null && filename.endsWith(".md")) {
                    String skillName = filename.replace(".md", "");
                    String content = readContent(resource);
                    SkillInfo skillInfo = parseSkill(skillName, content);
                    skills.put(skillName, skillInfo);
                    
                    log.info("[SKILL] ✓ 加载Skill: {}", skillName);
                    log.info("[SKILL]   描述: {}", skillInfo.getDescription());
                    log.info("[SKILL]   关键词: {}", skillInfo.getKeywords());
                    log.info("[SKILL]   MCP类型: {}", skillInfo.getMcpServerType());
                }
            }
            
            log.info("[SKILL] 总计加载 {} 个Skills", skills.size());
        } catch (IOException e) {
            log.error("[SKILL] 加载Skills失败", e);
        }
        log.info("========================================");
    }

    private String readContent(Resource resource) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private SkillInfo parseSkill(String name, String content) {
        SkillInfo info = new SkillInfo();
        info.setName(name);
        info.setContent(content);
        
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.startsWith("# ")) {
                info.setDescription(line.substring(2).trim());
                break;
            }
        }
        
        Set<String> keywords = new HashSet<>();
        keywords.add(name.toLowerCase());
        
        if (content.contains("旅游") || content.contains("旅行") || content.contains("景点")) {
            keywords.add("旅游");
            keywords.add("旅行");
            keywords.add("景点");
            keywords.add("攻略");
            keywords.add("路线");
            keywords.add("导航");
            keywords.add("地图");
            keywords.add("位置");
            keywords.add("坐标");
            keywords.add("距离");
            keywords.add("交通");
            keywords.add("查询");
            keywords.add("搜索");
        }
        
        if (content.contains("高德")) {
            keywords.add("高德");
            keywords.add("amap");
        }
        
        info.setKeywords(keywords);
        
        if (content.contains("高德") || content.contains("amap")) {
            info.setMcpServerType("amap-mcp");
        }
        
        return info;
    }

    public SkillInfo routeSkill(String userPrompt) {
        log.info("========================================");
        log.info("[SKILL-ROUTE] 开始路由Skill");
        log.info("[SKILL-ROUTE] 用户提示词: {}", userPrompt);
        
        String lowerPrompt = userPrompt.toLowerCase();
        Map<String, Integer> scores = new HashMap<>();
        
        log.info("[SKILL-ROUTE] 计算匹配分数...");
        for (Map.Entry<String, SkillInfo> entry : skills.entrySet()) {
            String skillName = entry.getKey();
            SkillInfo skill = entry.getValue();
            int score = 0;
            
            for (String keyword : skill.getKeywords()) {
                if (lowerPrompt.contains(keyword.toLowerCase())) {
                    score++;
                    if (lowerPrompt.startsWith(keyword.toLowerCase())) {
                        score += 2;
                    }
                }
            }
            
            if (score > 0) {
                scores.put(skillName, score);
                log.info("[SKILL-ROUTE]   Skill '{}' 得分: {}", skillName, score);
            }
        }
        
        SkillInfo result = scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> {
                log.info("[SKILL-ROUTE] ✓ 最佳匹配: {} (得分: {})", entry.getKey(), entry.getValue());
                return skills.get(entry.getKey());
            })
            .orElse(null);
        
        if (result == null) {
            log.info("[SKILL-ROUTE] ✗ 无匹配的Skill");
        }
        
        log.info("========================================");
        return result;
    }

    public List<SkillInfo> getAllSkills() {
        return new ArrayList<>(skills.values());
    }

    public SkillInfo getSkill(String name) {
        return skills.get(name);
    }

    public static class SkillInfo {
        private String name;
        private String description;
        private String content;
        private Set<String> keywords = new HashSet<>();
        private String mcpServerType;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public Set<String> getKeywords() { return keywords; }
        public void setKeywords(Set<String> keywords) { this.keywords = keywords; }
        
        public String getMcpServerType() { return mcpServerType; }
        public void setMcpServerType(String mcpServerType) { this.mcpServerType = mcpServerType; }
    }
}
