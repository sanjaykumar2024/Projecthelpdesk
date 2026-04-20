package com.projecthelpdesk.projecthelpdesk.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projecthelpdesk.projecthelpdesk.dto.CommentRequest;
import com.projecthelpdesk.projecthelpdesk.dto.CommentResponse;
import com.projecthelpdesk.projecthelpdesk.service.CommentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping(consumes = { org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<CommentResponse> addComment(@PathVariable Long ticketId,
            @Valid @org.springframework.web.bind.annotation.ModelAttribute CommentRequest request,
            @org.springframework.web.bind.annotation.RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            Authentication auth) {
        return ResponseEntity.ok(commentService.addComment(ticketId, request, file, auth.getName()));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long ticketId) {
        return ResponseEntity.ok(commentService.getComments(ticketId));
    }
}
