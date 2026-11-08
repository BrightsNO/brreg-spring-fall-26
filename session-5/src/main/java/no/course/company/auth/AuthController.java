package no.course.company.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.course.company.tasks.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** POST /auth/login: brukernavn og passord inn, et signert token ut. */
@RestController
@RequestMapping("/auth")
public class AuthController {

    public record LoginRequest(String username, String password) {
    }

    public record TokenResponse(@JsonProperty("access_token") String accessToken,
                                @JsonProperty("token_type") String tokenType,
                                @JsonProperty("expires_in") long expiresIn) {
    }

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final JwtProperties properties;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService,
                          JwtProperties properties) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.properties = properties;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        // Kaster BadCredentialsException (-> 401) hvis brukernavn eller passord er feil
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        String token = tokenService.issue(authentication);
        return new TokenResponse(token, "Bearer", properties.ttl().toSeconds());
    }
}
