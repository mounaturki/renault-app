package com.renault.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "L'email ou le téléphone est obligatoire")
    private String email;  // Peut être un email ou un numéro de téléphone

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}