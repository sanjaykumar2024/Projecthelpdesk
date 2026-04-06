package com.projecthelpdesk.projecthelpdesk.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.projecthelpdesk.projecthelpdesk.entity.KnowledgeArticle;
import com.projecthelpdesk.projecthelpdesk.service.KnowledgeService;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllArticles() {
        return ResponseEntity.ok(knowledgeService.getAllPublished().stream()
                .map(this::mapToResponse).collect(Collectors.toList()));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(knowledgeService.getAllCategories());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> search(@RequestParam String q) {
        return ResponseEntity.ok(knowledgeService.search(q).stream()
                .map(this::mapToResponse).collect(Collectors.toList()));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Map<String, Object>>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(knowledgeService.getByCategory(category).stream()
                .map(this::mapToResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getArticle(@PathVariable Long id) {
        return ResponseEntity.ok(mapToResponse(knowledgeService.getArticle(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createArticle(@RequestBody KnowledgeArticle article,
            Authentication auth) {
        return ResponseEntity.ok(mapToResponse(knowledgeService.createArticle(article, auth.getName())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateArticle(@PathVariable Long id,
            @RequestBody KnowledgeArticle article) {
        return ResponseEntity.ok(mapToResponse(knowledgeService.updateArticle(id, article)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id) {
        knowledgeService.deleteArticle(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<Map<String, Object>> vote(@PathVariable Long id, @RequestParam boolean helpful) {
        return ResponseEntity.ok(mapToResponse(knowledgeService.vote(id, helpful)));
    }

    private Map<String, Object> mapToResponse(KnowledgeArticle a) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", a.getId());
        map.put("title", a.getTitle());
        map.put("content", a.getContent());
        map.put("category", a.getCategory());
        map.put("tags", a.getTags());
        map.put("authorName", a.getAuthor() != null ? a.getAuthor().getFullName() : "System");
        map.put("viewCount", a.getViewCount());
        map.put("helpful", a.getHelpful());
        map.put("notHelpful", a.getNotHelpful());
        map.put("published", a.isPublished());
        map.put("createdAt", a.getCreatedAt());
        map.put("updatedAt", a.getUpdatedAt());
        return map;
    }
}
