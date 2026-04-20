package com.projecthelpdesk.projecthelpdesk.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projecthelpdesk.projecthelpdesk.entity.NotificationType;
import com.projecthelpdesk.projecthelpdesk.entity.Ticket;
import com.projecthelpdesk.projecthelpdesk.entity.TicketActivity;
import com.projecthelpdesk.projecthelpdesk.repository.TicketActivityRepository;
import com.projecthelpdesk.projecthelpdesk.repository.TicketRepository;

@Service
public class SLAService {

    private static final Logger log = LoggerFactory.getLogger(SLAService.class);

    private final TicketRepository ticketRepository;
    private final NotificationService notificationService;
    private final TicketActivityRepository activityRepository;

    public SLAService(TicketRepository ticketRepository, NotificationService notificationService, TicketActivityRepository activityRepository) {
        this.ticketRepository = ticketRepository;
        this.notificationService = notificationService;
        this.activityRepository = activityRepository;
    }

    /**
     * Set SLA due date for new ticket depending on priority
     */
    public LocalDateTime calculateDueDate(com.projecthelpdesk.projecthelpdesk.entity.Priority priority) {
        LocalDateTime now = LocalDateTime.now();
        switch (priority) {
            case URGENT:
                return now.plusHours(4); // 4 hours
            case HIGH:
                return now.plusHours(24); // 24 hours
            case MEDIUM:
                return now.plusDays(3); // 3 days
            case LOW:
            default:
                return now.plusDays(7); // 7 days
        }
    }

    /**
     * Run every hour to check for SLA breaches
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void checkForSlaBreaches() {
        log.info("Checking for overdue tickets...");
        LocalDateTime now = LocalDateTime.now();
        
        List<Ticket> overdueTickets = ticketRepository.findOverdueTickets(now);
        
        for (Ticket ticket : overdueTickets) {
            ticket.setEscalated(true);
            
            // Reassign to ADMIN or highest level Agent logic could go here.
            
            ticketRepository.save(ticket);
            activityRepository.save(new TicketActivity(ticket, null, "Ticket auto-escalated due to SLA breach", "ESCALATION"));
            
            // Notify assigned agent
            if (ticket.getAssignedAgent() != null) {
                notificationService.createNotification(
                    ticket.getAssignedAgent(),
                    "⚠️ SLA Breach: " + ticket.getTitle(),
                    "Ticket #" + ticket.getId() + " has exceeded its SLA and has been escalated.",
                    NotificationType.SLA_BREACH,
                    ticket.getId()
                );
            }
            
            log.warn("Ticket #{} was escalated due to SLA breach", ticket.getId());
        }
        
        if (overdueTickets.isEmpty()) {
            log.info("No SLA breaches found.");
        } else {
            log.info("Escalated {} tickets due to SLA breach.", overdueTickets.size());
        }
    }
}
