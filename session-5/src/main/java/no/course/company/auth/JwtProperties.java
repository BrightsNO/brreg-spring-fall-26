package no.course.company.auth;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Innstillinger under «app.jwt» i application.yml. */
@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, Duration ttl, Duration refreshTtl) {
}
