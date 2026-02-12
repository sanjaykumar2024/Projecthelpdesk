package com.projecthelpdesk.projecthelpdesk.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projecthelpdesk.projecthelpdesk.entity.TicketFeedback;

public interface TicketFeedbackRepository extends JpaRepository<TicketFeedback, Long> {
    Optional<TicketFeedback> findByTicketId(Long ticketId);

    boolean existsByTicketId(Long ticketId);
}
