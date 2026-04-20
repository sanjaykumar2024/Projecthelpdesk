package com.projecthelpdesk.projecthelpdesk.repository;

import com.projecthelpdesk.projecthelpdesk.entity.TicketActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketActivityRepository extends JpaRepository<TicketActivity, Long> {
    List<TicketActivity> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}
