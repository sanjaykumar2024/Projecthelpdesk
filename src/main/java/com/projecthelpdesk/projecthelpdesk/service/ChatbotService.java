package com.projecthelpdesk.projecthelpdesk.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    private static final Map<String, String> FAQ_RESPONSES = new LinkedHashMap<>();

    static {
        FAQ_RESPONSES.put("hello|hi|hey|greetings",
                "👋 Hello! Welcome to Helpdesk Support! How can I help you today?");
        FAQ_RESPONSES.put("create ticket|new ticket|submit ticket|raise ticket",
                "📝 To create a new ticket:\n1. Click 'Create Ticket' in the sidebar\n2. Fill in the title, description, and priority\n3. Select your department\n4. Click 'Submit'\n\nYour ticket will be assigned to an agent shortly!");
        FAQ_RESPONSES.put("track|status|check ticket|my ticket",
                "🔍 To check your ticket status:\n1. Go to 'My Tickets' from the sidebar\n2. You'll see all your tickets with their current status\n3. Click on any ticket to see full details and comments");
        FAQ_RESPONSES.put("priority|urgent|high priority",
                "🎯 Ticket Priority Levels:\n• LOW - General inquiries\n• MEDIUM - Standard issues\n• HIGH - Important problems\n• URGENT - Critical issues needing immediate attention");
        FAQ_RESPONSES.put("resolved|close|feedback|rate|rating",
                "⭐ When your ticket is resolved:\n1. You'll receive an email notification\n2. Go to the ticket details\n3. Submit your rating (1-5 stars)\n4. Leave optional feedback\n\nYour feedback helps us improve!");
        FAQ_RESPONSES.put("password|reset|forgot",
                "🔑 For password-related issues, please contact your system administrator or use the 'Forgot Password' link on the login page.");
        FAQ_RESPONSES.put("department|departments",
                "🏢 Our helpdesk supports multiple departments. When creating a ticket, choose the most relevant department to ensure your request reaches the right team.");
        FAQ_RESPONSES.put("agent|assigned|who",
                "👤 After you submit a ticket, an admin will assign it to an available agent from the relevant department. You'll be notified by email when this happens.");
        FAQ_RESPONSES.put("contact|email|phone|support",
                "📧 You can reach us through:\n• This helpdesk system (recommended)\n• Email: support@helpdesk.com\n• Create a ticket for fastest response!");
        FAQ_RESPONSES.put("thanks|thank you|bye|goodbye",
                "😊 You're welcome! If you need anything else, feel free to ask. Have a great day!");
    }

    public String getResponse(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "Please type a message so I can help you! 😊";
        }

        String lowerMsg = userMessage.toLowerCase().trim();

        for (Map.Entry<String, String> entry : FAQ_RESPONSES.entrySet()) {
            String[] keywords = entry.getKey().split("\\|");
            for (String keyword : keywords) {
                if (lowerMsg.contains(keyword.trim())) {
                    return entry.getValue();
                }
            }
        }

        return "🤔 I'm not sure about that. Here are some things I can help with:\n\n"
                + "• How to create a ticket\n"
                + "• How to track ticket status\n"
                + "• Priority levels explained\n"
                + "• Rating & feedback system\n"
                + "• Department information\n"
                + "• Contact support\n\n"
                + "Try asking about any of these topics!";
    }
}
