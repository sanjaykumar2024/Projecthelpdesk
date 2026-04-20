package com.projecthelpdesk.projecthelpdesk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projecthelpdesk.projecthelpdesk.service.AIService;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/generate-reply")
    public ResponseEntity<Map<String, String>> generateReply(@RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "");
        String description = request.getOrDefault("description", "");
        String priority = request.getOrDefault("priority", "LOW");

        String reply = aiService.generateSupportReply(title, description, priority);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}
