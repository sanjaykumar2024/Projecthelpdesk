package com.projecthelpdesk.projecthelpdesk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projecthelpdesk.projecthelpdesk.dto.ChatMessage;
import com.projecthelpdesk.projecthelpdesk.service.ChatbotService;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/message")
    public ResponseEntity<ChatMessage> chat(@RequestBody ChatMessage message) {
        String response = chatbotService.getResponse(message.getMessage());
        ChatMessage reply = new ChatMessage();
        reply.setMessage(message.getMessage());
        reply.setResponse(response);
        return ResponseEntity.ok(reply);
    }
}
