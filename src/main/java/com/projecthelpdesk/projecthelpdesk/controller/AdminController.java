package com.projecthelpdesk.projecthelpdesk.controller;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
// @PreAuthorize("hasRole('ADMIN')") // In real implementation, secure this
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

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
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update Role
        if (request.getRole() != null) {
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
            // If demoting to USER or promoting to ADMIN, clear department (optional logic,
            // but clean)
            user.setDepartment(null);
        }

        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}
