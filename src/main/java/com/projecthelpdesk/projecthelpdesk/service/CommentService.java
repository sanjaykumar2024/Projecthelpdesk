package com.projecthelpdesk.projecthelpdesk.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.projecthelpdesk.projecthelpdesk.dto.CommentRequest;
import com.projecthelpdesk.projecthelpdesk.dto.CommentResponse;
import com.projecthelpdesk.projecthelpdesk.entity.NotificationType;
import com.projecthelpdesk.projecthelpdesk.entity.Ticket;
import com.projecthelpdesk.projecthelpdesk.entity.TicketActivity;
import com.projecthelpdesk.projecthelpdesk.entity.TicketComment;
import com.projecthelpdesk.projecthelpdesk.entity.User;
import com.projecthelpdesk.projecthelpdesk.exception.ResourceNotFoundException;
import com.projecthelpdesk.projecthelpdesk.repository.TicketCommentRepository;
import com.projecthelpdesk.projecthelpdesk.repository.TicketRepository;
import com.projecthelpdesk.projecthelpdesk.repository.UserRepository;
import com.projecthelpdesk.projecthelpdesk.repository.TicketActivityRepository;

@Service
public class CommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final TicketActivityRepository activityRepository;
    private final FileStorageService fileStorageService;

    public CommentService(TicketCommentRepository commentRepository, TicketRepository ticketRepository,
            UserRepository userRepository, NotificationService notificationService, TicketActivityRepository activityRepository,
            FileStorageService fileStorageService) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.activityRepository = activityRepository;
        this.fileStorageService = fileStorageService;
    }

    public CommentResponse addComment(Long ticketId, CommentRequest request, org.springframework.web.multipart.MultipartFile file, String email) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TicketComment comment = new TicketComment();
        comment.setMessage(request.getMessage());
        comment.setTicket(ticket);
        comment.setAuthor(author);
        
        if (file != null && !file.isEmpty()) {
            comment.setAttachmentUrl(fileStorageService.storeFile(file));
        }
        
        comment = commentRepository.save(comment);

        activityRepository.save(new TicketActivity(ticket, author, "Added a comment", "COMMENT"));

        // Notify ticket creator (if commenter is not the creator)
        if (!ticket.getCreator().getId().equals(author.getId())) {
            notificationService.createNotification(ticket.getCreator(), "New Comment",
                    author.getFullName() + " commented on ticket #" + ticketId,
                    NotificationType.COMMENT_ADDED, ticketId);
        }
        // Notify assigned agent (if commenter is not the agent)
        if (ticket.getAssignedAgent() != null && !ticket.getAssignedAgent().getId().equals(author.getId())) {
            notificationService.createNotification(ticket.getAssignedAgent(), "New Comment",
                    author.getFullName() + " commented on ticket #" + ticketId,
                    NotificationType.COMMENT_ADDED, ticketId);
        }

        return mapToResponse(comment);
    }

    public List<CommentResponse> getComments(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Ticket not found");
        }
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private CommentResponse mapToResponse(TicketComment c) {
        CommentResponse r = new CommentResponse();
        r.setId(c.getId());
        r.setMessage(c.getMessage());
        r.setAuthorName(c.getAuthor().getFullName());
        r.setAuthorEmail(c.getAuthor().getEmail());
        r.setCreatedAt(c.getCreatedAt());
        r.setAttachmentUrl(c.getAttachmentUrl());
        return r;
    }
}
