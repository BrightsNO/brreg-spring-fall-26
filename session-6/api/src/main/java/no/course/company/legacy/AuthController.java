package no.course.company.legacy;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Økt 5: POST /auth/login. I økt 6 gjør autorisasjonsserveren dette, og denne klassen forsvinner. */
@RestController
@RequestMapping("/auth")
@ConditionalOnProperty(name = "app.legacy", havingValue = "true", matchIfMissing = true)
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
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        return new TokenResponse(tokenService.issue(authentication), "Bearer", properties.ttl().toSeconds());
    }
}
