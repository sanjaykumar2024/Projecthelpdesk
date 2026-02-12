package com.projecthelpdesk.projecthelpdesk.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.projecthelpdesk.projecthelpdesk.dto.DashboardStats;
import com.projecthelpdesk.projecthelpdesk.entity.TicketStatus;
import com.projecthelpdesk.projecthelpdesk.repository.TicketRepository;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;

    public DashboardService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalTickets(ticketRepository.count());
        stats.setOpenTickets(ticketRepository.countByStatus(TicketStatus.OPEN));
        stats.setInProgressTickets(ticketRepository.countByStatus(TicketStatus.IN_PROGRESS));
        stats.setOnHoldTickets(ticketRepository.countByStatus(TicketStatus.ON_HOLD));
        stats.setResolvedTickets(ticketRepository.countByStatus(TicketStatus.RESOLVED));
        stats.setClosedTickets(ticketRepository.countByStatus(TicketStatus.CLOSED));

        // Tickets by priority
        Map<String, Long> byPriority = new HashMap<>();
        ticketRepository.findAll().forEach(t -> {
            String p = t.getPriority().name();
            byPriority.put(p, byPriority.getOrDefault(p, 0L) + 1);
        });
        stats.setTicketsByPriority(byPriority);

        // Tickets by department
        Map<String, Long> byDept = new HashMap<>();
        ticketRepository.findAll().forEach(t -> {
            String d = t.getDepartment().getName();
            byDept.put(d, byDept.getOrDefault(d, 0L) + 1);
        });
        stats.setTicketsByDepartment(byDept);

        return stats;
    }
}
