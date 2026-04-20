package com.projecthelpdesk.projecthelpdesk.controller;

import com.projecthelpdesk.projecthelpdesk.dto.DepartmentRequest;
import com.projecthelpdesk.projecthelpdesk.dto.UpdateUserRequest;
import com.projecthelpdesk.projecthelpdesk.dto.UserDTO;
import com.projecthelpdesk.projecthelpdesk.entity.Department;
import com.projecthelpdesk.projecthelpdesk.entity.ERole;
import com.projecthelpdesk.projecthelpdesk.entity.Role;
import com.projecthelpdesk.projecthelpdesk.entity.User;
import com.projecthelpdesk.projecthelpdesk.exception.BadRequestException;
import com.projecthelpdesk.projecthelpdesk.exception.ResourceNotFoundException;
import com.projecthelpdesk.projecthelpdesk.repository.DepartmentRepository;
import com.projecthelpdesk.projecthelpdesk.repository.RoleRepository;
import com.projecthelpdesk.projecthelpdesk.repository.UserRepository;
import com.projecthelpdesk.projecthelpdesk.service.AuditService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditService auditService;

    public AdminController(UserRepository userRepository, RoleRepository roleRepository,
            DepartmentRepository departmentRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.auditService = auditService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> dtos = users.stream().map(user -> new UserDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().getRoleName().name(),
                user.getDepartment() != null ? user.getDepartment().getName() : "-",
                user.isEnabled(),
                user.getCreatedAt())).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request,
            Authentication auth) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String oldRole = user.getRole().getRoleName().name();

        // Update Role
        if (request.getRole() != null) {
            // HOD requires a department
            if ("HOD".equals(request.getRole()) && request.getDepartmentId() == null) {
                // Check if user already has a department
                if (user.getDepartment() == null) {
                    throw new BadRequestException("Department is mandatory for HOD role. Please select a department.");
                }
            }
            try {
                ERole roleEnum = ERole.valueOf(request.getRole());
                Role role = roleRepository.findByRoleName(roleEnum)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + request.getRole()));
                user.setRole(role);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role: " + request.getRole());
            }
        }

        // Update Department
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with id: " + request.getDepartmentId()));
            user.setDepartment(department);
        } else if ("USER".equals(request.getRole()) || "ADMIN".equals(request.getRole())) {
            user.setDepartment(null);
        }
        // Note: HOD and AGENT keep their department even if departmentId is null in the request

        userRepository.save(user);

        // Audit log
        auditService.log(auth.getName(), "UPDATE_USER", "User", id,
                "Updated user " + user.getFullName() + " role: " + oldRole + " → " + user.getRole().getRoleName().name());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication auth) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setEnabled(false); // soft delete
        userRepository.save(user);
        auditService.log(auth.getName(), "DISABLE_USER", "User", id, "Disabled user " + user.getFullName());
        return ResponseEntity.ok().build();
    }

    // ===== Department Management =====

    @PostMapping("/departments")
    public ResponseEntity<Department> createDepartment(@RequestBody DepartmentRequest request, Authentication auth) {
        Department dept = new Department();
        dept.setName(request.getName());
        dept.setDescription(request.getDescription());
        dept = departmentRepository.save(dept);
        auditService.log(auth.getName(), "CREATE_DEPARTMENT", "Department", dept.getId(), "Created department: " + dept.getName());
        return ResponseEntity.ok(dept);
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id,
            @RequestBody DepartmentRequest request, Authentication auth) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        if (request.getName() != null) dept.setName(request.getName());
        if (request.getDescription() != null) dept.setDescription(request.getDescription());
        departmentRepository.save(dept);
        auditService.log(auth.getName(), "UPDATE_DEPARTMENT", "Department", id, "Updated department: " + dept.getName());
        return ResponseEntity.ok(dept);
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id, Authentication auth) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        departmentRepository.delete(dept);
        auditService.log(auth.getName(), "DELETE_DEPARTMENT", "Department", id, "Deleted department: " + dept.getName());
        return ResponseEntity.ok().build();
    }

    // ===== Audit Log =====

    @GetMapping("/audit-log")
    public ResponseEntity<List<Map<String, Object>>> getAuditLog() {
        return ResponseEntity.ok(auditService.getRecentLogs());
    }
}
