package dev.elyes.workorders.auth;

import java.time.Instant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 120) String displayName) {
    }

    public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {
    }

    public record AuthResponse(
        String token,
        Instant expiresAt,
        String email,
        String displayName,
        Role role) {
    }
}
