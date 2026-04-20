package com.projecthelpdesk.projecthelpdesk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_activities")
public class TicketActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor; // Null if system action

    @Column(nullable = false)
    private String actionInfo; // e.g., "Status changed from OPEN to IN_PROGRESS"

    @Column(length = 50)
    private String activityType; // e.g., STATUS_CHANGE, ESCALATION, ASSIGNMENT

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public TicketActivity() {}

    public TicketActivity(Ticket ticket, User actor, String actionInfo, String activityType) {
        this.ticket = ticket;
        this.actor = actor;
        this.actionInfo = actionInfo;
        this.activityType = activityType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
    public User getActor() { return actor; }
    public void setActor(User actor) { this.actor = actor; }
    public String getActionInfo() { return actionInfo; }
    public void setActionInfo(String actionInfo) { this.actionInfo = actionInfo; }
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
