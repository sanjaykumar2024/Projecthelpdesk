package com.projecthelpdesk.projecthelpdesk.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.projecthelpdesk.projecthelpdesk.entity.Ticket;
import com.projecthelpdesk.projecthelpdesk.entity.TicketFeedback;
import com.projecthelpdesk.projecthelpdesk.entity.TicketStatus;
import com.projecthelpdesk.projecthelpdesk.repository.TicketFeedbackRepository;
import com.projecthelpdesk.projecthelpdesk.repository.TicketRepository;
import com.projecthelpdesk.projecthelpdesk.repository.UserRepository;

@Service
public class AnalyticsService {

    private final TicketRepository ticketRepository;
    private final TicketFeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public AnalyticsService(TicketRepository ticketRepository, TicketFeedbackRepository feedbackRepository,
            UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    public Map<String, Object> getResolutionTime() {
        List<Ticket> resolved = ticketRepository.findAll().stream()
                .filter(t -> t.getResolvedAt() != null && t.getCreatedAt() != null)
                .collect(Collectors.toList());

        if (resolved.isEmpty()) {
            return Map.of("avgHours", 0, "avgDays", 0, "totalResolved", 0);
        }

        double totalHours = resolved.stream()
                .mapToDouble(t -> Duration.between(t.getCreatedAt(), t.getResolvedAt()).toHours())
                .average().orElse(0);

        Map<String, Object> result = new HashMap<>();
        result.put("avgHours", Math.round(totalHours * 10.0) / 10.0);
        result.put("avgDays", Math.round(totalHours / 24 * 10.0) / 10.0);
        result.put("totalResolved", resolved.size());

        // Per-department resolution time
        Map<String, Double> byDept = resolved.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getDepartment().getName(),
                        Collectors.averagingDouble(t ->
                                Duration.between(t.getCreatedAt(), t.getResolvedAt()).toHours())));
        result.put("byDepartment", byDept);

        return result;
    }

    public Map<String, Object> getTrends(String period) {
        List<Ticket> all = ticketRepository.findAll();
        Map<String, Long> trends = new LinkedHashMap<>();

        LocalDateTime now = LocalDateTime.now();
        int days = "monthly".equals(period) ? 30 : "weekly".equals(period) ? 7 : 30;

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime day = now.minusDays(i);
            String key = day.toLocalDate().toString();
            long count = all.stream()
                    .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().equals(day.toLocalDate()))
                    .count();
            trends.put(key, count);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", trends.keySet());
        result.put("data", trends.values());
        result.put("period", period);
        return result;
    }

    public List<Map<String, Object>> getAgentPerformance() {
        List<Ticket> allTickets = ticketRepository.findAll();

        return userRepository.findAll().stream()
                .filter(u -> "AGENT".equals(u.getRole().getRoleName().name()) || "ADMIN".equals(u.getRole().getRoleName().name()))
                .map(agent -> {
                    Map<String, Object> perf = new HashMap<>();
                    perf.put("agentId", agent.getId());
                    perf.put("agentName", agent.getFullName());
                    perf.put("department", agent.getDepartment() != null ? agent.getDepartment().getName() : "-");

                    List<Ticket> assigned = allTickets.stream()
                            .filter(t -> t.getAssignedAgent() != null && t.getAssignedAgent().getId().equals(agent.getId()))
                            .collect(Collectors.toList());

                    long resolved = assigned.stream()
                            .filter(t -> t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED)
                            .count();

                    perf.put("totalAssigned", assigned.size());
                    perf.put("totalResolved", resolved);
                    perf.put("openTickets", assigned.size() - resolved);

                    // Average resolution time
                    double avgHours = assigned.stream()
                            .filter(t -> t.getResolvedAt() != null)
                            .mapToDouble(t -> Duration.between(t.getCreatedAt(), t.getResolvedAt()).toHours())
                            .average().orElse(0);
                    perf.put("avgResolutionHours", Math.round(avgHours * 10.0) / 10.0);

                    return perf;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getSatisfaction() {
        List<TicketFeedback> feedbacks = feedbackRepository.findAll();

        if (feedbacks.isEmpty()) {
            return Map.of("averageRating", 0, "totalFeedbacks", 0, "distribution", Map.of());
        }

        double avg = feedbacks.stream().mapToInt(TicketFeedback::getRating).average().orElse(0);

        Map<Integer, Long> distribution = feedbacks.stream()
                .collect(Collectors.groupingBy(TicketFeedback::getRating, Collectors.counting()));

        Map<String, Object> result = new HashMap<>();
        result.put("averageRating", Math.round(avg * 10.0) / 10.0);
        result.put("totalFeedbacks", feedbacks.size());
        result.put("distribution", distribution);
        return result;
    }

    public Map<String, Object> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("resolutionTime", getResolutionTime());
        overview.put("satisfaction", getSatisfaction());
        overview.put("agentPerformance", getAgentPerformance());
        overview.put("trends", getTrends("monthly"));
        return overview;
    }
}
