package com.projecthelpdesk.projecthelpdesk.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.projecthelpdesk.projecthelpdesk.dto.DashboardStats;
import com.projecthelpdesk.projecthelpdesk.entity.Ticket;
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

        // Single query for both priority and department aggregation
        Map<String, Long> byPriority = new HashMap<>();
        Map<String, Long> byDept = new HashMap<>();
        ticketRepository.findAll().forEach(t -> {
            String p = t.getPriority().name();
            byPriority.put(p, byPriority.getOrDefault(p, 0L) + 1);
            String d = t.getDepartment().getName();
            byDept.put(d, byDept.getOrDefault(d, 0L) + 1);
        });
        stats.setTicketsByPriority(byPriority);
        stats.setTicketsByDepartment(byDept);

        return stats;
    }

    public DashboardStats getStatsByDepartment(Long departmentId) {
        List<Ticket> deptTickets = ticketRepository.findByDepartmentId(departmentId);

        DashboardStats stats = new DashboardStats();
        stats.setTotalTickets(deptTickets.size());
        stats.setOpenTickets(deptTickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count());
        stats.setInProgressTickets(deptTickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count());
        stats.setOnHoldTickets(deptTickets.stream().filter(t -> t.getStatus() == TicketStatus.ON_HOLD).count());
        stats.setResolvedTickets(deptTickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count());
        stats.setClosedTickets(deptTickets.stream().filter(t -> t.getStatus() == TicketStatus.CLOSED).count());

        Map<String, Long> byPriority = new HashMap<>();
        Map<String, Long> byDept = new HashMap<>();
        deptTickets.forEach(t -> {
            String p = t.getPriority().name();
            byPriority.put(p, byPriority.getOrDefault(p, 0L) + 1);
            String d = t.getDepartment().getName();
            byDept.put(d, byDept.getOrDefault(d, 0L) + 1);
        });
        stats.setTicketsByPriority(byPriority);
        stats.setTicketsByDepartment(byDept);

        return stats;
    }
}
