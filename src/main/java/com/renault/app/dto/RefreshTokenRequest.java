package com.renault.app.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequest {
    @NotBlank(message = "Le refresh token est obligatoire")
    private String refreshToken;

    // GETTERS
    public String getRefreshToken() { return refreshToken; }

    // SETTERS
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}