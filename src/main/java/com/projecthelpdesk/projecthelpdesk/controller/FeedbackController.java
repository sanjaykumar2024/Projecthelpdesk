package com.projecthelpdesk.projecthelpdesk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projecthelpdesk.projecthelpdesk.dto.FeedbackRequest;
import com.projecthelpdesk.projecthelpdesk.dto.FeedbackResponse;
import com.projecthelpdesk.projecthelpdesk.service.FeedbackService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets/{ticketId}/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<FeedbackResponse> submitFeedback(@PathVariable Long ticketId,
            @Valid @RequestBody FeedbackRequest request,
            Authentication auth) {
        return ResponseEntity.ok(feedbackService.submitFeedback(ticketId, request, auth.getName()));
    }

    @GetMapping
    public ResponseEntity<FeedbackResponse> getFeedback(@PathVariable Long ticketId) {
        return ResponseEntity.ok(feedbackService.getFeedback(ticketId));
    }
}
