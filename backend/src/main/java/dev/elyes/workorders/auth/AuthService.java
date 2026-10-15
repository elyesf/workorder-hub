package dev.elyes.workorders.auth;

import java.util.Locale;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.elyes.workorders.common.ConflictException;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    /** New accounts always get the USER role. ADMIN is only given by the demo data or directly in the database. */
    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = normalize(request.email());
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        AppUser user = users.save(new AppUser(
            email, passwordEncoder.encode(request.password()), request.displayName().trim(), Role.USER));
        return tokenService.issueFor(user);
    }

    @Transactional(readOnly = true)
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        // Same message for unknown email and wrong password, so the API does not reveal which emails exist
        AppUser user = users.findByEmailIgnoreCase(normalize(request.email()))
            .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        return tokenService.issueFor(user);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
