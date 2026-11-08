package no.course.company.tasks;

import no.course.company.security.RequestLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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

/**
 * Sikkerhetskonfigurasjonen fra økt 5: en resource server som sjekker en JWT og leser rollene fra
 * «roles»-claimet.
 *
 * Oppgave 6.4 endrer denne klassen:
 * TODO 6.4 a: la URL-reglene sjekke scopes (SCOPE_companies.read og SCOPE_companies.write) i stedet for roller.
 * TODO 6.4 b: la konverteren gi BÅDE scopes (SCOPE_...) og roller (ROLE_...) som authorities.
 */
@Configuration
@Profile("!d3")
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // /auth/login er kode fra økt 5. Den forsvinner sammen med legacy-pakken (oppgave 6.3).
                        .requestMatchers("/public/**", "/actuator/health", "/error", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").authenticated()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .addFilterAfter(new RequestLoggingFilter(), BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    /** Fra økt 5: authorities kommer fra claimet «roles» (uten prefiks). Scopes leses ikke. */
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
