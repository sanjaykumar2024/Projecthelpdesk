package com.projecthelpdesk.projecthelpdesk.dto;

import java.time.LocalDateTime;

public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String department;
    private boolean enabled;
    private LocalDateTime createdAt;

    public UserDTO(Long id, String fullName, String email, String role, String department, boolean enabled,
            LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.department = department;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getDepartment() {
        return department;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
