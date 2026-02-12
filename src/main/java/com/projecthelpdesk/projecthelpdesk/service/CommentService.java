package com.projecthelpdesk.projecthelpdesk.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.projecthelpdesk.projecthelpdesk.dto.CommentRequest;
import com.projecthelpdesk.projecthelpdesk.dto.CommentResponse;
import com.projecthelpdesk.projecthelpdesk.entity.Ticket;
import com.projecthelpdesk.projecthelpdesk.entity.TicketComment;
import com.projecthelpdesk.projecthelpdesk.entity.User;
import com.projecthelpdesk.projecthelpdesk.exception.ResourceNotFoundException;
import com.projecthelpdesk.projecthelpdesk.repository.TicketCommentRepository;
import com.projecthelpdesk.projecthelpdesk.repository.TicketRepository;
import com.projecthelpdesk.projecthelpdesk.repository.UserRepository;

@Service
public class CommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public CommentService(TicketCommentRepository commentRepository, TicketRepository ticketRepository,
            UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    public CommentResponse addComment(Long ticketId, CommentRequest request, String email) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        TicketComment comment = new TicketComment();
        comment.setMessage(request.getMessage());
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment = commentRepository.save(comment);
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
        return r;
    }
}
