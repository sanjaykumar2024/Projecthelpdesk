package com.projecthelpdesk.projecthelpdesk.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.projecthelpdesk.projecthelpdesk.dto.AuthResponse;
import com.projecthelpdesk.projecthelpdesk.dto.LoginRequest;
import com.projecthelpdesk.projecthelpdesk.dto.RegisterRequest;
import com.projecthelpdesk.projecthelpdesk.entity.ERole;
import com.projecthelpdesk.projecthelpdesk.entity.Role;
import com.projecthelpdesk.projecthelpdesk.entity.User;
import com.projecthelpdesk.projecthelpdesk.exception.BadRequestException;
import com.projecthelpdesk.projecthelpdesk.repository.RoleRepository;
import com.projecthelpdesk.projecthelpdesk.repository.UserRepository;
import com.projecthelpdesk.projecthelpdesk.security.JwtUtil;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered");
        }

        ERole eRole;
        try {
            eRole = ERole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + request.getRole());
        }

        Role role = roleRepository.findByRoleName(eRole)
                .orElseGet(() -> {
                    Role newRole = new Role(eRole);
                    return roleRepository.save(newRole);
                });

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getRoleName().name());
        return new AuthResponse(token, user.getId(), user.getEmail(),
                user.getFullName(), user.getRole().getRoleName().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getRoleName().name());
        return new AuthResponse(token, user.getId(), user.getEmail(),
                user.getFullName(), user.getRole().getRoleName().name());
    }
}
