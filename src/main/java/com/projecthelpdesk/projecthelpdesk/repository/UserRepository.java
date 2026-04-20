package com.projecthelpdesk.projecthelpdesk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.projecthelpdesk.projecthelpdesk.entity.ERole;
import com.projecthelpdesk.projecthelpdesk.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByDepartmentIdAndRole_RoleName(Long departmentId, ERole roleName);
}
