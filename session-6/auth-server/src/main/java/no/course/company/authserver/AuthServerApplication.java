package no.course.company.authserver;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * En liten autorisasjonsserver for kurset (Spring Authorization Server). Den er gitt: du endrer den ikke,
 * unntatt i ekstraoppgave 6.6 a (i fasiten legger den til audience). Klientene og utstederen ligger i application.yml.
 *
 * Den signerer tokens med en RSA-nøkkel som genereres ved hver oppstart. En omstart gir nye nøkler, så tokens fra
 * før omstarten slutter å virke (dette er en av feilene du kommer til å møte).
 */
@SpringBootApplication
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }

    /** Personene som kan logge inn. De samme to som i økt 5. */
    @Bean
    UserDetailsService users() {
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return new InMemoryUserDetailsManager(
                User.withUsername("user").password(encoder.encode("password")).roles("USER").build(),
                User.withUsername("admin").password(encoder.encode("admin")).roles("ADMIN").build());
    }

    /**
     * Legger rollene til den innloggede brukeren i access tokenet, som claimet «roles» (som i økt 5).
     * Et token for en maskin (Client Credentials) har ingen bruker, så det får ingen roller.
     */
    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> rolesInAccessToken() {
        return context -> {
            boolean accessToken = "access_token".equals(context.getTokenType().getValue());
            boolean forUser = AuthorizationGrantType.AUTHORIZATION_CODE.equals(context.getAuthorizationGrantType());
            if (accessToken) {
                // Oppgave 6.6 a: tokenet er ment for Company-API-et (standard aud er klient-id-en)
                context.getClaims().audience(List.of("company-api"));
            }
            if (accessToken && forUser) {
                context.getClaims().claim("roles", context.getPrincipal().getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        // Spring Security 7 legger også til FACTOR_PASSWORD (hvordan brukeren logget inn). Det er ikke en rolle.
                        .filter(authority -> authority.startsWith("ROLE_"))
                        .sorted()
                        .toList());
            }
        };
    }
}
