package com.projecthelpdesk.projecthelpdesk.dto;

public class AuthResponse {

    private String token;
    private Long userId;
    private String email;
    private String fullName;
    private String role;
    private Long departmentId;
    private String departmentName;

    public AuthResponse() {
    }

    public AuthResponse(String token, Long userId, String email, String fullName, String role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public AuthResponse(String token, Long userId, String email, String fullName, String role,
                         Long departmentId, String departmentName) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

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

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
}
