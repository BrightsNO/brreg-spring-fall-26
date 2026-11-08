package no.course.company.auth;

import javax.crypto.SecretKey;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.course.company.tasks.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** POST /auth/login: brukernavn og passord inn, et signert token ut. POST /auth/refresh (oppgave 5.7 a): refresh token inn. */
@RestController
@RequestMapping("/auth")
public class AuthController {

    public record LoginRequest(String username, String password) {
    }

    public record RefreshRequest(@JsonProperty("refresh_token") String refreshToken) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TokenResponse(@JsonProperty("access_token") String accessToken,
                                @JsonProperty("token_type") String tokenType,
                                @JsonProperty("expires_in") long expiresIn,
                                @JsonProperty("refresh_token") String refreshToken) {
    }

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final JwtProperties properties;
    private final UserDetailsService users;
    private final NimbusJwtDecoder refreshDecoder;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService,
                          JwtProperties properties, UserDetailsService users, SecretKey jwtSecretKey) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.properties = properties;
        this.users = users;
        // Ikke decoder-beanen: den avviser refresh tokens. Denne sjekker bare signatur og utløp.
        this.refreshDecoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        // Kaster BadCredentialsException (-> 401) hvis brukernavn eller passord er feil
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        return new TokenResponse(tokenService.issue(authentication), "Bearer", properties.ttl().toSeconds(),
                tokenService.issueRefresh(authentication));
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshRequest request) {
        Jwt jwt;
        try {
            jwt = refreshDecoder.decode(request.refreshToken());
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid refresh token", e);
        }
        if (!TokenService.REFRESH.equals(jwt.getClaimAsString(TokenService.TOKEN_USE))) {
            throw new BadCredentialsException("Not a refresh token");
        }
        // Last brukeren på nytt: roller som er endret siden innloggingen blir plukket opp
        UserDetails user = users.loadUserByUsername(jwt.getSubject());
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                user.getUsername(), null, user.getAuthorities());
        return new TokenResponse(tokenService.issue(authentication), "Bearer", properties.ttl().toSeconds(), null);
    }
}
