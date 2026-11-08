package no.course.company.demo;

import java.time.Instant;
import java.util.List;

import no.course.company.auth.JwtProperties;
import no.course.company.tasks.TokenService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Demo D2: token-tjenesten slik den ser ut når oppgave 5.1 er løst.
 * Aktiv med profilene d2, d3 og d3-roles. Den erstatter stubben i tasks/ (@Primary).
 */
@Service
@Primary
@Profile({"d2", "d3", "d3-roles"})
public class DemoTokenService extends TokenService {

    public DemoTokenService(JwtEncoder encoder, JwtProperties properties) {
        super(encoder, properties);
    }

    @Override
    public String issue(Authentication authentication) {
        Instant now = Instant.now();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(properties.ttl()))
                .claim("roles", roles)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
