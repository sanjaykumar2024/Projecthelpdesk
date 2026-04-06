package com.projecthelpdesk.projecthelpdesk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves static HTML pages at clean URLs (without .html extension).
 * e.g. /dashboard → dashboard.html, /tickets → tickets.html
 */
@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        return "forward:/index.html";
    }

    @GetMapping("/register")
    public String register() {
        return "forward:/register.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/tickets")
    public String tickets() {
        return "forward:/tickets.html";
    }

    @GetMapping("/create-ticket")
    public String createTicket() {
        return "forward:/create-ticket.html";
    }

    @GetMapping("/ticket-detail")
    public String ticketDetail() {
        return "forward:/ticket-detail.html";
    }

    @GetMapping("/oauth2callback")
    public String oauth2Callback() {
        return "forward:/oauth2callback.html";
    }

    @GetMapping("/manage-agents")
    public String manageAgents() {
        return "forward:/manage-agents.html";
    }

    @GetMapping("/all-tickets")
    public String allTickets() {
        return "forward:/all-tickets.html";
    }

    @GetMapping("/department-tickets")
    public String departmentTickets() {
        return "forward:/department-tickets.html";
    }

    @GetMapping("/profile")
    public String profile() {
        return "forward:/profile.html";
    }

    @GetMapping("/notifications")
    public String notifications() {
        return "forward:/notifications.html";
    }

    @GetMapping("/knowledge-base")
    public String knowledgeBase() {
        return "forward:/knowledge-base.html";
    }

    @GetMapping("/analytics")
    public String analytics() {
        return "forward:/analytics.html";
    }

    @GetMapping("/admin-settings")
    public String adminSettings() {
        return "forward:/admin-settings.html";
    }
}
