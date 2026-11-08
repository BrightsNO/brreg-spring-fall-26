package no.course.company.tasks;

import java.util.ArrayList;

import no.course.company.security.RequestLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/** Oppgave 6.3 og 6.4: en resource server som sjekker scopes i URL-reglene og roller på metodene. */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Oppgave 6.3: /auth/login er borte sammen med legacy-pakken
                        .requestMatchers("/public/**", "/actuator/health", "/error").permitAll()
                        // Oppgave 6.4 a: scopet sier hva klientappen kan gjøre ...
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAuthority("SCOPE_companies.read")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAuthority("SCOPE_companies.write")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasAuthority("SCOPE_companies.write")
                        .anyRequest().denyAll())
                // ... og rollen sier hva brukeren kan gjøre: @PreAuthorize på CompanyService
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .addFilterAfter(new RequestLoggingFilter(), BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Oppgave 6.4 b: både scopes (claimet «scope», prefiks SCOPE_, standard) og roller (claimet «roles», uten prefiks)
     * blir til authorities.
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var scopes = new JwtGrantedAuthoritiesConverter();
        var roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("");
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var authorities = new ArrayList<GrantedAuthority>(scopes.convert(jwt));
            authorities.addAll(roles.convert(jwt));
            return authorities;
        });
        return converter;
    }
}
