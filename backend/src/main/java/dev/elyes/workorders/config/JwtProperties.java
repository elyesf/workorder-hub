package dev.elyes.workorders.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings for the tokens this API issues.
 * The secret signs tokens with HS256, so it must be at least 32 bytes long.
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration ttl) {

    public JwtProperties {
        if (secret == null || secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes for HS256");
        }
        if (ttl == null) {
            ttl = Duration.ofHours(8);
        }
    }
}
