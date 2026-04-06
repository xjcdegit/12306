package com.springaiquickstart.controller;

import com.springai.skills.agent.WorkflowCoordinator;
import com.springai.skills.core.Skill;
import com.springai.skills.core.SkillResult;
import com.springai.skills.core.Tool;
import com.springai.skills.registry.SkillRegistry;
import com.springai.skills.registry.ToolRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/skills")
public class SkillsTestController {

    private final SkillRegistry skillRegistry;
    private final ToolRegistry toolRegistry;
    private final WorkflowCoordinator workflowCoordinator;

    @Autowired
    public SkillsTestController(SkillRegistry skillRegistry, ToolRegistry toolRegistry, WorkflowCoordinator workflowCoordinator) {
        this.skillRegistry = skillRegistry;
        this.toolRegistry = toolRegistry;
        this.workflowCoordinator = workflowCoordinator;
    }

    @GetMapping
    public List<Map<String, Object>> listSkills() {
        return skillRegistry.getAll().stream()
                .map(skill -> Map.<String, Object>of(
                        "name", skill.getName(),
                        "description", skill.getDescription(),
                        "requiredTools", skill.getRequiredTools()
                ))
                .toList();
    }

    @GetMapping("/tools")
    public List<Map<String, Object>> listTools() {
        return toolRegistry.getAll().stream()
                .map(tool -> Map.<String, Object>of(
                        "name", tool.getName(),
                        "description", tool.getDescription(),
                        "parameters", tool.getParameters()
                ))
                .toList();
    }

    @PostMapping("/execute")
    public SkillResult execute(@RequestBody Map<String, String> request) {
        String userInput = request.getOrDefault("input", "");
        return workflowCoordinator.run(userInput);
    }
}
