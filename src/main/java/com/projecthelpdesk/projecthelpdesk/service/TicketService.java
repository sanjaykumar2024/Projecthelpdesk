package com.projecthelpdesk.projecthelpdesk.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.projecthelpdesk.projecthelpdesk.entity.ERole;
import com.projecthelpdesk.projecthelpdesk.entity.NotificationType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projecthelpdesk.projecthelpdesk.dto.TicketRequest;
import com.projecthelpdesk.projecthelpdesk.dto.TicketResponse;
import com.projecthelpdesk.projecthelpdesk.dto.UserDTO;
import com.projecthelpdesk.projecthelpdesk.entity.Department;
import com.projecthelpdesk.projecthelpdesk.entity.Priority;
import com.projecthelpdesk.projecthelpdesk.entity.Ticket;
import com.projecthelpdesk.projecthelpdesk.entity.TicketActivity;
import com.projecthelpdesk.projecthelpdesk.entity.TicketStatus;
import com.projecthelpdesk.projecthelpdesk.entity.User;
import com.projecthelpdesk.projecthelpdesk.dto.ActivityResponse;
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
    private final NotificationService notificationService;
    private final SLAService slaService;
    private final com.projecthelpdesk.projecthelpdesk.repository.TicketActivityRepository activityRepository;
    private final FileStorageService fileStorageService;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository,
            DepartmentRepository departmentRepository, TicketFeedbackRepository feedbackRepository,
            EmailService emailService, NotificationService notificationService, SLAService slaService,
            com.projecthelpdesk.projecthelpdesk.repository.TicketActivityRepository activityRepository,
            FileStorageService fileStorageService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.feedbackRepository = feedbackRepository;
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.slaService = slaService;
        this.activityRepository = activityRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public TicketResponse createTicket(TicketRequest request, org.springframework.web.multipart.MultipartFile file, String email) {
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

        // SLA Calculation
        ticket.setDueDate(slaService.calculateDueDate(ticket.getPriority()));

        if (file != null && !file.isEmpty()) {
            String fileUrl = fileStorageService.storeFile(file);
            ticket.setAttachmentUrl(fileUrl);
        }

        ticket = ticketRepository.save(ticket);
        
        activityRepository.save(new TicketActivity(ticket, creator, "Ticket created", "TICKET_CREATED"));
        
        emailService.sendTicketCreatedEmail(ticket);
        notificationService.createNotification(creator, "Ticket Created",
                "Your ticket #" + ticket.getId() + " '" + ticket.getTitle() + "' has been created.",
                NotificationType.TICKET_CREATED, ticket.getId());
        return mapToResponse(ticket);
    }

    public List<TicketResponse> getTicketsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String roleName = user.getRole().getRoleName().name();
        List<Ticket> tickets;

        if ("ADMIN".equals(roleName)) {
            tickets = ticketRepository.findAll();
        } else if ("HOD".equals(roleName)) {
            if (user.getDepartment() == null) {
                throw new BadRequestException("HOD is not assigned to any department");
            }
            tickets = ticketRepository.findByDepartmentId(user.getDepartment().getId());
        } else if ("AGENT".equals(roleName)) {
            tickets = ticketRepository.findByAssignedAgentId(user.getId());
        } else {
            tickets = ticketRepository.findByCreatorId(user.getId());
        }

        return tickets.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<TicketResponse> getDepartmentTickets(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String role = user.getRole().getRoleName().name();
        if (!"AGENT".equals(role) && !"ADMIN".equals(role) && !"HOD".equals(role)) {
            throw new BadRequestException("Only agents, HODs and admins can view department tickets");
        }

        if ("ADMIN".equals(role)) {
            return ticketRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
        }

        if (user.getDepartment() == null) {
            throw new BadRequestException("User is not assigned to any department");
        }

        List<Ticket> tickets = ticketRepository.findByDepartmentId(user.getDepartment().getId());
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
    public TicketResponse updateStatus(Long id, String status, String email) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        User user = userRepository.findByEmail(email).orElse(null);

        TicketStatus newStatus;
        try {
            newStatus = TicketStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }

        String oldStatus = ticket.getStatus().name();
        ticket.setStatus(newStatus);
        
        activityRepository.save(new TicketActivity(ticket, user, "Status changed from " + oldStatus + " to " + newStatus.name(), "STATUS_CHANGE"));
        
        if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(LocalDateTime.now());
            emailService.sendTicketResolvedEmail(ticket);
            notificationService.createNotification(ticket.getCreator(), "Ticket Resolved",
                    "Your ticket #" + ticket.getId() + " has been resolved.",
                    NotificationType.TICKET_RESOLVED, ticket.getId());
        }
        notificationService.createNotification(ticket.getCreator(), "Status Updated",
                "Ticket #" + ticket.getId() + " status changed to " + newStatus.name(),
                NotificationType.STATUS_CHANGED, ticket.getId());
        return mapToResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse assignAgent(Long ticketId, Long agentId) {
        return assignAgent(ticketId, agentId, null);
    }

    @Transactional
    public TicketResponse assignAgent(Long ticketId, Long agentId, String callerEmail) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));
        User caller = callerEmail != null ? userRepository.findByEmail(callerEmail).orElse(null) : null;

        if (!agent.getRole().getRoleName().name().equals("AGENT") &&
                !agent.getRole().getRoleName().name().equals("ADMIN")) {
            throw new BadRequestException("User is not an agent");
        }

        // HOD validation: enforce department isolation
        if (caller != null) {
            if ("HOD".equals(caller.getRole().getRoleName().name())) {
                if (caller.getDepartment() == null) {
                    throw new BadRequestException("HOD is not assigned to any department");
                }
                Long hodDeptId = caller.getDepartment().getId();
                // Ticket must belong to HOD's department
                if (!ticket.getDepartment().getId().equals(hodDeptId)) {
                    throw new BadRequestException("HOD can only assign tickets within their own department");
                }
                // Agent must belong to HOD's department
                if (agent.getDepartment() == null || !agent.getDepartment().getId().equals(hodDeptId)) {
                    throw new BadRequestException("Cannot assign agent from a different department");
                }
            }
        }

        ticket.setAssignedAgent(agent);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        ticket = ticketRepository.save(ticket);
        activityRepository.save(new TicketActivity(ticket, caller, "Assigned to agent: " + agent.getFullName(), "ASSIGNMENT"));
        emailService.sendTicketAssignedEmail(ticket);
        notificationService.createNotification(agent, "Ticket Assigned",
                "Ticket #" + ticket.getId() + " '" + ticket.getTitle() + "' has been assigned to you.",
                NotificationType.TICKET_ASSIGNED, ticket.getId());
        return mapToResponse(ticket);
    }

    public List<UserDTO> getAgentsByDepartment(String callerEmail) {
        User caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (caller.getDepartment() == null) {
            throw new BadRequestException("User is not assigned to any department");
        }
        List<User> agents = userRepository.findByDepartmentIdAndRole_RoleName(
                caller.getDepartment().getId(), ERole.AGENT);
        return agents.stream().map(a -> {
            UserDTO dto = new UserDTO(a.getId(), a.getFullName(), a.getEmail(),
                    a.getRole().getRoleName().name(),
                    a.getDepartment() != null ? a.getDepartment().getName() : "-",
                    a.isEnabled(), a.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public TicketResponse assignToSelf(Long ticketId, String email) {
        User agent = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return assignAgent(ticketId, agent.getId());
    }

    public List<TicketResponse> filterTickets(String status, String priority,
            Long departmentId, LocalDateTime startDate,
            LocalDateTime endDate) {
        TicketStatus ts = status != null ? TicketStatus.valueOf(status) : null;
        Priority pr = priority != null ? Priority.valueOf(priority) : null;
        return ticketRepository.findByFilters(ts, pr, departmentId, startDate, endDate)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<TicketResponse> searchTickets(String keyword) {
        return ticketRepository.searchByKeyword(keyword)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<ActivityResponse> getTicketActivities(Long ticketId) {
        return activityRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream()
                .map(ActivityResponse::new)
                .collect(Collectors.toList());
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
        r.setDueDate(t.getDueDate());
        r.setEscalated(t.isEscalated());
        r.setAttachmentUrl(t.getAttachmentUrl());
        if (t.getAssignedAgent() != null) {
            r.setAssignedAgentId(t.getAssignedAgent().getId());
            r.setAssignedAgentName(t.getAssignedAgent().getFullName());
        }
        r.setHasFeedback(feedbackRepository.existsByTicketId(t.getId()));
        return r;
    }
}
