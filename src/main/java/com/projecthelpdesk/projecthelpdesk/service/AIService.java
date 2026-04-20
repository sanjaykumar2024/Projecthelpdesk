package com.projecthelpdesk.projecthelpdesk.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AIService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    public String generateSupportReply(String ticketTitle, String ticketDescription, String priority) {
        String prompt = "You are a polite, professional IT Helpdesk Support Agent. " +
                "Draft a short, empathetic response to the user who opened this ticket.\n" +
                "Ticket Title: " + ticketTitle + "\n" +
                "Priority: " + priority + "\n" +
                "Details: " + ticketDescription + "\n\n" +
                "Do not include placeholders like [Your Name]. Just write the message.";

        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return generateFallbackReply(priority);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Gemini API v1beta GenerateContent Request Format
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(part));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String url = geminiApiUrl + "?key=" + geminiApiKey;

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> contentMap = (Map<String, Object>) firstCandidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                    if (!parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini API Call failed: " + e.getMessage());
        }

        return generateFallbackReply(priority);
    }

    private String generateFallbackReply(String priority) {
        String[] templates = {
            "Thank you for reaching out. We have logged your request and our team is actively reviewing the details you furnished. We will get back to you with an update shortly.",
            "We appreciate you bringing this to our attention. A support engineer will investigate this further and will update this ticket once we have more information.",
            "Your ticket has been securely received by our system. We are reviewing the logs and information provided. We'll be in touch."
        };
        
        String reply = templates[random.nextInt(templates.length)];
        if ("URGENT".equalsIgnoreCase(priority) || "HIGH".equalsIgnoreCase(priority)) {
            return "⚡ HIGH PRIORITY: " + reply;
        }
        return reply;
    }
}
