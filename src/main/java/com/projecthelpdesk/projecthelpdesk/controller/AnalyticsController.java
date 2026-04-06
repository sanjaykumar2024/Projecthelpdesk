package com.projecthelpdesk.projecthelpdesk.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projecthelpdesk.projecthelpdesk.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getOverview() {
        return ResponseEntity.ok(analyticsService.getOverview());
    }

    @GetMapping("/resolution-time")
    public ResponseEntity<Map<String, Object>> getResolutionTime() {
        return ResponseEntity.ok(analyticsService.getResolutionTime());
    }

    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getTrends(
            @RequestParam(defaultValue = "monthly") String period) {
        return ResponseEntity.ok(analyticsService.getTrends(period));
    }

    @GetMapping("/agent-performance")
    public ResponseEntity<List<Map<String, Object>>> getAgentPerformance() {
        return ResponseEntity.ok(analyticsService.getAgentPerformance());
    }

    @GetMapping("/satisfaction")
    public ResponseEntity<Map<String, Object>> getSatisfaction() {
        return ResponseEntity.ok(analyticsService.getSatisfaction());
    }
}
