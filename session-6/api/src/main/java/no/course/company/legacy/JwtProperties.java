package no.course.company.legacy;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Innstillinger under «app.jwt» i application.yml. En del av innloggingskoden fra økt 5. */
@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, Duration ttl) {
}
