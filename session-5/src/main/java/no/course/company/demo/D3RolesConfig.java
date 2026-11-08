package no.course.company.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * Demo D3, andre halvdel: fortell Spring Security hvilket claim som inneholder authorities.
 * Som standard leser den claimet «scope» (eller «scp») og setter SCOPE_ foran verdiene.
 * Tokenet vårt har «roles»: ["ROLE_USER"], så vi leser det claimet og legger ikke til prefiks.
 */
@Configuration
@Profile("d3-roles")
public class D3RolesConfig {

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
