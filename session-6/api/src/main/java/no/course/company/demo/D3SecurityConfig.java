package no.course.company.demo;

import java.util.ArrayList;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Demo D3: scopes i URL-reglene (hva klientappen kan gjøre), roller på metodene (hva brukeren kan gjøre).
 * Start med --spring.profiles.active=d3
 */
@Configuration
@Profile("d3")
@EnableMethodSecurity
public class D3SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**", "/actuator/health", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAuthority("SCOPE_companies.read")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAuthority("SCOPE_companies.write")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasAuthority("SCOPE_companies.write")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .addFilterAfter(new RequestLoggingFilter(), BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    /** Begge: claimet «scope» gir SCOPE_x, claimet «roles» gir ROLE_x. */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var scopes = new JwtGrantedAuthoritiesConverter();   // standard: claimet scope eller scp, prefiks SCOPE_
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
