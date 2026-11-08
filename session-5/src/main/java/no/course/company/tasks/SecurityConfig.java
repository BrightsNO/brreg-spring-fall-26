package no.course.company.tasks;

import no.course.company.security.RequestLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/** Oppgave 5.2, 5.3 og 5.6: API-et som resource server. */
@Configuration
@EnableMethodSecurity   // oppgave 5.6: slår på @PreAuthorize
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // /auth/** er ny i økt 5: du må kunne nå den uten token
                        .requestMatchers("/public/**", "/actuator/health", "/error", "/auth/login", "/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        // Oppgave 5.6: DELETE sjekkes av @PreAuthorize på CompanyService.delete(...)
                        .requestMatchers(HttpMethod.DELETE, "/api/**").authenticated()
                        .anyRequest().denyAll())
                // Oppgave 5.2: tokenet erstatter HTTP Basic. JwtDecoder-beanen i AuthConfig sjekker det.
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .addFilterAfter(new RequestLoggingFilter(), BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Oppgave 5.3: som standard kommer authorities fra claimet «scope» med prefikset SCOPE_.
     * Tokenet vårt har claimet «roles» med verdier som ROLE_USER, så vi leser det claimet og legger ikke til prefiks.
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("");
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
