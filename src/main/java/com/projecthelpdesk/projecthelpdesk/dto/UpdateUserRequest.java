package com.projecthelpdesk.projecthelpdesk.dto;

public class UpdateUserRequest {
    private String role; // "USER", "AGENT", "ADMIN"
    private Long departmentId; // Optional, for agents

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
