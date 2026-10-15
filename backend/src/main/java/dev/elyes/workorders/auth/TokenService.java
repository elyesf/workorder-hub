package dev.elyes.workorders.auth;

import java.time.Instant;
import java.util.List;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import dev.elyes.workorders.config.JwtProperties;

/** Issues signed access tokens. Validation is done by Spring's JwtDecoder in SecurityConfig. */
@Service
public class TokenService {

    static final String ISSUER = "workorder-hub";

    private final JwtEncoder encoder;
    private final JwtProperties properties;

    public TokenService(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public AuthDtos.AuthResponse issueFor(AppUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.ttl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(ISSUER)
            .subject(user.getEmail())
            .issuedAt(now)
            .expiresAt(expiresAt)
            .claim("name", user.getDisplayName())
            .claim("roles", List.of(user.getRole().name()))
            .build();
        // The header must name HS256 explicitly, the encoder defaults to RS256
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AuthDtos.AuthResponse(token, expiresAt, user.getEmail(), user.getDisplayName(), user.getRole());
    }
}
