package com.projecthelpdesk.projecthelpdesk.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecthelpdesk.projecthelpdesk.dto.AuthResponse;
import com.projecthelpdesk.projecthelpdesk.dto.LoginRequest;
import com.projecthelpdesk.projecthelpdesk.dto.RegisterRequest;
import com.projecthelpdesk.projecthelpdesk.exception.BadRequestException;
import com.projecthelpdesk.projecthelpdesk.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(
            @Valid @RequestBody com.projecthelpdesk.projecthelpdesk.dto.VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody Map<String, String> body) {
        String credential = body.get("credential");
        if (credential == null || credential.isBlank()) {
            throw new BadRequestException("Missing Google credential");
        }

        try {
            // Verify token with Google's tokeninfo API
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + credential))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new BadRequestException("Invalid Google token");
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> tokenInfo = mapper.readValue(response.body(), Map.class);

            String email = (String) tokenInfo.get("email");
            String name = (String) tokenInfo.get("name");

            if (email == null) {
                throw new BadRequestException("Google account has no email");
            }
            if (name == null || name.isBlank()) {
                name = email.split("@")[0];
            }

            return ResponseEntity.ok(authService.processOAuth2Login(email, name, "GOOGLE"));
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Google sign-in failed: " + e.getMessage());
        }
    }
}
