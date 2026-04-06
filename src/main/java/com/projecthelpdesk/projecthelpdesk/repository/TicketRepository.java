package com.projecthelpdesk.projecthelpdesk.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.projecthelpdesk.projecthelpdesk.entity.Priority;
import com.projecthelpdesk.projecthelpdesk.entity.Ticket;
import com.projecthelpdesk.projecthelpdesk.entity.TicketStatus;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCreatorId(Long creatorId);

    List<Ticket> findByAssignedAgentId(Long agentId);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByPriority(Priority priority);

    List<Ticket> findByDepartmentId(Long departmentId);

    @Query("SELECT t FROM Ticket t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:departmentId IS NULL OR t.department.id = :departmentId) AND " +
            "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR t.createdAt <= :endDate)")
    List<Ticket> findByFilters(
            @Param("status") TicketStatus status,
            @Param("priority") Priority priority,
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    long countByStatus(TicketStatus status);

    long countByPriority(Priority priority);

    long countByDepartmentId(Long departmentId);

    @Query("SELECT t FROM Ticket t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Ticket> searchByKeyword(@Param("q") String keyword);

    @Query("SELECT t FROM Ticket t WHERE t.dueDate IS NOT NULL AND t.dueDate < :now " +
            "AND t.status NOT IN (com.projecthelpdesk.projecthelpdesk.entity.TicketStatus.RESOLVED, " +
            "com.projecthelpdesk.projecthelpdesk.entity.TicketStatus.CLOSED) AND t.escalated = false")
    List<Ticket> findOverdueTickets(@Param("now") LocalDateTime now);
}
