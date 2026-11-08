package no.course.company;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Oppgave 6.5: reglene til API-et, testet uten autorisasjonsserver.
 * jwt()-post-processoren legger en ferdig Jwt i SecurityContext: JwtDecoder og konverteren vår brukes
 * ikke, så vi gir authorities selv. Konverteren har en egen test.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityRulesTest {

    private static final String NEW_COMPANY = "{\"organizationNumber\":\"555666777\",\"name\":\"Test AS\"}";

    private static SimpleGrantedAuthority auth(String name) {
        return new SimpleGrantedAuthority(name);
    }

    @Autowired
    MockMvc mvc;
    @Autowired
    JwtAuthenticationConverter converter;

    @Test
    void noTokenGives401() throws Exception {
        mvc.perform(get("/api/companies")).andExpect(status().isUnauthorized());
    }

    @Test
    void publicEndpointsAreOpen() throws Exception {
        mvc.perform(get("/public/info")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void readScopeCanRead() throws Exception {
        mvc.perform(get("/api/companies").with(jwt().authorities(auth("SCOPE_companies.read"))))
                .andExpect(status().isOk());
    }

    @Test
    void validTokenWithoutAuthoritiesGives403() throws Exception {
        mvc.perform(get("/api/companies").with(jwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void roleAloneIsNotEnoughToRead() throws Exception {
        mvc.perform(get("/api/companies").with(jwt().authorities(auth("ROLE_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void machineWithReadScopeCannotWrite() throws Exception {
        mvc.perform(post("/api/companies").with(jwt().authorities(auth("SCOPE_companies.read")))
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_COMPANY))
                .andExpect(status().isForbidden());
    }

    @Test
    void writeScopeWithoutAdminRoleCannotWrite() throws Exception {
        var user = jwt().authorities(auth("SCOPE_companies.read"), auth("SCOPE_companies.write"), auth("ROLE_USER"));
        mvc.perform(post("/api/companies").with(user)
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_COMPANY))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/companies/123456789").with(user)).andExpect(status().isForbidden());
    }

    @Test
    void adminRoleWithoutWriteScopeCannotWrite() throws Exception {
        var admin = jwt().authorities(auth("SCOPE_companies.read"), auth("ROLE_ADMIN"));
        mvc.perform(post("/api/companies").with(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_COMPANY))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminWithWriteScopeCanWrite() throws Exception {
        var admin = jwt().authorities(auth("SCOPE_companies.read"), auth("SCOPE_companies.write"), auth("ROLE_ADMIN"));
        mvc.perform(post("/api/companies").with(admin)
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_COMPANY))
                .andExpect(status().isCreated());
        mvc.perform(delete("/api/companies/555666777").with(admin)).andExpect(status().isNoContent());
    }

    @Test
    void unknownCompanyIs404() throws Exception {
        mvc.perform(get("/api/companies/000").with(jwt().authorities(auth("SCOPE_companies.read"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void meShowsTheAuthentication() throws Exception {
        mvc.perform(get("/api/me").with(jwt().authorities(auth("SCOPE_companies.read"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("JwtAuthenticationToken"))
                .andExpect(jsonPath("$.authorities[0]").value("SCOPE_companies.read"));
    }

    // ---- selve konverteren: her blir scope- og roles-claims til authorities ----------------

    private Set<String> authoritiesOf(Jwt jwt) {
        return converter.convert(jwt).getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                // Spring Security 7 legger til FACTOR_BEARER (hvordan brukeren autentiserte seg). Det er verken et scope eller en rolle.
                .filter(authority -> !authority.startsWith("FACTOR_"))
                .collect(Collectors.toSet());
    }

    private static Jwt.Builder token() {
        return Jwt.withTokenValue("test").header("alg", "RS256").subject("someone")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60));
    }

    @Test
    void converterGivesScopesAndRoles() {
        Jwt jwt = token().claim("scope", List.of("companies.read", "openid"))
                .claim("roles", List.of("ROLE_ADMIN")).build();
        assertEquals(Set.of("SCOPE_companies.read", "SCOPE_openid", "ROLE_ADMIN"), authoritiesOf(jwt));
    }

    @Test
    void converterGivesOnlyScopesForAMachineToken() {
        Jwt jwt = token().claim("scope", List.of("companies.read")).build();
        assertEquals(Set.of("SCOPE_companies.read"), authoritiesOf(jwt));
    }

    @Test
    void jwtPostProcessorDoesNotUseOurConverter() throws Exception {
        // Et token med roles-claim, men uten authorities: jwt() leser bare «scope» selv, så rollen går tapt
        mvc.perform(delete("/api/companies/123456789")
                        .with(jwt().authorities(auth("SCOPE_companies.write")).jwt(j -> j.claim("roles", List.of("ROLE_ADMIN")))))
                .andExpect(status().isForbidden());
    }
}
