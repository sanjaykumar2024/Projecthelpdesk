package com.projecthelpdesk.projecthelpdesk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projecthelpdesk.projecthelpdesk.dto.DashboardStats;
import com.projecthelpdesk.projecthelpdesk.entity.User;
import com.projecthelpdesk.projecthelpdesk.exception.BadRequestException;
import com.projecthelpdesk.projecthelpdesk.repository.UserRepository;
import com.projecthelpdesk.projecthelpdesk.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public DashboardController(DashboardService dashboardService, UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user != null && "HOD".equals(user.getRole().getRoleName().name())) {
            if (user.getDepartment() == null) {
                throw new BadRequestException("HOD is not assigned to any department");
            }
            return ResponseEntity.ok(dashboardService.getStatsByDepartment(user.getDepartment().getId()));
        }
        return ResponseEntity.ok(dashboardService.getStats());
    }
}
