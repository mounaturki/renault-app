package com.renault.app.dto;

import java.util.Set;

public class AuthResponse {
    private boolean success;
    private String token;
    private String refreshToken;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private String message;
    private boolean otpRequired;

    // Builder manuel
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AuthResponse response = new AuthResponse();

        public Builder success(boolean success) {
            response.success = success;
            return this;
        }

        public Builder token(String token) {
            response.token = token;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            response.refreshToken = refreshToken;
            return this;
        }

        public Builder email(String email) {
            response.email = email;
            return this;
        }

        public Builder firstName(String firstName) {
            response.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            response.lastName = lastName;
            return this;
        }

        public Builder roles(Set<String> roles) {
            response.roles = roles;
            return this;
        }

        public Builder message(String message) {
            response.message = message;
            return this;
        }

        public Builder otpRequired(boolean otpRequired) {
            response.otpRequired = otpRequired;
            return this;
        }

        public AuthResponse build() {
            return response;
        }
    }

    // GETTERS
    public boolean isSuccess() { return success; }
    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Set<String> getRoles() { return roles; }
    public String getMessage() { return message; }
    public boolean isOtpRequired() { return otpRequired; }
}