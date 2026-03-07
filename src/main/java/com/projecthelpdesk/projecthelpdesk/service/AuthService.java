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
    private final org.springframework.mail.javamail.JavaMailSender mailSender;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
            org.springframework.mail.javamail.JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.mailSender = mailSender;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered");
        }

        // Force all new registrations to be USER
        ERole eRole = ERole.USER;

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

        // Generate OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setOtp(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10));
        user.setEnabled(false); // User is disabled until OTP verification

        userRepository.save(user);

        // Send OTP via Email (try-catch to avoid blocking registration if mail fails,
        // but log it)
        sendOtpEmail(user.getEmail(), otp);

        // Return empty token or specific response indicating OTP sent
        // For now, we return null token to indicate "Action Required" on frontend
        return new AuthResponse(null, user.getId(), user.getEmail(), user.getFullName(),
                user.getRole().getRoleName().name());
    }

    private void sendOtpEmail(String to, String otp) {
        System.out.println("==================================================");
        System.out.println("SENDING OTP TO: " + to);
        System.out.println("OTP CODE: " + otp);
        System.out.println("==================================================");

        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setTo(to);
            message.setSubject("HelpDesk Registration OTP");
            message.setText("Your OTP for registration is: " + otp + "\n\nThis code expires in 10 minutes.");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            // Proceeding anyway since we logged it to console for dev/test
        }
    }

    public AuthResponse verifyOtp(com.projecthelpdesk.projecthelpdesk.dto.VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.isEnabled()) {
            // Already enabled, just login
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getRoleName().name());
            return new AuthResponse(token, user.getId(), user.getEmail(),
                    user.getFullName(), user.getRole().getRoleName().name());
        }

        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
            throw new BadRequestException("Invalid OTP");
        }

        if (user.getOtpExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired");
        }

        // OTP Valid
        user.setEnabled(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getRoleName().name());
        return new AuthResponse(token, user.getId(), user.getEmail(),
                user.getFullName(), user.getRole().getRoleName().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.isEnabled()) {
            throw new BadRequestException("Account not verified. Please verify your email.");
        }

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getRoleName().name());
        return new AuthResponse(token, user.getId(), user.getEmail(),
                user.getFullName(), user.getRole().getRoleName().name());
    }

    public AuthResponse processOAuth2Login(String email, String fullName, String provider) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            Role role = roleRepository.findByRoleName(ERole.USER)
                    .orElseGet(() -> {
                        Role newRole = new Role(ERole.USER);
                        return roleRepository.save(newRole);
                    });

            user = new User();
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPassword(null);
            user.setRole(role);
            user.setAuthProvider(provider);
            user.setEnabled(true);
            userRepository.save(user);
        } else {
            if (!user.isEnabled()) {
                user.setEnabled(true);
                userRepository.save(user);
            }
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getRoleName().name());
        return new AuthResponse(token, user.getId(), user.getEmail(),
                user.getFullName(), user.getRole().getRoleName().name());
    }
}
