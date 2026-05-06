package com.renault.app.controller;

import com.renault.app.dto.*;
import com.renault.app.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private OtpService otpService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String identifier = request.getEmail() != null ? request.getEmail() : "unknown";

        return ResponseEntity.ok(Map.of(
                "message", "Utilisez Keycloak pour obtenir un token JWT",
                "keycloak_url", "http://localhost:8080/realms/renault-realm/protocol/openid-connect/token",
                "grant_type", "password",
                "client_id", "renault-app",
                "username", identifier,
                "info", "Envoyez ces donnees en POST a l'URL Keycloak pour obtenir un access_token"
        ));
    }

    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody OtpRequest request) {
        try {
            String otp = otpService.generateAndSendOtp(request.getPhoneNumber());
            return ResponseEntity.ok().body(Map.of(
                    "message", "OTP envoyé (simulé) au " + request.getPhoneNumber(),
                    "phoneNumber", request.getPhoneNumber(),
                    "otp", otp  // Visible pour debug
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        boolean valid = otpService.verifyOtp(request.getPhoneNumber(), request.getOtpCode());
        if (valid) {
            otpService.clearOtp(request.getPhoneNumber());
            return ResponseEntity.ok().body(Map.of(
                    "message", "OTP vérifié avec succès",
                    "access_granted", true
            ));
        }
        return ResponseEntity.badRequest().body(Map.of(
                "message", "OTP invalide ou expiré",
                "access_granted", false
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        String identifier = request.getEmail() != null ? request.getEmail() : "unknown";

        return ResponseEntity.ok(Map.of(
                "message", "Inscription via Keycloak",
                "info", "Utilisez l'API Keycloak ou le service KeycloakService pour creer un utilisateur",
                "username", identifier,
                "email", request.getEmail()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(Map.of(
                "message", "Utilisez Keycloak pour rafraichir le token",
                "keycloak_url", "http://localhost:8080/realms/renault-realm/protocol/openid-connect/token",
                "grant_type", "refresh_token",
                "client_id", "renault-app",
                "refresh_token", request.getRefreshToken()
        ));
    }
}