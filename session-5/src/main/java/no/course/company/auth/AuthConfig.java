package no.course.company.auth;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import no.course.company.tasks.TokenService;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Brukere, passordhashing og signeringsnøkkelen. Gitt i økt 5; du endrer ikke denne filen.
 *
 * Brukerne og encoderen er de samme som i økt 4. De nye delene er AuthenticationManager
 * (brukes av /auth/login til å sjekke brukernavn og passord) og JwtEncoder / JwtDecoder, som deler
 * én hemmelighet (HS256 = HMAC med SHA-256).
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class AuthConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var user = User.withUsername("user")
                .password(encoder.encode("password"))
                .roles("USER")
                .build();
        var admin = User.withUsername("admin")
                .password(encoder.encode("admin"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user, admin);
    }

    /** Sjekker brukernavn og passord mot UserDetailsService. Brukes av /auth/login. */
    @Bean
    AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder encoder) {
        var provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    SecretKey jwtSecretKey(JwtProperties properties) {
        return new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /** Signerer tokens. Bare koden som utsteder tokens trenger denne. */
    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    /**
     * Verifiserer tokens: signatur og utløp. Resource serveren (oppgave 5.2) bruker denne beanen.
     * Oppgave 5.7 (a): den avviser også refresh tokens, så et refresh token kan ikke brukes som Bearer-token mot API-et.
     */
    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        var decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> notRefresh = jwt -> TokenService.REFRESH.equals(jwt.getClaimAsString(TokenService.TOKEN_USE))
                ? OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "A refresh token cannot be used here", null))
                : OAuth2TokenValidatorResult.success();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(), notRefresh));
        return decoder;
    }
}
