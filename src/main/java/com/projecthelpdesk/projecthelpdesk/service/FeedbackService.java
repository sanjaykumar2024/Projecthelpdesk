package com.projecthelpdesk.projecthelpdesk.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.projecthelpdesk.projecthelpdesk.dto.FeedbackRequest;
import com.projecthelpdesk.projecthelpdesk.dto.FeedbackResponse;
import com.projecthelpdesk.projecthelpdesk.entity.Ticket;
import com.projecthelpdesk.projecthelpdesk.entity.TicketFeedback;
import com.projecthelpdesk.projecthelpdesk.entity.TicketStatus;
import com.projecthelpdesk.projecthelpdesk.entity.User;
import com.projecthelpdesk.projecthelpdesk.exception.BadRequestException;
import com.projecthelpdesk.projecthelpdesk.exception.ResourceNotFoundException;
import com.projecthelpdesk.projecthelpdesk.repository.TicketFeedbackRepository;
import com.projecthelpdesk.projecthelpdesk.repository.TicketRepository;
import com.projecthelpdesk.projecthelpdesk.repository.UserRepository;

@Service
public class FeedbackService {

    private final TicketFeedbackRepository feedbackRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public FeedbackService(TicketFeedbackRepository feedbackRepository, TicketRepository ticketRepository,
            UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    public FeedbackResponse submitFeedback(Long ticketId, FeedbackRequest request, String email) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED) {
            throw new BadRequestException("Feedback can only be submitted for resolved or closed tickets");
        }

        if (feedbackRepository.findByTicketId(ticketId).isPresent()) {
            throw new BadRequestException("Feedback already submitted for this ticket");
        }

        TicketFeedback feedback = new TicketFeedback();
        feedback.setTicket(ticket);
        feedback.setUser(user);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());
        feedback = feedbackRepository.save(feedback);
        return mapToResponse(feedback);
    }

    public FeedbackResponse getFeedback(Long ticketId) {
        Optional<TicketFeedback> feedback = feedbackRepository.findByTicketId(ticketId);
        return feedback.map(this::mapToResponse).orElse(null);
    }

    private FeedbackResponse mapToResponse(TicketFeedback f) {
        FeedbackResponse r = new FeedbackResponse();
        r.setId(f.getId());
        r.setRating(f.getRating());
        r.setComment(f.getComment());
        r.setUserName(f.getUser().getFullName());
        r.setCreatedAt(f.getCreatedAt());
        return r;
    }
}
