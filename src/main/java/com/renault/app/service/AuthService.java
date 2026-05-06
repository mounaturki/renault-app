package com.renault.app.service;

import com.renault.app.dto.*;
import com.renault.app.model.Role;
import com.renault.app.model.User;
import com.renault.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private OtpService otpService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Email déjà utilisé")
                    .build();
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRoles(Collections.singleton(Role.CLIENT));
        user.setActive(true);
        user.setEmailVerified(false);

        userRepository.save(user);

        // Envoyer OTP par SMS au numéro de téléphone
        try {
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                otpService.generateAndSendOtp(user.getPhoneNumber());
            }
        } catch (Exception e) {
            System.out.println("SMS OTP non disponible: " + e.getMessage());
        }

        return AuthResponse.builder()
                .success(true)
                .message("Inscription réussie. Vérifiez votre téléphone pour l'OTP.")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Email ou mot de passe incorrect")
                    .build();
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.isActive()) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Compte désactivé")
                    .build();
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRoles().stream().map(Role::name).toArray(String[]::new))
                .build();

        String token = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .success(true)
                .token(token)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream().map(Role::name).collect(Collectors.toSet()))
                .message("Connexion réussie")
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String email = jwtService.extractUsername(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRoles().stream().map(Role::name).toArray(String[]::new))
                .build();

        if (!jwtService.validateToken(refreshToken, userDetails)) {
            return AuthResponse.builder()
                    .success(false)
                    .message("Refresh token invalide")
                    .build();
        }

        String newToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .success(true)
                .token(newToken)
                .refreshToken(newRefreshToken)
                .message("Token rafraîchi")
                .build();
    }
}