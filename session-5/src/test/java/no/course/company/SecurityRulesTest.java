package no.course.company;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Kjører de samme sjekkene som Postman-samlingen, uten å starte en server. */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityRulesTest {

    private static final String NEW_COMPANY = "{\"organizationNumber\":\"555666777\",\"name\":\"Test AS\"}";
    private final ObjectMapper json = new ObjectMapper();

    @Autowired
    MockMvc mvc;
    @Autowired
    JwtEncoder encoder;

    private JsonNode login(String user, String password) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + user + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    private String token(String user, String password) throws Exception {
        return login(user, password).get("access_token").asText();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    /** Et token signert med den ekte nøkkelen, med claims vi velger selv. */
    private String signed(Instant issuedAt, Instant expiresAt, String... roles) {
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder().subject("user").issuedAt(issuedAt)
                .claim("roles", List.of(roles));
        if (expiresAt != null) {
            claims.expiresAt(expiresAt);
        }
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims.build())).getTokenValue();
    }

    @Test
    void publicEndpointsAreOpen() throws Exception {
        mvc.perform(get("/public/info")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void loginGivesTokenWithTheRolesClaim() throws Exception {
        JsonNode response = login("user", "password");
        String[] parts = response.get("access_token").asText().split("\\.");
        JsonNode header = json.readTree(Base64.getUrlDecoder().decode(parts[0]));
        JsonNode payload = json.readTree(Base64.getUrlDecoder().decode(parts[1]));
        assertEquals("HS256", header.get("alg").asText());
        assertEquals("user", payload.get("sub").asText());
        assertEquals("ROLE_USER", payload.get("roles").get(0).asText());
        assertEquals(900, payload.get("exp").asLong() - payload.get("iat").asLong());
        assertEquals(900, response.get("expires_in").asLong());
    }

    @Test
    void wrongPasswordOnLoginGives401() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void noTokenGives401WithBearerChallenge() throws Exception {
        mvc.perform(get("/api/companies"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", startsWith("Bearer")));
    }

    @Test
    void userCanReadAndHasRoleUserInAJwtAuthentication() throws Exception {
        String token = token("user", "password");
        mvc.perform(get("/api/companies").header("Authorization", bearer(token))).andExpect(status().isOk());
        mvc.perform(get("/api/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("user"))
                .andExpect(jsonPath("$.authorities", hasItem("ROLE_USER")))
                .andExpect(jsonPath("$.type").value("JwtAuthenticationToken"));
    }

    @Test
    void userCannotWrite() throws Exception {
        String token = token("user", "password");
        mvc.perform(post("/api/companies").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_COMPANY))
                .andExpect(status().isForbidden())
                .andExpect(header().string("WWW-Authenticate", containsString("insufficient_scope")));
        // Oppgave 5.6: URL-regelen slipper gjennom alle innloggede brukere, @PreAuthorize på tjenesten sier nei
        mvc.perform(delete("/api/companies/123456789").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanWrite() throws Exception {
        String token = token("admin", "admin");
        mvc.perform(post("/api/companies").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_COMPANY))
                .andExpect(status().isCreated());
        mvc.perform(delete("/api/companies/555666777").header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void unknownCompanyIs404BecauseErrorIsPermitted() throws Exception {
        mvc.perform(get("/api/companies/000").header("Authorization", bearer(token("user", "password"))))
                .andExpect(status().isNotFound());
    }

    // ---- oppgave 5.5: dårlige tokens ------------------------------------------------------------------

    @Test
    void tamperedSignatureGives401() throws Exception {
        String token = token("user", "password");
        String tampered = token.substring(0, token.length() - 5) + (token.endsWith("AAAAA") ? "BBBBB" : "AAAAA");
        mvc.perform(get("/api/companies").header("Authorization", bearer(tampered)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", containsString("Invalid signature")));
    }

    @Test
    void tokenWithChangedPayloadGives401() throws Exception {
        String token = token("user", "password");
        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1])).replace("ROLE_USER", "ROLE_ADMIN");
        String forged = parts[0] + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes())
                + "." + parts[2];
        mvc.perform(post("/api/companies").header("Authorization", bearer(forged))
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_COMPANY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenGives401() throws Exception {
        Instant now = Instant.now();
        String expired = signed(now.minus(Duration.ofHours(2)), now.minus(Duration.ofHours(1)), "ROLE_USER");
        mvc.perform(get("/api/companies").header("Authorization", bearer(expired)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", containsString("Jwt expired at")));
    }

    @Test
    void tokenExpiredLessThan60SecondsAgoIsStillAccepted() throws Exception {
        Instant now = Instant.now();
        String recent = signed(now.minus(Duration.ofMinutes(15)), now.minusSeconds(30), "ROLE_USER");
        mvc.perform(get("/api/companies").header("Authorization", bearer(recent))).andExpect(status().isOk());
        String older = signed(now.minus(Duration.ofMinutes(15)), now.minusSeconds(90), "ROLE_USER");
        mvc.perform(get("/api/companies").header("Authorization", bearer(older))).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithoutExpIsAccepted() throws Exception {
        String forever = signed(Instant.now(), null, "ROLE_USER");
        mvc.perform(get("/api/companies").header("Authorization", bearer(forever))).andExpect(status().isOk());
    }

    @Test
    void algNoneGives401() throws Exception {
        var enc = Base64.getUrlEncoder().withoutPadding();
        String none = enc.encodeToString("{\"alg\":\"none\"}".getBytes()) + "."
                + enc.encodeToString("{\"sub\":\"admin\",\"roles\":[\"ROLE_ADMIN\"],\"exp\":9999999999}".getBytes()) + ".";
        mvc.perform(get("/api/companies").header("Authorization", bearer(none)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", containsString("Unsupported algorithm of none")));
    }

    @Test
    void basicAuthIsNoLongerAccepted() throws Exception {
        mvc.perform(get("/api/companies").with(httpBasic("user", "password")))
                .andExpect(status().isUnauthorized());
    }

    // ---- oppgave 5.7 (b): jwt()-post-processoren: ingen innlogging, ingen signatur, bare authorities --------

    @Test
    void jwtPostProcessorWithRoleUserCanRead() throws Exception {
        mvc.perform(get("/api/companies").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void jwtPostProcessorWithoutAuthoritiesGives403() throws Exception {
        mvc.perform(get("/api/companies").with(jwt()))
                .andExpect(status().isForbidden());
    }

    // ---- oppgave 5.7 (a): refresh tokens -------------------------------------------------------------

    @Test
    void refreshTokenGivesNewAccessToken() throws Exception {
        String refresh = login("user", "password").get("refresh_token").asText();
        String body = mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refresh_token\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String access = json.readTree(body).get("access_token").asText();
        mvc.perform(get("/api/companies").header("Authorization", bearer(access))).andExpect(status().isOk());
    }

    @Test
    void refreshTokenCannotBeUsedOnTheApi() throws Exception {
        String refresh = login("user", "password").get("refresh_token").asText();
        mvc.perform(get("/api/companies").header("Authorization", bearer(refresh)))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", containsString("A refresh token cannot be used here")));
    }

    @Test
    void accessTokenCannotBeUsedToRefresh() throws Exception {
        mvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refresh_token\":\"" + token("user", "password") + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
