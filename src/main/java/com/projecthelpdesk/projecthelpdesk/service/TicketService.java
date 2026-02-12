package com.projecthelpdesk.projecthelpdesk.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projecthelpdesk.projecthelpdesk.dto.TicketRequest;
import com.projecthelpdesk.projecthelpdesk.dto.TicketResponse;
import com.projecthelpdesk.projecthelpdesk.entity.Department;
import com.projecthelpdesk.projecthelpdesk.entity.Priority;
import com.projecthelpdesk.projecthelpdesk.entity.Ticket;
import com.projecthelpdesk.projecthelpdesk.entity.TicketStatus;
import com.projecthelpdesk.projecthelpdesk.entity.User;
import com.projecthelpdesk.projecthelpdesk.exception.BadRequestException;
import com.projecthelpdesk.projecthelpdesk.exception.ResourceNotFoundException;
import com.projecthelpdesk.projecthelpdesk.repository.DepartmentRepository;
import com.projecthelpdesk.projecthelpdesk.repository.TicketFeedbackRepository;
import com.projecthelpdesk.projecthelpdesk.repository.TicketRepository;
import com.projecthelpdesk.projecthelpdesk.repository.UserRepository;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final TicketFeedbackRepository feedbackRepository;
    private final EmailService emailService;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository,
            DepartmentRepository departmentRepository, TicketFeedbackRepository feedbackRepository,
            EmailService emailService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.feedbackRepository = feedbackRepository;
        this.emailService = emailService;
    }

    @Transactional
    public TicketResponse createTicket(TicketRequest request, String email) {
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(Priority.valueOf(request.getPriority().toUpperCase()));
        ticket.setCreator(creator);
        ticket.setDepartment(dept);
        ticket.setStatus(TicketStatus.OPEN);

        ticket = ticketRepository.save(ticket);
        emailService.sendTicketCreatedEmail(ticket);
        return mapToResponse(ticket);
    }

    public List<TicketResponse> getTicketsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String roleName = user.getRole().getRoleName().name();
        List<Ticket> tickets;

        if ("AGENT".equals(roleName)) {
            tickets = ticketRepository.findByAssignedAgentId(user.getId());
        } else {
            tickets = ticketRepository.findByCreatorId(user.getId());
        }

        return tickets.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        return mapToResponse(ticket);
    }

    @Transactional
    public TicketResponse updateStatus(Long id, String status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        TicketStatus newStatus;
        try {
            newStatus = TicketStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }

        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
            emailService.sendTicketResolvedEmail(ticket);
        }
        return mapToResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse assignAgent(Long ticketId, Long agentId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        if (!agent.getRole().getRoleName().name().equals("AGENT") &&
                !agent.getRole().getRoleName().name().equals("ADMIN")) {
            throw new BadRequestException("User is not an agent");
        }

        ticket.setAssignedAgent(agent);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        ticket = ticketRepository.save(ticket);
        emailService.sendTicketAssignedEmail(ticket);
        return mapToResponse(ticket);
    }

    public List<TicketResponse> filterTickets(String status, String priority,
            Long departmentId, LocalDateTime startDate,
            LocalDateTime endDate) {
        TicketStatus ts = status != null ? TicketStatus.valueOf(status) : null;
        Priority pr = priority != null ? Priority.valueOf(priority) : null;
        return ticketRepository.findByFilters(ts, pr, departmentId, startDate, endDate)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private TicketResponse mapToResponse(Ticket t) {
        TicketResponse r = new TicketResponse();
        r.setId(t.getId());
        r.setTitle(t.getTitle());
        r.setDescription(t.getDescription());
        r.setStatus(t.getStatus().name());
        r.setPriority(t.getPriority().name());
        r.setDepartmentName(t.getDepartment().getName());
        r.setDepartmentId(t.getDepartment().getId());
        r.setCreatorId(t.getCreator().getId());
        r.setCreatorName(t.getCreator().getFullName());
        r.setCreatorEmail(t.getCreator().getEmail());
        r.setCreatedAt(t.getCreatedAt());
        r.setUpdatedAt(t.getUpdatedAt());
        r.setResolvedAt(t.getResolvedAt());
        if (t.getAssignedAgent() != null) {
            r.setAssignedAgentId(t.getAssignedAgent().getId());
            r.setAssignedAgentName(t.getAssignedAgent().getFullName());
        }
        r.setHasFeedback(feedbackRepository.existsByTicketId(t.getId()));
        return r;
    }
}
