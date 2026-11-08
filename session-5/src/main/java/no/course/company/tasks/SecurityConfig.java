package no.course.company.tasks;

import no.course.company.security.RequestLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Sikkerhetskonfigurasjonen fra økt 4 (HTTP Basic, tilstandsløs, ingen CSRF, CORS på).
 *
 * Oppgave 5.2 og 5.3 endrer denne klassen:
 * TODO 5.2: bytt ut httpBasic(...) med oauth2ResourceServer(...) slik at API-et godtar en JWT.
 * TODO 5.3: map «roles»-claimet i tokenet til authorities som Spring Security sjekker.
 */
@Configuration
@Profile("!d3 & !d3-roles")
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // /auth/login er ny i økt 5: du må kunne nå den uten token
                        .requestMatchers("/public/**", "/actuator/health", "/error", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .addFilterAfter(new RequestLoggingFilter(), BasicAuthenticationFilter.class);
        return http.build();
    }
}
