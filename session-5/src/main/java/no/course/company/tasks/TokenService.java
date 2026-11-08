package no.course.company.tasks;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import no.course.company.auth.JwtProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Oppgave 5.1 og 5.7 (a): lager JWT-ene som /auth/login og /auth/refresh gir til klienten. */
@Service
public class TokenService {

    /** Markerer et refresh token. Resource serveren avviser tokens med dette claimet (se AuthConfig). */
    public static final String TOKEN_USE = "token_use";
    public static final String REFRESH = "refresh";

    protected final JwtEncoder encoder;
    protected final JwtProperties properties;

    public TokenService(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    /** Oppgave 5.1: access tokenet med kort levetid. */
    public String issue(Authentication authentication) {
        return sign(authentication, properties.ttl(), null);
    }

    /** Oppgave 5.7 (a): refresh tokenet med lang levetid. Bare /auth/refresh godtar det. */
    public String issueRefresh(Authentication authentication) {
        return sign(authentication, properties.refreshTtl(), REFRESH);
    }

    private String sign(Authentication authentication, Duration lifetime, String tokenUse) {
        Instant now = Instant.now();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                // Spring Security 7 legger også til FACTOR_PASSWORD (hvordan brukeren logget inn). Det er ikke en rolle.
                .filter(authority -> authority.startsWith("ROLE_"))
                .sorted()
                .toList();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(lifetime))
                .claim("roles", roles);
        if (tokenUse != null) {
            claims.claim(TOKEN_USE, tokenUse);
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }
}
