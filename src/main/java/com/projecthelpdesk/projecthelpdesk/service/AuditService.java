package com.projecthelpdesk.projecthelpdesk.service;

import org.springframework.stereotype.Service;

import com.projecthelpdesk.projecthelpdesk.entity.AuditLog;
import com.projecthelpdesk.projecthelpdesk.entity.User;
import com.projecthelpdesk.projecthelpdesk.repository.AuditLogRepository;
import com.projecthelpdesk.projecthelpdesk.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public void log(String email, String action, String targetType, Long targetId, String details) {
        User admin = userRepository.findByEmail(email).orElse(null);
        AuditLog log = new AuditLog();
        log.setAdmin(admin);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    public List<Map<String, Object>> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc().stream()
                .map(log -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", log.getId());
                    map.put("action", log.getAction());
                    map.put("targetType", log.getTargetType());
                    map.put("targetId", log.getTargetId());
                    map.put("details", log.getDetails());
                    map.put("timestamp", log.getTimestamp());
                    map.put("adminName", log.getAdmin() != null ? log.getAdmin().getFullName() : "System");
                    return map;
                }).collect(Collectors.toList());
    }
}
