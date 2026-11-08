package no.course.company.tasks;

import no.course.company.auth.JwtProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Service;

/**
 * Oppgave 5.1: lager JWT-en som /auth/login gir til klienten.
 *
 * TODO: implementer issue(...). Bygg et JwtClaimsSet med
 *   - subject: brukernavnet,
 *   - issuedAt: nå, expiresAt: nå + properties.ttl(),
 *   - et claim «roles»: rollenavnene til brukeren (for eksempel ["ROLE_USER"]).
 *     Spring Security 7 legger også til en FACTOR_PASSWORD-authority etter innlogging. Den sier hvordan brukeren logget inn, og er
 *     ikke en rolle: behold bare authorities som starter med «ROLE_».
 * Kall deretter encoder.encode(JwtEncoderParameters.from(header, claims)) med en JwsHeader for MacAlgorithm.HS256
 * og returner getTokenValue().
 */
@Service
public class TokenService {

    protected final JwtEncoder encoder;
    protected final JwtProperties properties;

    public TokenService(JwtEncoder encoder, JwtProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public String issue(Authentication authentication) {
        throw new UnsupportedOperationException("TODO task 5.1: create and sign the token");
    }
}
