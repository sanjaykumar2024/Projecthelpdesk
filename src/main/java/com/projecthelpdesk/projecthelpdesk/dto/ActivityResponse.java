package com.projecthelpdesk.projecthelpdesk.dto;

import com.projecthelpdesk.projecthelpdesk.entity.TicketActivity;
import java.time.LocalDateTime;

public class ActivityResponse {
    private Long id;
    private String actionInfo;
    private String activityType;
    private LocalDateTime createdAt;
    private Long actorId;
    private String actorName;

    public ActivityResponse(TicketActivity activity) {
        this.id = activity.getId();
        this.actionInfo = activity.getActionInfo();
        this.activityType = activity.getActivityType();
        this.createdAt = activity.getCreatedAt();
        if (activity.getActor() != null) {
            this.actorId = activity.getActor().getId();
            this.actorName = activity.getActor().getFullName();
        } else {
            this.actorName = "System";
        }
    }

    public Long getId() { return id; }
    public String getActionInfo() { return actionInfo; }
    public String getActivityType() { return activityType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getActorId() { return actorId; }
    public String getActorName() { return actorName; }
}
